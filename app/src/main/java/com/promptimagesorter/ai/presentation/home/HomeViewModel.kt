package com.promptimagesorter.ai.presentation.home

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.promptimagesorter.ai.domain.image.ImageScanner
import com.promptimagesorter.ai.domain.model.ImageItem
import com.promptimagesorter.ai.domain.model.PromptItem
import com.promptimagesorter.ai.domain.prompt.PromptParser
import com.promptimagesorter.ai.domain.storage.SafFileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val promptFileUri: Uri? = null,
    val imageFolderUri: Uri? = null,
    val outputFolderUri: Uri? = null,
    val prompts: List<PromptItem> = emptyList(),
    val images: List<ImageItem> = emptyList(),
    val isLoading: Boolean = false,
    val loadingMessage: String? = null,
    val warnings: List<String> = emptyList(),
    val errorMessage: String? = null,
) {
    val canStart: Boolean
        get() = promptFileUri != null && imageFolderUri != null && outputFolderUri != null &&
            prompts.isNotEmpty() && images.isNotEmpty()
}

class HomeViewModel(
    private val safFileRepository: SafFileRepository,
    private val promptParser: PromptParser,
    private val imageScanner: ImageScanner,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun onPromptFilePicked(uri: Uri) {
        safFileRepository.persistPermission(uri, forTree = false)
        _uiState.update { it.copy(promptFileUri = uri, isLoading = true, loadingMessage = "Reading prompts…", errorMessage = null) }

        viewModelScope.launch {
            val textResult = safFileRepository.readTextFile(uri)
            textResult.fold(
                onSuccess = { text ->
                    val parsed = promptParser.parse(text)
                    _uiState.update {
                        it.copy(
                            prompts = parsed.items,
                            warnings = parsed.warnings,
                            isLoading = false,
                            loadingMessage = null,
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(isLoading = false, loadingMessage = null, errorMessage = e.message ?: "Failed to read prompt file.")
                    }
                },
            )
        }
    }

    fun onImageFolderPicked(uri: Uri) {
        safFileRepository.persistPermission(uri, forTree = true)
        _uiState.update { it.copy(imageFolderUri = uri, isLoading = true, loadingMessage = "Scanning images…", errorMessage = null) }

        viewModelScope.launch {
            runCatching { imageScanner.scan(uri) }
                .onSuccess { images ->
                    _uiState.update { it.copy(images = images, isLoading = false, loadingMessage = null) }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(isLoading = false, loadingMessage = null, errorMessage = e.message ?: "Failed to scan image folder.")
                    }
                }
        }
    }

    fun onOutputFolderPicked(uri: Uri) {
        safFileRepository.persistPermission(uri, forTree = true)
        _uiState.update { it.copy(outputFolderUri = uri) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
