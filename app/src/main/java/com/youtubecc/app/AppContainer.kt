package com.youtubecc.app

import android.content.Context
import com.youtubecc.app.data.PreferencesManager
import com.youtubecc.app.data.YouTubeRepository

class AppContainer(context: Context) {
    val preferences = PreferencesManager(context.applicationContext)
    val repository = YouTubeRepository(preferences)
}
