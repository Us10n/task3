package com.example.task3.database

class PhoneContactRepository(private val phoneContactDao: PhoneContactDao) {

    fun findAllPhoneNumbers(): List<String> =
        phoneContactDao.findAllPhoneNumbers()

    fun findPhoneContactByPhoneNumber(phoneNumber: String): PhoneContact =
        phoneContactDao.findContactByPhoneNumber(phoneNumber)

    suspend fun insertContact(phoneContact: PhoneContact): Long =
        phoneContactDao.addPhoneContact(phoneContact)

}