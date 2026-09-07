package com.promptimagesorter.ai.domain.model

/**
 * A single image discovered inside the user-selected source folder.
 *
 * [uriString] is the persisted content:// URI (via SAF) — never a file path,
 * since the app must not assume direct filesystem access. Filename-derived
 * fields (extension) are metadata only; they are never used for scene
 * matching (see project requirement #3).
 */
data class ImageItem(
    val uriString: String,
    val displayName: String,
    val extension: String,
    val sizeBytes: Long,
    val widthPx: Int? = null,
    val heightPx: Int? = null,
    val lastModifiedEpochMs: Long? = null,
) {
    /** Stable identity for dedup/caching, independent of display name. */
    val id: String get() = uriString
}
