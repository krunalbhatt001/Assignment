package com.assignment.model

import android.graphics.Bitmap

/**
 * Sealed class representing various UI models used in the application.
 */
sealed class UiModel {
    /**
     * Represents an image UI model.
     *
     * @property imageUrl The URL of the image.
     * @property bitmap The bitmap representation of the image.
     */
    data class Image(val imageUrl: String, val bitmap: Bitmap) : UiModel()
}