package com.assignment.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.widget.Toast
import androidx.compose.material3.Snackbar
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.assignment.app.MyApp
import com.assignment.exception.ForbiddenException
import com.assignment.exception.HttpErrorException
import com.assignment.exception.JsonParsingException
import com.assignment.exception.NoInternetException
import com.assignment.exception.UnreachableServerException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.InetAddress
import java.net.URL
import java.net.UnknownHostException

/**
 * Paging source for loading paginated data from the Unsplash API.
 *
 * This class implements the [PagingSource] interface to load paginated data
 * from the Unsplash API based on the given client ID.
 *
 */
class UnsplashPagingSource : PagingSource<Int, String>() {


    /**
     * Loads paginated data from the Unsplash API.
     *
     * This function is called by the paging library to load data for a given page.
     *
     * @param params Parameters for loading data, including the page key and page size.
     * @return A [LoadResult<Int, String>] object representing the result of the load operation.
     */
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, String> {
        return try {
            val nextPageNumber = params.key ?: 1
            val response = withContext(Dispatchers.IO) {
                if (isInternetAvailable(MyApp.getContext())) {
                    makeNetworkCall(nextPageNumber)
                } else {
                    throw NoInternetException("No Internet Connection")
                }
            }
            val urls = parseResponse(response)

            LoadResult.Page(
                data = urls,
                prevKey = null,
                nextKey = nextPageNumber + 1
            )
        } catch (e: Exception) {
            Toast.makeText(MyApp.getContext(),e.message,Toast.LENGTH_LONG).show()
            LoadResult.Error(e)
        }
    }

    /**
     * Makes a network call to the Unsplash API to fetch photos.
     *
     * @param nextPage The page number to fetch.
     * @return The JSON response string from the API.
     * @throws ForbiddenException If the request is forbidden (HTTP 403 error).
     * @throws HttpErrorException If an HTTP error occurs.
     * @throws UnreachableServerException If the server is unreachable.
     */
    private fun makeNetworkCall(nextPage: Int): String {
        try {
            val urlString = "https://api.unsplash.com/photos/?client_id=E5Hwf57UP-yMQ8xCAqgSvRABRI62SzpiWTXwKK0KEqU&page=$nextPage"
            val url = URL(urlString)

            // Resolve the host to check its availability
            val hostAddress = InetAddress.getByName(url.host)

            // If the host is reachable, proceed with the network call
            if (!hostAddress.isReachable(3000)) { // Timeout set to 3 seconds
                throw UnreachableServerException("Host ${url.host} is not reachable")
            }


            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"

            val response = StringBuilder()

            when (val responseCode = connection.responseCode) {
                HttpURLConnection.HTTP_OK -> {
                    val reader = BufferedReader(InputStreamReader(connection.inputStream))
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        response.append(line)
                    }
                    reader.close()
                }

                HttpURLConnection.HTTP_FORBIDDEN -> {
                    // Handle 403 Forbidden error
                    throw ForbiddenException("You can use the Unsplash API for free, but please note that it comes with a default rate limit of 50 requests per hour. This is referred to as Demo status.")
                }

                else -> {
                    // Handle HTTP error responses
                    throw HttpErrorException("HTTP Error: $responseCode")
                }
            }

            connection.disconnect()

            return response.toString()
        } catch (e: UnknownHostException) {
            // Handle case when server is unreachable
            Toast.makeText(MyApp.getContext(),e.localizedMessage,Toast.LENGTH_SHORT).show()
            e.printStackTrace()
            return ""
        }catch (e: ForbiddenException) {
            // Handle case when server is unreachable
            e.printStackTrace()
            return ""
        } catch (e: Exception) {
            // Handle other exceptions
            e.printStackTrace()
            return ""
        }
    }

    /**
     * Parses the JSON response from the Unsplash API to extract image URLs.
     *
     * @param response The JSON response string.
     * @return A list of image URLs extracted from the response.
     * @throws JsonParsingException If an error occurs while parsing JSON.
     */

    private fun parseResponse(response: String): List<String> {
        val urlsList = mutableListOf<String>()
        try {
            val jsonArray = JSONArray(response)
            for (i in 0 until jsonArray.length()) {
                val jsonObject: JSONObject = jsonArray.getJSONObject(i)
                val urlsObject: JSONObject = jsonObject.getJSONObject("urls")
                val regularUrl: String = urlsObject.getString("small")
                urlsList.add(regularUrl)
            }
        } catch (e: JSONException) {
            // Handle JSON parsing error
            throw JsonParsingException("Error parsing JSON: ${e.message}")
        }

        return urlsList
    }

    /**
     * Retrieves the refresh key for the current paging state.
     *
     * This function is called to determine the refresh key for the current state
     * of the paging operation.
     *
     * @param state The current [PagingState].
     * @return The refresh key for the current state, typically the anchor position.
     */
    override fun getRefreshKey(state: PagingState<Int, String>): Int? {
        return state.anchorPosition
    }

    /**
     * Checks if the device has an active internet connection.
     *
     * @param context The context of the application or activity.
     * @return True if the device has an active internet connection, false otherwise.
     */
    private fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}