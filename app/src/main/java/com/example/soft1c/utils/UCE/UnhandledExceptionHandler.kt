package com.example.soft1c.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.example.soft1c.utils.UCE.UCEActivity
import com.example.soft1c.utils.UCEHandler.COMMA_SEPARATED_EMAIL_ADDRESSES
import com.example.soft1c.utils.UCEHandler.DEFAULT_HANDLER_PACKAGE_NAME
import com.example.soft1c.utils.UCEHandler.EXTRA_ACTIVITY_LOG
import com.example.soft1c.utils.UCEHandler.EXTRA_STACK_TRACE
import com.example.soft1c.utils.UCEHandler.MAX_STACK_TRACE_SIZE
import com.example.soft1c.utils.UCEHandler.SHARED_PREFERENCES_FIELD_TIMESTAMP
import com.example.soft1c.utils.UCEHandler.SHARED_PREFERENCES_FILE
import com.example.soft1c.utils.UCEHandler.TAG
import com.example.soft1c.utils.UCEHandler.UCE_HANDLER_PACKAGE_NAME
import timber.log.Timber
import java.io.PrintWriter
import java.io.StringWriter
import java.lang.ref.WeakReference
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayDeque

class UnhandledExceptionHandler(builder: Builder) {
    private val activityLog = ArrayDeque<String>(UCEHandler.MAX_ACTIVITIES_IN_LOG)
    @SuppressLint("StaticFieldLeak")
    private lateinit var application: Application
    private var isInBackground = true
    private var isBackgroundMode = false
    private var isUCEHEnabled = false
    private var isTrackActivitiesEnabled = false
    private var lastActivityCreated = WeakReference<Activity>(null)

    init {
        isUCEHEnabled = builder.isUCEHEnabled
        isTrackActivitiesEnabled = builder.isTrackActivitiesEnabled
        isBackgroundMode = builder.isBackgroundModeEnabled
        COMMA_SEPARATED_EMAIL_ADDRESSES = builder.commaSeparatedEmailAddresses
        setUCEHandler(builder.context)
    }

    private fun setUCEHandler(context: Context?) {
        try {
            if (context != null) {
                val oldHandler = Thread.getDefaultUncaughtExceptionHandler()
                if (oldHandler != null && oldHandler.javaClass.name.startsWith(
                        UCE_HANDLER_PACKAGE_NAME
                    )
                ) {
                    Timber.tag(TAG).e("UCEHandler was already installed, doing nothing!")
                } else {
                    if (oldHandler != null && !oldHandler.javaClass.name.startsWith(
                            DEFAULT_HANDLER_PACKAGE_NAME
                        )
                    ) {
                        Timber.tag(TAG)
                            .e("You already have an UncaughtExceptionHandler. If you use a custom UncaughtExceptionHandler, it should be initialized after UCEHandler! Installing anyway, but your original handler will not be called.")
                    }
                    application = context.applicationContext as Application
                    // Setup UCE Handler.
                    Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
                        if (isUCEHEnabled) {
                            Timber.tag(TAG).e(
                                throwable,
                                "App crashed, executing UCEHandler's UncaughtExceptionHandler"
                            )
                            if (hasCrashedInTheLastSeconds(application)) {
                                Timber.tag(TAG).e(
                                    throwable,
                                    "App already crashed recently, not starting custom error activity because we could enter a restart loop. Are you sure that your app does not crash directly on init?"
                                )
                                if (oldHandler != null) {
                                    oldHandler.uncaughtException(thread, throwable)
                                    return@setDefaultUncaughtExceptionHandler
                                }
                            } else {
                                setLastCrashTimestamp(application, Date().time)
                                if (!isInBackground || isBackgroundMode) {
                                    val intent = Intent(application, UCEActivity::class.java)
                                    val sw = StringWriter()
                                    val pw = PrintWriter(sw)
                                    throwable.printStackTrace(pw)
                                    var stackTraceString = sw.toString()
                                    if (stackTraceString.length > MAX_STACK_TRACE_SIZE) {
                                        val disclaimer = " [stack trace too large]"
                                        stackTraceString = stackTraceString.substring(
                                            0,
                                            MAX_STACK_TRACE_SIZE - disclaimer.length
                                        ) + disclaimer
                                    }
                                    intent.putExtra(EXTRA_STACK_TRACE, stackTraceString)
                                    if (isTrackActivitiesEnabled) {
                                        val activityLogStringBuilder = StringBuilder()
                                        while (!activityLog.isEmpty()) {
                                            activityLogStringBuilder.append(activityLog.removeFirstOrNull())
                                        }
                                        intent.putExtra(
                                            EXTRA_ACTIVITY_LOG,
                                            activityLogStringBuilder.toString()
                                        )
                                    }
                                    intent.flags =
                                        Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    application.startActivity(intent)
                                } else {
                                    if (oldHandler != null) {
                                        oldHandler.uncaughtException(thread, throwable)
                                        return@setDefaultUncaughtExceptionHandler
                                    }
                                    // If it is null (should not be), we let it continue and kill the process or it will be stuck
                                }
                            }
                            val lastActivity = lastActivityCreated.get()
                            if (lastActivity != null) {
                                lastActivity.finish()
                                lastActivityCreated.clear()
                            }
                            killCurrentProcess()
                        } else if (oldHandler != null) {
                            // Pass control to old uncaught exception handler
                            oldHandler.uncaughtException(thread, throwable)
                        }
                    }
                    application.registerActivityLifecycleCallbacks(object :
                        Application.ActivityLifecycleCallbacks {
                        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
                        var currentlyStartedActivities = 0

                        override fun onActivityCreated(
                            activity: Activity,
                            savedInstanceState: Bundle?
                        ) {
                            if (activity.javaClass != UCEActivity::class.java) {
                                lastActivityCreated = WeakReference(activity)
                            }
                            if (isTrackActivitiesEnabled) {
                                activityLog.add(dateFormat.format(Date()) + ": " + activity.javaClass.simpleName + " created\n")
                            }
                        }

                        override fun onActivityStarted(activity: Activity) {
                            currentlyStartedActivities++
                            isInBackground = (currentlyStartedActivities == 0)
                        }

                        override fun onActivityResumed(activity: Activity) {
                            if (isTrackActivitiesEnabled) {
                                activityLog.add(dateFormat.format(Date()) + ": " + activity.javaClass.simpleName + " resumed\n")
                            }
                        }

                        override fun onActivityPaused(activity: Activity) {
                            if (isTrackActivitiesEnabled) {
                                activityLog.add(dateFormat.format(Date()) + ": " + activity.javaClass.simpleName + " paused\n")
                            }
                        }

                        override fun onActivityStopped(activity: Activity) {
                            currentlyStartedActivities--
                            isInBackground = (currentlyStartedActivities == 0)
                        }

                        override fun onActivitySaveInstanceState(
                            activity: Activity,
                            outState: Bundle
                        ) {
                        }

                        override fun onActivityDestroyed(activity: Activity) {
                            if (isTrackActivitiesEnabled) {
                                activityLog.add(dateFormat.format(Date()) + ": " + activity.javaClass.simpleName + " destroyed\n")
                            }
                        }
                    })
                }
                Timber.tag(TAG).i("UCEHandler has been installed.")
            } else {
                Timber.tag(TAG).e("Context can not be null")
            }
        } catch (throwable: Throwable) {
            Timber.tag(TAG).e(
                throwable,
                "UCEHandler can not be initialized. Help making it better by reporting this as a bug."
            )
        }
    }

    private fun hasCrashedInTheLastSeconds(context: Context): Boolean {
        val lastTimestamp = getLastCrashTimestamp(context)
        val currentTimestamp = Date().time
        return lastTimestamp <= currentTimestamp && currentTimestamp - lastTimestamp < 3000
    }

    @SuppressLint("ApplySharedPref")
    private fun setLastCrashTimestamp(context: Context, timestamp: Long) {
        context.getSharedPreferences(SHARED_PREFERENCES_FILE, Context.MODE_PRIVATE).edit().putLong(SHARED_PREFERENCES_FIELD_TIMESTAMP, timestamp).commit()
    }

    private fun killCurrentProcess() {
        android.os.Process.killProcess(android.os.Process.myPid())
        System.exit(10)
    }

    private fun getLastCrashTimestamp(context: Context): Long {
        return context.getSharedPreferences(SHARED_PREFERENCES_FILE, Context.MODE_PRIVATE).getLong(SHARED_PREFERENCES_FIELD_TIMESTAMP, -1)
    }

    fun closeApplication(activity: Activity) {
        activity.finish()
        killCurrentProcess()
    }

    class Builder(val context: Context) {
        var isUCEHEnabled = true
        var commaSeparatedEmailAddresses: String? = null
        var isTrackActivitiesEnabled = false
        var isBackgroundModeEnabled = true

        fun setUCEHEnabled(isUCEHEnabled: Boolean): Builder {
            this.isUCEHEnabled = isUCEHEnabled
            return this
        }

        fun setTrackActivitiesEnabled(isTrackActivitiesEnabled: Boolean): Builder {
            this.isTrackActivitiesEnabled = isTrackActivitiesEnabled
            return this
        }

        fun setBackgroundModeEnabled(isBackgroundModeEnabled: Boolean): Builder {
            this.isBackgroundModeEnabled = isBackgroundModeEnabled
            return this
        }

        fun addCommaSeparatedEmailAddresses(commaSeparatedEmailAddresses: String?): Builder {
            this.commaSeparatedEmailAddresses = commaSeparatedEmailAddresses ?: ""
            return this
        }

        fun build(): UnhandledExceptionHandler {
            return UnhandledExceptionHandler(this)
        }
    }
}

object UCEHandler{
    const val EXTRA_STACK_TRACE = "EXTRA_STACK_TRACE"
    var COMMA_SEPARATED_EMAIL_ADDRESSES: String? = null
    const val EXTRA_ACTIVITY_LOG = "EXTRA_ACTIVITY_LOG"
    const val TAG = "UCEHandler"
    const val UCE_HANDLER_PACKAGE_NAME = "com.rohitss.uceh"
    const val DEFAULT_HANDLER_PACKAGE_NAME = "com.android.internal.os"
    const val MAX_STACK_TRACE_SIZE = 131071 // 128 KB - 1
    const val MAX_ACTIVITIES_IN_LOG = 50
    const val SHARED_PREFERENCES_FILE = "uceh_preferences"
    const val SHARED_PREFERENCES_FIELD_TIMESTAMP = "last_crash_timestamp"
}