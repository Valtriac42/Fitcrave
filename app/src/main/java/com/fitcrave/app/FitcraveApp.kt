package com.fitcrave.app

import android.app.Application
import com.fitcrave.app.data.SupabaseProvider

class FitcraveApp : Application() {
    override fun onCreate() {
        super.onCreate()
        SupabaseProvider.init(this)
    }
}
