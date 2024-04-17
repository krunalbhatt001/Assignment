package com.assignment.loader

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.util.LruCache
import android.widget.Toast
import androidx.core.graphics.drawable.toBitmap
import com.assignment.exception.BitmapDecodeException
import com.assignment.exception.UrlNotImageException
import com.assignment.app.MyApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

/**
 * Utility class for loading and caching images from URLs.
 * This class provides methods to efficiently manage memory and cache images
 * to improve performance and reduce network requests.
 */

class ImageLoader {

    /** Maximum available memory for the application in kilobytes. */
    private val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()

    /** Name of the directory where cached images will be stored. */
    private val cacheDir = "imageCache"

    /** LRU (Least Recently Used) cache to store images in memory. */
    private val memoryCache = LruCache<String, Bitmap>(maxMemory)

    /**
     * Loads an image from the cache if available, otherwise loads it from the network.
     * @param imageUrl The URL of the image to load.
     * @return The loaded Bitmap image.
     */
    suspend fun loadImage(imageUrl: String): Bitmap {
        return memoryCache.get(imageUrl) ?: loadBitmapFromCacheOrUrl(imageUrl)
    }

    /**
     * Tries to load the image from the disk cache. If not found, loads it from the network.
     * @param imageUrl The URL of the image to load.
     * @return The loaded Bitmap image.
     */

    private suspend fun loadBitmapFromCacheOrUrl(imageUrl: String): Bitmap {
        val cacheFile = File(MyApp.getContext().cacheDir, cacheDir)
        if (!cacheFile.exists()) cacheFile.mkdirs()

        val cachedFile = File(cacheFile, imageUrl.hashCode().toString())

        return if (cachedFile.exists()) {
            // Load from disk cache
            val bitmap = withContext(Dispatchers.IO) {
                BitmapFactory.decodeStream(FileInputStream(cachedFile))
            }
            memoryCache.put(imageUrl, bitmap)
            bitmap
        } else {
            // Load from URL and save to cache
            val bitmap = loadImageBitmap(imageUrl)
            saveBitmapToCache(bitmap, cachedFile)
            memoryCache.put(imageUrl, bitmap)
            bitmap
        }
    }

    /**
     * Loads the image bitmap from the network.
     * @param imageUrl The URL of the image to load.
     * @return The loaded Bitmap image.
     */
    private suspend fun loadImageBitmap(imageUrl: String): Bitmap {
        return  try {
            withContext(Dispatchers.IO) {
                val urlConnection = URL(imageUrl).openConnection() as HttpURLConnection
                urlConnection.connect()

                val contentType = urlConnection.contentType
                if (contentType != null && contentType.contains("image")) {
                    // Content is an image, proceed with decoding
                    val inputStream = urlConnection.inputStream
                    val options = BitmapFactory.Options().apply {
                        // Set inSampleSize to lower memory usage
                        inSampleSize = 2 // You can adjust this as needed
                    }
                    val bitmap = BitmapFactory.decodeStream(inputStream, null, options)
                    inputStream.close()
                    bitmap ?: throw BitmapDecodeException("Failed to decode bitmap")
                } else {
                    throw UrlNotImageException("Given URL not belongs to image")
                }
            }
        } catch (e: Exception)
        {
            // If loading fails, return a default image from resources
            val context = MyApp.getContext()
            val defaultImageResource = context.getDrawable(android.R.drawable.ic_menu_close_clear_cancel)
            defaultImageResource?.toBitmap()
                ?: throw IllegalStateException("Default image resource not found or cannot be converted to Bitmap")
        }

    }

    /**
     * Saves the bitmap to the disk cache.
     * @param bitmap The Bitmap image to save.
     * @param cachedFile The file where the bitmap will be saved.
     */
    private suspend fun saveBitmapToCache(bitmap: Bitmap, cachedFile: File) {
        withContext(Dispatchers.IO) {
            val outputStream = FileOutputStream(cachedFile)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.flush()
            outputStream.close()
        }
    }
}