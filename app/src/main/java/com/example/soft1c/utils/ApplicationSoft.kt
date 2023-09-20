package com.example.soft1c.utils

import android.app.Application
import com.example.soft1c.BuildConfig
import timber.log.Timber

class ApplicationSoft:Application() {

    //Логи для дебага
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
            UnhandledExceptionHandler.Builder(applicationContext)
            .addCommaSeparatedEmailAddresses("qodirhcr@gmail.com")
            .build()
    }

}