package com.promptimagesorter.ai.domain.storage

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * All filesystem access goes through this class, using the Storage Access
 * Framework — no legacy storage permissions are requested anywhere in the
 * app (requirement #6).
 *
 * Persisting the read/write URI permission means the user never has to
 * re-pick the same prompt file / image folder / output folder on the next
 * app launch, as long as they don't revoke access from Android settings.
 */
class SafFileRepository(private val context: Context) {

    fun persistPermission(uri: Uri, forTree: Boolean) {
        val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
            if (forTree) Intent.FLAG_GRANT_WRITE_URI_PERMISSION else 0
        context.contentResolver.takePersistableUriPermission(uri, flags)
    }

    suspend fun readTextFile(uri: Uri): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                stream.bufferedReader(Charsets.UTF_8).readText()
            } ?: throw IllegalStateException("Could not open prompt file (file unavailable).")
        }
    }

    fun documentFileFromTreeUri(treeUri: Uri): DocumentFile? =
        DocumentFile.fromTreeUri(context, treeUri)

    fun documentFileFromSingleUri(uri: Uri): DocumentFile? =
        DocumentFile.fromSingleUri(context, uri)

    /** Creates (or reuses) a subfolder inside [parent]. Never touches original source folders. */
    fun getOrCreateSubfolder(parent: DocumentFile, name: String): DocumentFile? =
        parent.findFile(name)?.takeIf { it.isDirectory } ?: parent.createDirectory(name)
}
