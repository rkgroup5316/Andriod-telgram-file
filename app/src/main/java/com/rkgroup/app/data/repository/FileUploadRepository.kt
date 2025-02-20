package com.rkgroup.app.data.repository

import android.content.ContentResolver
import android.net.Uri
import com.rkgroup.app.data.network.TelegramApiConfig
import com.rkgroup.app.data.network.TelegramApiService
import com.rkgroup.app.data.network.TelegramResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FileUploadRepository @Inject constructor(
    private val telegramApiService: TelegramApiService
) {
    companion object {
        private const val MAX_RETRIES = 3
        private const val BUFFER_SIZE = 8192 // 8KB buffer for file operations
    }

    sealed class UploadState {
        object Idle : UploadState()
        data class Preparing(val progress: Int) : UploadState()
        data class Uploading(
            val progress: Int,
            val bytesUploaded: Long,
            val totalBytes: Long
        ) : UploadState()
        data class Success(val response: TelegramResponse) : UploadState()
        data class Error(val message: String) : UploadState()
    }

    fun uploadFile(
        uri: Uri,
        fileName: String,
        mimeType: String,
        contentResolver: ContentResolver,
        caption: String? = null
    ): Flow<UploadState> = flow {
        emit(UploadState.Preparing(0))
        
        try {
            val fileSize = getFileSize(uri, contentResolver)
            if (fileSize <= 0) {
                emit(UploadState.Error("Invalid file size"))
                return@flow
            }

            // Create temporary file with progress tracking
            val tempFile = createTempFile(uri, contentResolver, fileSize) { progress ->
                emit(UploadState.Preparing(progress))
            }
            
            try {
                uploadWithRetry(tempFile, fileName, mimeType, caption, fileSize)
            } finally {
                tempFile.delete()
            }
        } catch (e: Exception) {
            emit(UploadState.Error(when (e) {
                is IOException -> "Network error: ${e.message}"
                else -> "Upload failed: ${e.message}"
            }))
        }
    }.flowOn(Dispatchers.IO)

    private suspend fun Flow<UploadState>.uploadWithRetry(
        file: File,
        fileName: String,
        mimeType: String,
        caption: String?,
        fileSize: Long
    ) {
        var retryCount = 0
        var lastError: Exception? = null

        while (retryCount < MAX_RETRIES) {
            try {
                val requestFile = file.asRequestBody(mimeType.toMediaTypeOrNull())
                val filePart = MultipartBody.Part.createFormData("document", fileName, requestFile)

                emit(UploadState.Uploading(0, 0, fileSize))
                
                val response = telegramApiService.sendDocument(
                    chatId = TelegramApiConfig.CHAT_ID,
                    file = filePart,
                    caption = caption
                )

                if (response.isSuccessful && response.body() != null) {
                    emit(UploadState.Success(response.body()!!))
                    return
                } else {
                    throw IOException("Upload failed: ${response.code()} ${response.message()}")
                }
            } catch (e: Exception) {
                lastError = e
                retryCount++
                if (retryCount < MAX_RETRIES) {
                    // Exponential backoff delay
                    kotlinx.coroutines.delay(1000L * (1 shl retryCount))
                }
            }
        }

        emit(UploadState.Error("Upload failed after $MAX_RETRIES attempts: ${lastError?.message}"))
    }

    private suspend fun getFileSize(
        uri: Uri,
        contentResolver: ContentResolver
    ): Long = withContext(Dispatchers.IO) {
        contentResolver.openFileDescriptor(uri, "r")?.use { 
            it.statSize
        } ?: -1
    }

    private suspend fun createTempFile(
        uri: Uri,
        contentResolver: ContentResolver,
        totalSize: Long,
        onProgress: suspend (Int) -> Unit
    ): File = withContext(Dispatchers.IO) {
        val tempFile = File.createTempFile("upload", null)
        var bytesWritten = 0L

        contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(tempFile).use { output ->
                val buffer = ByteArray(BUFFER_SIZE)
                var bytes: Int
                
                while (input.read(buffer).also { bytes = it } != -1) {
                    output.write(buffer, 0, bytes)
                    bytesWritten += bytes
                    val progress = ((bytesWritten.toFloat() / totalSize) * 100).toInt()
                    onProgress(progress)
                }
            }
        } ?: throw IOException("Failed to read file")

        tempFile
    }
}
