package com.promptimagesorter.ai.presentation.home

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.promptimagesorter.ai.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: HomeViewModel) {
    val state by viewModel.uiState.collectAsState()

    val pickPromptFile = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let(viewModel::onPromptFilePicked)
    }
    val pickImageFolder = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        uri?.let(viewModel::onImageFolderPicked)
    }
    val pickOutputFolder = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        uri?.let(viewModel::onOutputFolderPicked)
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResourceCompat(R.string.app_name)) }) },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Button(
                onClick = { pickPromptFile.launch(arrayOf("text/plain")) },
                modifier = Modifier.fillMaxWidth(),
            ) { Text(stringResourceCompat(R.string.select_prompt_file)) }

            Button(
                onClick = { pickImageFolder.launch(null) },
                modifier = Modifier.fillMaxWidth(),
            ) { Text(stringResourceCompat(R.string.select_image_folder)) }

            Button(
                onClick = { pickOutputFolder.launch(null) },
                modifier = Modifier.fillMaxWidth(),
            ) { Text(stringResourceCompat(R.string.select_output_folder)) }

            Button(
                onClick = { /* Phase 3: wired to the matching engine */ },
                enabled = state.canStart && !state.isLoading,
                modifier = Modifier.fillMaxWidth(),
            ) { Text(stringResourceCompat(R.string.start_matching)) }

            if (state.isLoading) {
                Column(horizontalAlignment = Alignment.Start) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    Text(state.loadingMessage ?: "Loading…")
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(stringResourceCompat(R.string.project_summary), style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
                    Text(stringFormatCompat(R.string.prompts_count, state.prompts.size))
                    Text(stringFormatCompat(R.string.images_count, state.images.size))
                }
            }

            if (state.warnings.isNotEmpty()) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Warnings", style = androidx.compose.material3.MaterialTheme.typography.titleSmall)
                        state.warnings.forEach { Text("• $it") }
                    }
                }
            }
        }
    }

    state.errorMessage?.let { message ->
        AlertDialog(
            onDismissRequest = viewModel::clearError,
            confirmButton = { TextButton(onClick = viewModel::clearError) { Text("OK") } },
            title = { Text("Error") },
            text = { Text(message) },
        )
    }
}

// Small helpers kept local to avoid pulling in extra Compose resource APIs for Phase 1 scaffolding.
@Composable
private fun stringResourceCompat(id: Int): String = androidx.compose.ui.res.stringResource(id)

@Composable
private fun stringFormatCompat(id: Int, value: Int): String =
    androidx.compose.ui.res.stringResource(id, value)
