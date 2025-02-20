package com.rkgroup.app.ui.viewmodel

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rkgroup.app.data.repository.FileUploadRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FileUploadViewModel @Inject constructor(
    private val fileUploadRepository: FileUploadRepository
) : ViewModel() {

    sealed class UiState {
        object Idle : UiState()
        object Loading : UiState()
        data class Success(val fileName: String) : UiState()
        data class Error(val message: String) : UiState()
    }

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState

    fun uploadFile(
        uri: Uri,
        fileName: String,
        mimeType: String,
        contentResolver: ContentResolver
    ) {
        viewModelScope.launch {
            try {
                _uiState.value = UiState.Loading
                
                fileUploadRepository.uploadFile(
                    uri = uri,
                    fileName = fileName,
                    mimeType = mimeType,
                    contentResolver = contentResolver
                )
                
                _uiState.value = UiState.Success(fileName)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    fun resetState() {
        _uiState.value = UiState.Idle
    }
}