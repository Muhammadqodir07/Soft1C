package com.example.soft1c.utils.UCE

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.TypedValue
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.example.soft1c.R
import com.example.soft1c.databinding.ActivityUceactivityBinding
import com.example.soft1c.utils.UCEHandler
import com.example.soft1c.utils.UnhandledExceptionHandler
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


class UCEActivity : AppCompatActivity() {
    private lateinit var txtFile: File
    private lateinit var binding: ActivityUceactivityBinding
    private var strCurrentErrorLog: String? = null
    private val uce = UnhandledExceptionHandler.Builder(this).build()

    @SuppressLint("PrivateResource")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUceactivityBinding.inflate(layoutInflater)

        setContentView(binding.root)

        findViewById<View>(R.id.button_close_app).setOnClickListener {
            uce.closeApplication(this@UCEActivity)
        }

        findViewById<View>(R.id.button_copy_error_log).setOnClickListener {
            copyErrorToClipboard()
        }

        findViewById<View>(R.id.button_share_error_log).setOnClickListener {
            shareErrorLog()
        }

        findViewById<View>(R.id.button_email_error_log).setOnClickListener {
            emailErrorLog()
        }

        findViewById<View>(R.id.button_view_error_log).setOnClickListener {
            val dialog = AlertDialog.Builder(this@UCEActivity)
                .setTitle("Error Log")
                .setMessage(getAllErrorDetailsFromIntent(this@UCEActivity, intent))
                .setPositiveButton(getString(R.string.copy_error_log)) { dialog, which ->
                    copyErrorToClipboard()
                    dialog.dismiss()
                }
                .setNeutralButton(getString(R.string.text_close)) { dialog, which ->
                    dialog.dismiss()
                }
                .show()

            val textView = dialog.findViewById<TextView>(android.R.id.message)
            if (textView != null) {
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
            }
        }
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onResume() {
        super.onResume()
    }

    private fun getApplicationName(context: Context): String {
        val applicationInfo = context.applicationInfo
        val stringId = applicationInfo.labelRes
        return if (stringId == 0) applicationInfo.nonLocalizedLabel.toString() else context.getString(stringId)
    }

    private fun getVersionName(context: Context): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName
        } catch (e: Exception) {
            "Unknown"
        }
    }

    private fun getActivityLogFromIntent(intent: Intent): String? {
        return intent.getStringExtra(UCEHandler.EXTRA_ACTIVITY_LOG)
    }

    private fun getStackTraceFromIntent(intent: Intent): String? {
        return intent.getStringExtra(UCEHandler.EXTRA_STACK_TRACE)
    }

    private fun emailErrorLog() {
        saveErrorLogToFile(false)
        val errorLog = getAllErrorDetailsFromIntent(this@UCEActivity, intent)
        val emailAddressArray = UCEHandler.COMMA_SEPARATED_EMAIL_ADDRESSES?.trim()?.split("\\s*,\\s*".toRegex())?.toTypedArray()
        val emailIntent = Intent(Intent.ACTION_SEND)
        emailIntent.type = "plain/text"
        emailIntent.putExtra(Intent.EXTRA_EMAIL, emailAddressArray)
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, getApplicationName(this@UCEActivity) + " Application Crash Error Log")
        emailIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.email_welcome_note) + errorLog)
        if (txtFile.exists()) {
            val filePath = FileProvider.getUriForFile(this, applicationContext.packageName + ".provider", txtFile)
            emailIntent.putExtra(Intent.EXTRA_STREAM, filePath)
        }
        emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startActivity(Intent.createChooser(emailIntent, "Email Error Log"))
    }

    private fun saveErrorLogToFile(isShowToast: Boolean) {
        val isSDPresent = Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
        if (isSDPresent && isExternalStorageWritable()) {
            val currentDate = Date()
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINESE)
            var strCurrentDate = dateFormat.format(currentDate)
            strCurrentDate = strCurrentDate.replace(" ", "_")
            val errorLogFileName = getApplicationName(this@UCEActivity) + "_Error-Log_" + strCurrentDate
            val errorLog = getAllErrorDetailsFromIntent(this@UCEActivity, intent)
            val fullPath = Environment.getExternalStorageDirectory().toString() + "/AppErrorLogs_UCEH/"
            var outputStream: FileOutputStream? = null
            try {
                val file = File(fullPath)
                file.mkdir()
                txtFile = File(fullPath + errorLogFileName + ".txt")
                txtFile.createNewFile()
                outputStream = FileOutputStream(txtFile)
                outputStream.write(errorLog.toByteArray())
                outputStream.close()
                if (txtFile.exists() && isShowToast) {
                    Toast.makeText(this, "File Saved Successfully", Toast.LENGTH_SHORT).show()
                }
            } catch (e: IOException) {
                Timber.tag("REQUIRED")
                    .e("This app does not have write storage permission to save log file.")
                if (isShowToast) {
                    Toast.makeText(this, "Storage Permission Not Found", Toast.LENGTH_SHORT).show()
                }
                e.printStackTrace()
            } finally {
                try {
                    outputStream?.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun shareErrorLog() {
        val errorLog = getAllErrorDetailsFromIntent(this@UCEActivity, intent)
        val share = Intent(Intent.ACTION_SEND)
        share.type = "text/plain"
        share.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET)
        share.putExtra(Intent.EXTRA_SUBJECT, "Application Crash Error Log")
        share.putExtra(Intent.EXTRA_TEXT, errorLog)
        startActivity(Intent.createChooser(share, "Share Error Log"))
    }

    private fun copyErrorToClipboard() {
        val errorInformation = getAllErrorDetailsFromIntent(this@UCEActivity, intent)
        val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager?
        if (clipboard != null) {
            val clip = ClipData.newPlainText("View Error Log", errorInformation)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this@UCEActivity, "Error Log Copied", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getAllErrorDetailsFromIntent(context: Context, intent: Intent): String {
        if (strCurrentErrorLog.isNullOrEmpty()) {
            val LINE_SEPARATOR = "\n"
            val errorReport = StringBuilder()
            errorReport.append("***** UCE HANDLER Library ")
            errorReport.append("\n***** by Rohit Surwase \n")
            errorReport.append("\n***** DEVICE INFO \n")
            errorReport.append("Brand: ${Build.BRAND}")
            errorReport.append(LINE_SEPARATOR)
            errorReport.append("Device: ${Build.DEVICE}")
            errorReport.append(LINE_SEPARATOR)
            errorReport.append("Model: ${Build.MODEL}")
            errorReport.append(LINE_SEPARATOR)
            errorReport.append("Manufacturer: ${Build.MANUFACTURER}")
            errorReport.append(LINE_SEPARATOR)
            errorReport.append("Product: ${Build.PRODUCT}")
            errorReport.append(LINE_SEPARATOR)
            errorReport.append("SDK: ${Build.VERSION.SDK}")
            errorReport.append(LINE_SEPARATOR)
            errorReport.append("Release: ${Build.VERSION.RELEASE}")
            errorReport.append(LINE_SEPARATOR)
            errorReport.append("\n***** APP INFO \n")
            val versionName = getVersionName(context)
            errorReport.append("Version: $versionName")
            errorReport.append(LINE_SEPARATOR)
            val currentDate = Date()
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINESE)
            val firstInstallTime = getFirstInstallTimeAsString(context, dateFormat)
            if (!firstInstallTime.isNullOrEmpty()) {
                errorReport.append("Installed On: $firstInstallTime")
                errorReport.append(LINE_SEPARATOR)
            }
            val lastUpdateTime = getLastUpdateTimeAsString(context, dateFormat)
            if (!lastUpdateTime.isNullOrEmpty()) {
                errorReport.append("Updated On: $lastUpdateTime")
                errorReport.append(LINE_SEPARATOR)
            }
            errorReport.append("Current Date: ${dateFormat.format(currentDate)}")
            errorReport.append(LINE_SEPARATOR)
            errorReport.append("\n***** ERROR LOG \n")
            errorReport.append(getStackTraceFromIntent(intent))
            errorReport.append(LINE_SEPARATOR)
            val activityLog = getActivityLogFromIntent(intent)
            errorReport.append(LINE_SEPARATOR)
            if (activityLog != null) {
                errorReport.append("\n***** USER ACTIVITIES \n")
                errorReport.append("User Activities: $activityLog")
                errorReport.append(LINE_SEPARATOR)
            }
            errorReport.append("\n***** END OF LOG *****\n")
            strCurrentErrorLog = errorReport.toString()
            return strCurrentErrorLog!!
        } else {
            return strCurrentErrorLog!!
        }
    }

    private fun getFirstInstallTimeAsString(context: Context, dateFormat: DateFormat): String? {
        val firstInstallTime: Long
        return try {
            firstInstallTime = context.packageManager.getPackageInfo(context.packageName, 0).firstInstallTime
            dateFormat.format(Date(firstInstallTime))
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
    }

    private fun getLastUpdateTimeAsString(context: Context, dateFormat: DateFormat): String? {
        val lastUpdateTime: Long
        return try {
            lastUpdateTime = context.packageManager.getPackageInfo(context.packageName, 0).lastUpdateTime
            dateFormat.format(Date(lastUpdateTime))
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
    }

    private fun isExternalStorageWritable(): Boolean {
        val state = Environment.getExternalStorageState()
        return Environment.MEDIA_MOUNTED == state
    }
}