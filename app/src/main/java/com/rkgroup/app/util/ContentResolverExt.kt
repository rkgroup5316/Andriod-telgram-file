package com.rkgroup.app.util

import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns

/**
 * Query the file name from a content URI
 */
fun ContentResolver.queryFileName(uri: Uri): String? {
    val cursor = query(uri, null, null, null, null)
    return cursor?.use {
        if (it.moveToFirst()) {
            val displayNameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (displayNameIndex != -1) {
                it.getString(displayNameIndex)
            } else null
        } else null
    }
}

/**
 * Get file size from a content URI
 */
fun ContentResolver.getFileSize(uri: Uri): Long? {
    val cursor = query(uri, null, null, null, null)
    return cursor?.use {
        if (it.moveToFirst()) {
            val sizeIndex = it.getColumnIndex(OpenableColumns.SIZE)
            if (sizeIndex != -1) {
                it.getLong(sizeIndex)
            } else null
        } else null
    }
}