package com.example.art.utils

import android.app.Application
import android.content.Context

class AppContextProvider : Application() {
    companion object {
        lateinit var instance: Context
            private set

        fun getContext(): Context {
            return instance
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = applicationContext
    }
}

