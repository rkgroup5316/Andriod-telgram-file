package com.rkgroup.app.ui

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rkgroup.app.data.repository.FileUploadRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val fileUploadRepository: FileUploadRepository
) : ViewModel() {

    companion object {
        private const val MAX_FILE_SIZE = 2L * 1024 * 1024 * 1024 // 2GB max file size
        private val ALLOWED_MIME_TYPES = setOf(
            "image/", "video/", "audio/",
            "application/pdf", "application/msword",
            "application/vnd.openxmlformats-officedocument",
            "text/plain"
        )
    }

    private val _uiState = MutableStateFlow<UiState>(UiState.Initial)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun processFilesInBackground(uris: List<Uri>, contentResolver: ContentResolver) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val validFiles = validateFiles(uris, contentResolver)
                if (validFiles.isEmpty()) {
                    _uiState.value = UiState.Error("No valid files selected")
                    return@launch
                }

                validFiles.forEach { fileInfo ->
                    processFile(fileInfo)
                }
                
                _uiState.value = UiState.Success
            } catch (e: Exception) {
                _uiState.value = UiState.Error(
                    when (e) {
                        is IOException -> "Error accessing files: ${e.message}"
                        is SecurityException -> "Permission denied: ${e.message}"
                        else -> "Unexpected error: ${e.message}"
                    }
                )
            }
        }
    }

    private suspend fun validateFiles(
        uris: List<Uri>, 
        contentResolver: ContentResolver
    ): List<FileInfo> = withContext(Dispatchers.IO) {
        uris.mapNotNull { uri ->
            try {
                val mimeType = contentResolver.getType(uri)
                val size = contentResolver.openFileDescriptor(uri, "r")?.statSize ?: 0
                val name = getFileName(uri, contentResolver)

                when {
                    mimeType == null -> {
                        null // Skip files without mime type
                    }
                    !isValidMimeType(mimeType) -> {
                        null // Skip unsupported file types
                    }
                    size > MAX_FILE_SIZE -> {
                        null // Skip files larger than 2GB
                    }
                    else -> FileInfo(
                        uri = uri,
                        name = name ?: "Unknown",
                        mimeType = mimeType,
                        size = size
                    )
                }
            } catch (e: Exception) {
                null // Skip problematic files
            }
        }
    }

    private fun isValidMimeType(mimeType: String): Boolean {
        return ALLOWED_MIME_TYPES.any { allowed -> 
            mimeType.startsWith(allowed) 
        }
    }

    private fun getFileName(uri: Uri, contentResolver: ContentResolver): String? {
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    return cursor.getString(nameIndex)
                }
            }
        }
        return uri.lastPathSegment
    }

    private suspend fun processFile(fileInfo: FileInfo) {
        try {
            fileUploadRepository.uploadFile(
                uri = fileInfo.uri,
                fileName = fileInfo.name,
                mimeType = fileInfo.mimeType,
                contentResolver = contentResolver
            )
        } catch (e: Exception) {
            throw IOException("Failed to upload ${fileInfo.name}: ${e.message}")
        }
    }

    private suspend fun simulateProcessing(fileInfo: FileInfo) {
        // This is temporary and will be replaced with actual upload logic
        withContext(Dispatchers.IO) {
            // Simulate some work
            kotlinx.coroutines.delay(1000)
        }
    }

    data class FileInfo(
        val uri: Uri,
        val name: String,
        val mimeType: String,
        val size: Long
    )

    sealed class UiState {
        object Initial : UiState()
        object Success : UiState()
        data class Error(val message: String) : UiState()
    }
}