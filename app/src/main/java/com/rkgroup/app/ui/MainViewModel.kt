package com.rkgroup.app.ui

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow<UiState>(UiState.Initial)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun processFilesInBackground(uris: List<Uri>, contentResolver: ContentResolver) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                uris.forEach { uri ->
                    // Process each file in background
                    processFile(uri, contentResolver)
                }
                _uiState.value = UiState.Success
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    private suspend fun processFile(uri: Uri, contentResolver: ContentResolver) {
        // TODO: Implement actual file processing logic
        // This will be implemented when we add the Telegram API integration
    }

    sealed class UiState {
        object Initial : UiState()
        object Success : UiState()
        data class Error(val message: String) : UiState()
    }
}