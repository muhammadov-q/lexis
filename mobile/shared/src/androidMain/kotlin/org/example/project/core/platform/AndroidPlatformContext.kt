package org.example.project.core.platform

import android.content.Context

object AndroidPlatformContext {
    @Volatile
    private var applicationContext: Context? = null

    fun initialize(context: Context) {
        applicationContext = context.applicationContext
    }

    fun requireContext(): Context =
        checkNotNull(applicationContext) {
            "AndroidPlatformContext is not initialized. Call initialize(context) in MainActivity."
        }
}

