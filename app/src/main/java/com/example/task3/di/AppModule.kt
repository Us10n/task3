package com.example.task3.di

import android.content.Context
import android.content.SharedPreferences
import com.example.task3.database.PhoneContactRepository
import com.example.task3.database.PhoneContactDao
import com.example.task3.database.PhoneContactDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Singleton
    @Named("database")
    @Provides
    fun providePhoneContactDatabase(@ApplicationContext context: Context): PhoneContactDatabase =
        PhoneContactDatabase.getDatabase(context)

    @Singleton
    @Named("phoneContactDao")
    @Provides
    fun providePhoneContactDao(
        @Named("database")
        database: PhoneContactDatabase
    ): PhoneContactDao = database.phoneContactDao()

    @Singleton
    @Named("phonesSP")
    @Provides
    fun providePhonesSharedPreference(@ApplicationContext context: Context): SharedPreferences =
        context.getSharedPreferences("phones", Context.MODE_PRIVATE)

    @Singleton
    @Named("phoneContactRep")
    @Provides
    fun providePhoneContactRepository(
        @Named("phoneContactDao") phoneContactDao: PhoneContactDao
    ): PhoneContactRepository = PhoneContactRepository(phoneContactDao)



}