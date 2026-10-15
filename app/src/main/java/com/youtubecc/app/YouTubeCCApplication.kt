package com.youtubecc.app

import android.app.Application

class YouTubeCCApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
