package com.example.task3.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PhoneContactDao {

    @Query("SELECT phone_number FROM phone_contacts")
    fun findAllPhoneNumbers(): List<String>

    @Query("SELECT id,first_name,last_name,phone_number,email FROM phone_contacts WHERE phone_number=:phoneNumber")
    fun findContactByPhoneNumber(phoneNumber: String): PhoneContact

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addPhoneContact(phoneContact: PhoneContact): Long

}