package com.rkgroup.app.data.network

import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface TelegramApiService {
    @Multipart
    @POST("sendDocument")
    suspend fun sendDocument(
        @Query("chat_id") chatId: String,
        @Part file: MultipartBody.Part,
        @Query("caption") caption: String? = null,
        @Query("disable_notification") disableNotification: Boolean? = null
    ): Response<TelegramResponse>
}

data class TelegramResponse(
    val ok: Boolean,
    val result: DocumentResult?
)

data class DocumentResult(
    val message_id: Long,
    val document: Document?
)

data class Document(
    val file_id: String,
    val file_unique_id: String,
    val file_name: String?,
    val mime_type: String?,
    val file_size: Long?
)