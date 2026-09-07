package com.promptimagesorter.ai.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.promptimagesorter.ai.AppContainer

class HomeViewModelFactory(private val container: AppContainer) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return HomeViewModel(
            safFileRepository = container.safFileRepository,
            promptParser = container.promptParser,
            imageScanner = container.imageScanner,
        ) as T
    }
}
