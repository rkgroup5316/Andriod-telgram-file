package com.rkgroup.app.data.repository

import android.content.ContentResolver
import android.net.Uri
import com.rkgroup.app.data.network.TelegramApiConfig
import com.rkgroup.app.data.network.TelegramApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
    suspend fun uploadFile(
        uri: Uri,
        fileName: String,
        mimeType: String,
        contentResolver: ContentResolver
    ) = withContext(Dispatchers.IO) {
        // Create a temporary file
        val tempFile = createTempFile(uri, contentResolver)
        
        try {
            // Create MultipartBody.Part from the file
            val requestFile = tempFile.asRequestBody(mimeType.toMediaTypeOrNull())
            val filePart = MultipartBody.Part.createFormData(
                "document",
                fileName,
                requestFile
            )

            // Upload file to Telegram
            val response = telegramApiService.sendDocument(
                chatId = TelegramApiConfig.CHAT_ID,
                file = filePart,
                caption = fileName
            )

            if (!response.isSuccessful) {
                throw Exception("Upload failed: ${response.code()} ${response.message()}")
            }

        } finally {
            // Clean up temporary file
            tempFile.delete()
        }
    }

    private suspend fun createTempFile(
        uri: Uri,
        contentResolver: ContentResolver
    ): File = withContext(Dispatchers.IO) {
        val tempFile = File.createTempFile("upload", null)
        contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(tempFile).use { output ->
                input.copyTo(output)
            }
        } ?: throw Exception("Failed to read file")
        tempFile
    }
}