package com.promptimagesorter.ai

import android.content.Context
import com.promptimagesorter.ai.domain.image.ImageScanner
import com.promptimagesorter.ai.domain.prompt.PromptParser
import com.promptimagesorter.ai.domain.storage.SafFileRepository

/**
 * Very small hand-rolled dependency container.
 *
 * Phase 1 only needs three collaborators. As AI matching, caching and
 * project history are added in later phases, this is the single place new
 * repositories/use-cases get wired up.
 */
class AppContainer(context: Context) {
    val safFileRepository = SafFileRepository(context.applicationContext)
    val promptParser = PromptParser()
    val imageScanner = ImageScanner(context.applicationContext)
}
