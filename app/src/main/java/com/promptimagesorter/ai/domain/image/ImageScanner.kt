package com.promptimagesorter.ai.domain.image

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.promptimagesorter.ai.domain.model.ImageItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Lists images inside a user-selected SAF tree.
 *
 * Phase 1 only collects lightweight metadata (name, extension, size) — no
 * bitmap decoding happens here, so scanning 500+ images stays fast and does
 * not load anything into memory (requirement #23). Bitmap/thumbnail
 * decoding for embeddings is added in Phase 2 with proper downsampling.
 */
class ImageScanner(private val context: Context) {

    private val supportedExtensions = setOf("jpg", "jpeg", "png", "webp")

    suspend fun scan(treeUri: Uri): List<ImageItem> = withContext(Dispatchers.IO) {
        val root = DocumentFile.fromTreeUri(context, treeUri) ?: return@withContext emptyList()
        root.listFiles()
            .asSequence()
            .filter { it.isFile }
            .mapNotNull { toImageItemOrNull(it) }
            .toList()
    }

    private fun toImageItemOrNull(doc: DocumentFile): ImageItem? {
        val name = doc.name ?: return null
        val extension = name.substringAfterLast('.', missingDelimiterValue = "").lowercase()
        if (extension !in supportedExtensions) return null

        return ImageItem(
            uriString = doc.uri.toString(),
            displayName = name,
            extension = extension,
            sizeBytes = doc.length(),
            lastModifiedEpochMs = doc.lastModified().takeIf { it > 0 },
        )
    }
}
