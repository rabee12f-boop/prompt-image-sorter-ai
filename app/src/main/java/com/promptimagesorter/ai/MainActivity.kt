package com.promptimagesorter.ai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.promptimagesorter.ai.presentation.home.HomeScreen
import com.promptimagesorter.ai.presentation.home.HomeViewModel
import com.promptimagesorter.ai.presentation.home.HomeViewModelFactory
import com.promptimagesorter.ai.presentation.theme.PromptImageSorterTheme

class MainActivity : ComponentActivity() {

    private val homeViewModel: HomeViewModel by viewModels {
        HomeViewModelFactory((application as PromptImageSorterApp).container)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PromptImageSorterTheme {
                HomeScreen(viewModel = homeViewModel)
            }
        }
    }
}
