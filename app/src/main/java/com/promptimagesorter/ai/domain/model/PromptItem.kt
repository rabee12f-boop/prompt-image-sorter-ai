package com.promptimagesorter.ai.domain.model

/**
 * A single parsed "Scene" from the user's prompt file.
 *
 * The optional*Info fields are intentionally nullable and unused by Phase 1.
 * They exist now so the [com.promptimagesorter.ai.domain.prompt.PromptParser]
 * output shape does not need to change when a real Prompt Analyzer
 * (Phase 3/4) starts filling them in from [cleanVisualText].
 */
data class PromptItem(
    val sceneNumber: Int,
    val originalText: String,
    val cleanVisualText: String,
    val optionalCharacterInfo: String? = null,
    val optionalEnvironment: String? = null,
    val optionalAction: String? = null,
    val optionalObjects: List<String>? = null,
    val optionalCamera: String? = null,
    val optionalLighting: String? = null,
)
