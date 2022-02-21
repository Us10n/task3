package com.example.task3.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "phone_contacts")
data class PhoneContact(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id") val contactId: Long = 0,
    @ColumnInfo(name = "first_name") val firstName: String = "Unknown",
    @ColumnInfo(name = "last_name") val lastName: String = "Unknown",
    @ColumnInfo(name = "phone_number") val phoneNumber: String = "Unknown",
    @ColumnInfo(name = "email") val email: String = "Unknown"
)
