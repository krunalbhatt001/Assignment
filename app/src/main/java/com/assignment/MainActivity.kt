package com.assignment

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.paging.compose.collectAsLazyPagingItems
import com.assignment.model.UiModel
import com.assignment.viewmodel.PagingViewModel


class MainActivity : ComponentActivity() {
    private val pagingViewModel: PagingViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApp(pagingViewModel)
        }
    }
}


@Composable
fun MyApp(viewModel: PagingViewModel) {
    viewModel.fetchDataFromNetwork()
    val response = viewModel.imageResponse.collectAsLazyPagingItems()

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier.fillMaxSize()
    ) {
        items(response.itemCount) { index ->
            val item = response[index]
            item?.let { LoadImageFromUrl(it) }
        }
    }
}

@Composable
fun LoadImageFromUrl(image: UiModel.Image) {
    val bitmap by remember(image) { mutableStateOf(image.bitmap) }

    Image(
        contentScale = ContentScale.FillBounds,
        bitmap = bitmap.asImageBitmap(),
        contentDescription = image.imageUrl, // Provide proper content description
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
    )
}