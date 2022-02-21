package com.example.task3.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [PhoneContact::class], version = 1, exportSchema = false)
abstract class PhoneContactDatabase : RoomDatabase() {

    abstract fun phoneContactDao(): PhoneContactDao

    companion object {
        fun getDatabase(context: Context): PhoneContactDatabase =
            Room.databaseBuilder(
                context.applicationContext,
                PhoneContactDatabase::
                class.java,
                "contact_database"
            ).build()

    }
}