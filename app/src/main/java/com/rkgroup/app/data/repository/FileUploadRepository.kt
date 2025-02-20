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
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FileUploadRepository @Inject constructor(
    private val telegramApiService: TelegramApiService
) {
    sealed class UploadState {
        object Idle : UploadState()
        data class Uploading(val progress: Int) : UploadState()
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
        emit(UploadState.Uploading(0))
        
        try {
            // Create temporary file
            val tempFile = createTempFile(uri, contentResolver)
            
            try {
                // Create MultipartBody.Part
                val requestFile = tempFile.asRequestBody(mimeType.toMediaTypeOrNull())
                val filePart = MultipartBody.Part.createFormData(
                    "document",
                    fileName,
                    requestFile
                )

                // Upload file to Telegram
                emit(UploadState.Uploading(50))
                
                val response = telegramApiService.sendDocument(
                    chatId = TelegramApiConfig.CHAT_ID,
                    file = filePart,
                    caption = caption
                )

                if (response.isSuccessful && response.body() != null) {
                    emit(UploadState.Success(response.body()!!))
                } else {
                    emit(UploadState.Error("Upload failed: ${response.code()} ${response.message()}"))
                }
            } finally {
                // Clean up temporary file
                tempFile.delete()
            }
        } catch (e: Exception) {
            emit(UploadState.Error("Upload failed: ${e.message}"))
        }
    }.flowOn(Dispatchers.IO)

    private suspend fun createTempFile(
        uri: Uri,
        contentResolver: ContentResolver
    ): File = kotlinx.coroutines.withContext(Dispatchers.IO) {
        val tempFile = File.createTempFile("upload", null)
        contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(tempFile).use { output ->
                input.copyTo(output)
            }
        } ?: throw Exception("Failed to read file")
        tempFile
    }
}