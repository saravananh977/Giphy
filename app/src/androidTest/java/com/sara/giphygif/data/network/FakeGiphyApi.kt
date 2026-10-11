package com.sara.giphygif.data.network

import com.google.gson.Gson
import com.sara.giphygif.domain.model.GifData
import java.util.concurrent.CopyOnWriteArrayList
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Stands in for the Giphy API in UI tests. Responses are parsed from JSON with Gson,
 * the same way Retrofit parses real responses.
 */
@Singleton
class FakeGiphyApi @Inject constructor() : ApiInterface {

    // When set, every request fails with this exception
    @Volatile
    var error: Exception? = null

    val requestedUrls = CopyOnWriteArrayList<String>()

    override suspend fun getGifData(url: String): GifData {
        requestedUrls.add(url)
        error?.let { throw it }
        return if (url.contains("/v1/gifs/search")) gifData(SEARCH, 3) else gifData(TRENDING, 3)
    }

    private fun gifData(prefix: String, count: Int): GifData {
        val items = (1..count).joinToString(",") {
            """{"id":"$prefix-$it","title":"$prefix gif $it","type":"gif",
               "images":{"original":{"url":"https://example.invalid/$prefix-$it.gif"}}}"""
        }
        val json = """{"data":[$items],
            "meta":{"msg":"OK","response_id":"test","status":200},
            "pagination":{"count":$count,"offset":0,"total_count":$count}}"""
        return Gson().fromJson(json, GifData::class.java)
    }

    companion object {
        const val TRENDING = "trending"
        const val SEARCH = "search"
    }

}
