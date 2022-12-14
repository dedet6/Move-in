package com.example.movein

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.example.movein.converters.Converters
import com.example.movein.database.AppDatabase
import com.example.movein.database.MovieDao
import com.example.movein.apis.FakeTmdbApi
import com.example.movein.repository.Repository
import com.example.movein.apis.TmdbApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Qualifier
import javax.inject.Singleton

@HiltAndroidApp
class MyApplication: Application()

@Qualifier
annotation class FakeApi
@Qualifier
annotation class RealApi

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Singleton
    @Provides
    fun providerConverters() : Converters {
        return Converters()
    }
    @Singleton
    @Provides
    fun provideDb(@ApplicationContext context: Context, converters : Converters)
            : MovieDao =
        Room.databaseBuilder(
            context,
            AppDatabase::class.java, "move_in_database"
        )
            .addTypeConverter(converters)
            .fallbackToDestructiveMigration()
            .build().filmDao()

    @RealApi
    @Singleton
    @Provides
    fun provideTmdbApi() : TmdbApi =
        Retrofit.Builder()
            .baseUrl("https://api.themoviedb.org/3/")
            .addConverterFactory(MoshiConverterFactory.create())
            .build().create(TmdbApi::class.java)

    @FakeApi
    @Singleton
    @Provides
    fun provideFakeTmdbApi() : TmdbApi { return FakeTmdbApi() }

    @Singleton
    @Provides
    fun provideRepository(@RealApi api: TmdbApi, db: MovieDao) = Repository(api, db)
}