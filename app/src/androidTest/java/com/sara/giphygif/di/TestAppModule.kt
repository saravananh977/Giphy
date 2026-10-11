package com.sara.giphygif.di

import android.app.Application
import androidx.room.Room
import com.sara.giphygif.data.localdb.AppDao
import com.sara.giphygif.data.localdb.AppDb
import com.sara.giphygif.data.network.ApiInterface
import com.sara.giphygif.data.network.FakeGiphyApi
import com.sara.giphygif.data.repository.GifRepository
import com.sara.giphygif.data.repository.GifRepositoryImpl
import com.sara.giphygif.utils.UrlUtils
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

// Replaces AppModule in UI tests: fake network, fresh in-memory database per test
@Module
@TestInstallIn(components = [SingletonComponent::class], replaces = [AppModule::class])
object TestAppModule {

    @Provides
    @Singleton
    fun provideGifRepository(
        apiInterface: ApiInterface,
        urlUtils: UrlUtils, appDao: AppDao
    ): GifRepository {
        return GifRepositoryImpl(apiInterface, urlUtils, appDao)
    }

    @Provides
    @Singleton
    fun provideApiInterface(fakeGiphyApi: FakeGiphyApi): ApiInterface = fakeGiphyApi

    @Provides
    @Singleton
    fun provideGifDb(app: Application): AppDb {
        return Room.inMemoryDatabaseBuilder(app, AppDb::class.java).build()
    }

    @Provides
    @Singleton
    fun appDao(appDatabase: AppDb) = appDatabase.appDao()

}
