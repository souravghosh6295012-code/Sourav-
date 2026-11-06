package com.example.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

object DownloadHelper {

    suspend fun saveMediaToGallery(
        context: Context,
        uriString: String,
        title: String,
        isVideo: Boolean
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val resolver = context.contentResolver
            val sanitizedTitle = title.replace("[^a-zA-Z0-9_-]".toRegex(), "_").ifEmpty { "bright_digital" }
            val fileName = if (isVideo) "${sanitizedTitle}_${System.currentTimeMillis()}.mp4" else "${sanitizedTitle}_${System.currentTimeMillis()}.jpg"
            val mimeType = if (isVideo) "video/mp4" else "image/jpeg"

            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val relativePath = if (isVideo) {
                        "${Environment.DIRECTORY_MOVIES}/BrightDigital"
                    } else {
                        "${Environment.DIRECTORY_PICTURES}/BrightDigital"
                    }
                    put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath)
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }
            }

            val collection = if (isVideo) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                } else {
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                }
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                } else {
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                }
            }

            val itemUri = resolver.insert(collection, contentValues) ?: return@withContext false

            var inputStream: InputStream? = null
            if (uriString.startsWith("http://") || uriString.startsWith("https://")) {
                val url = URL(uriString)
                val connection = (url.openConnection() as HttpURLConnection).apply {
                    connectTimeout = 15000
                    readTimeout = 20000
                    connect()
                }
                inputStream = connection.inputStream
            } else if (uriString.startsWith("content://")) {
                inputStream = resolver.openInputStream(Uri.parse(uriString))
            } else {
                val file = File(uriString)
                if (file.exists()) {
                    inputStream = FileInputStream(file)
                }
            }

            if (inputStream == null) {
                return@withContext false
            }

            resolver.openOutputStream(itemUri)?.use { out ->
                inputStream.use { input ->
                    input.copyTo(out)
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                resolver.update(itemUri, contentValues, null, null)
            }

            withContext(Dispatchers.Main) {
                val destination = if (isVideo) "Movies/BrightDigital" else "Pictures/BrightDigital"
                Toast.makeText(context, "Saved to $destination!", Toast.LENGTH_LONG).show()
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Download failed: ${e.localizedMessage ?: "Unknown error"}", Toast.LENGTH_SHORT).show()
            }
            false
        }
    }

    fun shareMedia(context: Context, title: String, uriString: String, isVideo: Boolean) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Bright Digital - $title")
            putExtra(
                Intent.EXTRA_TEXT,
                "Check out \"$title\" from Bright Digital Photography & Videography:\n$uriString\n\nBook your shoot: +91 83274 29438"
            )
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share via").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }

    fun callNow(context: Context, phoneNumber: String = "+918327429438") {
        try {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$phoneNumber")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Unable to dial: $phoneNumber", Toast.LENGTH_SHORT).show()
        }
    }

    fun openWhatsApp(context: Context, message: String = "Hello Bright Digital, I would like to inquire about photography and videography booking.") {
        try {
            val url = "https://api.whatsapp.com/send?phone=918327429438&text=" + Uri.encode(message)
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "WhatsApp not available, calling instead...", Toast.LENGTH_SHORT).show()
            callNow(context)
        }
    }
}
