package com.rkgroup.app.ui

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor() : ViewModel() {

    // UI State
    private val _uiState = MutableStateFlow<UiState>(UiState.Initial)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    // Selected Files
    private val _selectedFiles = MutableLiveData<List<FileItem>>(emptyList())
    val selectedFiles: LiveData<List<FileItem>> = _selectedFiles

    // Upload Progress
    private val _uploadProgress = MutableStateFlow<Map<String, Int>>(emptyMap())
    val uploadProgress: StateFlow<Map<String, Int>> = _uploadProgress.asStateFlow()

    /**
     * Add files to the upload queue
     */
    fun addFiles(uris: List<Uri>, contentResolver: android.content.ContentResolver) {
        viewModelScope.launch {
            try {
                val currentFiles = _selectedFiles.value.orEmpty().toMutableList()
                val newFiles = uris.mapNotNull { uri ->
                    contentResolver.queryFileName(uri)?.let { fileName ->
                        FileItem(
                            id = System.currentTimeMillis().toString(),
                            uri = uri,
                            name = fileName,
                            size = contentResolver.getFileSize(uri) ?: 0L
                        )
                    }
                }
                currentFiles.addAll(newFiles)
                _selectedFiles.value = currentFiles
                _uiState.value = if (currentFiles.isEmpty()) {
                    UiState.Initial
                } else {
                    UiState.FilesSelected(currentFiles.size)
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Error adding files: ${e.message}")
            }
        }
    }

    /**
     * Remove file from the upload queue
     */
    fun removeFile(fileItem: FileItem) {
        val currentFiles = _selectedFiles.value.orEmpty().toMutableList()
        currentFiles.remove(fileItem)
        _selectedFiles.value = currentFiles
        
        // Update progress map
        val currentProgress = _uploadProgress.value.toMutableMap()
        currentProgress.remove(fileItem.id)
        _uploadProgress.value = currentProgress

        if (currentFiles.isEmpty()) {
            _uiState.value = UiState.Initial
        }
    }

    /**
     * Clear all selected files
     */
    fun clearFiles() {
        _selectedFiles.value = emptyList()
        _uploadProgress.value = emptyMap()
        _uiState.value = UiState.Initial
    }

    /**
     * Update upload progress for a specific file
     */
    fun updateProgress(fileId: String, progress: Int) {
        val currentProgress = _uploadProgress.value.toMutableMap()
        currentProgress[fileId] = progress
        _uploadProgress.value = currentProgress
    }

    sealed class UiState {
        object Initial : UiState()
        data class FilesSelected(val count: Int) : UiState()
        data class Uploading(val fileId: String) : UiState()
        data class Error(val message: String) : UiState()
        object Success : UiState()
    }

    data class FileItem(
        val id: String,
        val uri: Uri,
        val name: String,
        val size: Long
    )
}