package com.promptimagesorter.ai.domain.model

/**
 * Confidence bands for a match. Thresholds are configurable (see
 * project requirement #13) and therefore live in Settings, not here —
 * this enum is just the label the UI renders.
 */
enum class MatchStatus {
    EXCELLENT,
    GOOD,
    REVIEW_RECOMMENDED,
    NEEDS_REVIEW,
}

/**
 * Result of matching one [PromptItem] to a candidate [ImageItem].
 *
 * NOTE: Not used until Phase 3 (Matching Engine). Defined now so the
 * project structure and Review UI can be scaffolded ahead of the AI work
 * without churn later.
 */
data class MatchResult(
    val sceneNumber: Int,
    val imageId: String?,
    val similarityScore: Float,
    val status: MatchStatus,
    val alternativeImageIds: List<Pair<String, Float>> = emptyList(),
)

/** Aggregate outcome of a full sorting run, shown on the Results screen. */
data class SortingReport(
    val totalScenes: Int,
    val totalImages: Int,
    val matchedCount: Int,
    val needsReviewCount: Int,
    val unusedCount: Int,
    val averageSimilarity: Float,
)
