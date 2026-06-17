package com.someoddguy.snapshare.globalcontext

import android.app.Application
import android.content.Context

class GlobalContext : Application() {

    companion object {
        // This makes the context available globally
        lateinit var appContext: Context
            private set
    }

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
    }
}