package com.lemzeeyyy.sayfe.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.lemzeeyyy.sayfe.repository.AuthRepository
import com.lemzeeyyy.sayfe.repository.AuthRepositoryImpl
import com.lemzeeyyy.sayfe.repository.SayfeRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Singleton
    @Provides
    fun provideFirebaseAuth() : FirebaseAuth {
        return Firebase.auth
    }
    @Singleton
    @Provides
    fun provideRepository() : SayfeRepository{
        return SayfeRepository()
    }

    @Singleton
    @Provides
    fun provideAuthRepository(impl : AuthRepositoryImpl) : AuthRepository{
        return impl
    }

}