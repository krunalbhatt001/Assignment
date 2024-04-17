package com.assignment.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.assignment.loader.ImageLoader
import com.assignment.model.UiModel
import com.assignment.network.UnsplashPagingSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel responsible for managing the data loading and processing logic for paging images.
 *
 * This class orchestrates the loading of images from the network using the Unsplash API
 * and provides the paginated data to the UI layer.
 */
class PagingViewModel : ViewModel() {

    // Image loader instance for loading images from URLs
    private var imageLoader = ImageLoader()

    // Mutable state flow representing the paginated image data
    private val _imageResponse: MutableStateFlow<PagingData<UiModel.Image>> =
        MutableStateFlow(PagingData.empty())

    // Immutable state flow exposing the paginated image data to observers
    val imageResponse: StateFlow<PagingData<UiModel.Image>> = _imageResponse

    /**
     * Fetches paginated image data from the network and updates the [imageResponse].
     */
    fun fetchDataFromNetwork() {
        viewModelScope.launch {
            try {
                // Launch a coroutine to load paginated data from the network
                Pager(
                    config = PagingConfig(
                        pageSize = 20, // Number of items per page
                        enablePlaceholders = true, // Enable placeholders for items
                    ),
                    // Provide a paging source factory to fetch data from the Unsplash API
                    pagingSourceFactory = { UnsplashPagingSource() }
                ).flow
                    // Cache the data in the ViewModel scope to avoid redundant network calls
                    .cachedIn(viewModelScope)
                    // Collect the paginated data and transform it into UI model objects
                    .collect { pagingData ->
                        // Map the URLs to UI model objects representing images
                        val uiModelData = pagingData.map { imageUrl ->
                            UiModel.Image(
                                imageUrl,
                                imageLoader.loadImage(imageUrl = imageUrl)
                            )
                        }
                        // Update the state flow with the transformed UI model data
                        _imageResponse.value = uiModelData
                    }
            }catch (e:Exception){
                e.printStackTrace()
            }
        }
    }
}















/*
*
* @Composable
fun LoadImageFromUrl(image: UiModel.Image,imageLoader: ImageLoader) {
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        bitmap = try {
            withContext(Dispatchers.IO) {
                imageLoader.loadImage(image.imageUrl)
            }
        } catch (e: Exception) {
            com.assignment.app.MyApp.getContext().getDrawable(R.drawable.ic_android_black)?.toBitmap()
        } finally {
            loading = false
        }
    }

    if (loading) {
        // Show a placeholder image while loading
        PlaceholderImage()
    } else {
        bitmap?.let {
            Image(
                contentScale = ContentScale.FillBounds,
                bitmap = it.asImageBitmap(),
                contentDescription = image.imageUrl,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
            )
        }
    }
}

@Composable
fun PlaceholderImage() {
    // You can replace this with your desired placeholder image
    Image(
        painterResource(id = R.drawable.ic_download),
        contentDescription = null,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
    )
}
* */