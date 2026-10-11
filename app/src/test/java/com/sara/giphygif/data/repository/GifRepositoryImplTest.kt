package com.sara.giphygif.data.repository

import com.google.common.truth.Truth
import com.sara.giphygif.data.entities.GifEntity
import com.sara.giphygif.data.localdb.FakeAppDao
import com.sara.giphygif.data.network.FakeApiInterface
import com.sara.giphygif.domain.ResponseState
import com.sara.giphygif.domain.model.GifData
import com.sara.giphygif.domain.model.Meta
import com.sara.giphygif.domain.model.Pagination
import com.sara.giphygif.utils.UrlUtils
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.io.IOException

class GifRepositoryImplTest {

    var fakeDao = FakeAppDao()

    var fakeApiInterface = FakeApiInterface()

    var gifRepository = GifRepositoryImpl(fakeApiInterface, UrlUtils(), fakeDao)

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `get Trending Data From Server Success`() {

        val gifData = GifData(emptyList(), Meta("a", "b", 1), Pagination(1, 2, 3))


        fakeApiInterface.getGifDataCall = {
            gifData
        }


        runTest {

            val response = gifRepository.getTrendingDataFromServer().toList()

            Truth.assertThat(response.first()).isInstanceOf(ResponseState.LOADING::class.java)


            Truth.assertThat(response[1]).isInstanceOf(ResponseState.SUCCESS::class.java)
            Truth.assertThat(response[1].list).isEqualTo(gifData)


        }

    }


    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `get Trending Data From Server Failure`() {

        fakeApiInterface.getGifDataCall = {
            throw IOException()
        }


        runTest {

            val response = gifRepository.getTrendingDataFromServer().toList()

            Truth.assertThat(response.first()).isInstanceOf(ResponseState.LOADING::class.java)

            Truth.assertThat(response[1]).isInstanceOf(ResponseState.FAILURE::class.java)
            Truth.assertThat(response[1].message).startsWith("Error")


        }

    }
    
    
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun insertGifTest() = runTest {

        val gif = GifEntity("1","title1","gif","https://gif.com")

        fakeDao.insertGif(gif)

        val allgif = fakeDao.fetchAllGifFromDb()
        Truth.assertThat(allgif).containsExactly(gif)

    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun removeGifFromDbTest() = runTest {

        val gif = GifEntity("1","title1","gif","https://gif.com")

        fakeDao.insertGif(gif)
        fakeDao.removeGifFromDb(gif)

        val allgif = fakeDao.fetchAllGifFromDb()
        Truth.assertThat(allgif).isEmpty()

    }






}