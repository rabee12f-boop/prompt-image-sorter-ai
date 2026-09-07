package com.promptimagesorter.ai

import android.app.Application

/**
 * Application entry point.
 *
 * Phase 1 uses a very small manual service locator instead of a DI framework
 * (Hilt/Koin) to keep the project buildable with zero annotation-processing
 * setup. This can be swapped for Hilt in a later phase without changing the
 * domain/presentation layers, since everything already goes through
 * [AppContainer].
 */
class PromptImageSorterApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(applicationContext)
    }
}
