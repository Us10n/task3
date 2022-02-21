package com.example.task3.viewmodel

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.task3.database.PhoneContact
import com.example.task3.database.PhoneContactRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class MainViewModel @Inject constructor() : ViewModel() {

    private val NO_PHONE_NUMBER = "No phone number saved"
    private val PREFERENCE_PHONE_NUMBER = "phoneNumber"

    @Inject
    @Named("phonesSP")
    lateinit var sharedPreferences: SharedPreferences

    @Inject
    @Named("phoneContactRep")
    lateinit var repository: PhoneContactRepository

    private val phoneNumbersLiveData = MutableLiveData<List<String>>()
    val phoneNumbers: LiveData<List<String>>
        get() = phoneNumbersLiveData

    private val phoneContactLiveData = MutableLiveData<PhoneContact>()
    val phoneContact: LiveData<PhoneContact>
        get() = phoneContactLiveData

    fun findAllPhoneNumbers() {
        viewModelScope.launch(Dispatchers.IO) {
            val list = repository.findAllPhoneNumbers()
            phoneNumbersLiveData.postValue(list)
        }
    }

    fun findPhoneContactByArrayIndex(index: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val foundContact = if (phoneNumbers.value != null) {
                repository.findPhoneContactByPhoneNumber(phoneNumbers.value!![index])
            } else {
                PhoneContact()
            }
            phoneContactLiveData.postValue(foundContact)
        }
    }

    fun saveContactToDatabase(
        firstName: String?,
        lastName: String?,
        phoneNumber: String?,
        email: String?
    ) {
        val contact = PhoneContact(
            0,
            firstName.orEmpty(),
            lastName.orEmpty(),
            phoneNumber.orEmpty(),
            email.orEmpty()
        )
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertContact(contact)
        }
    }

    fun saveNumberToSharedPreference(phoneNumberId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val value = phoneNumbers.value?.get(phoneNumberId)
            if (value != null) {
                val editor = sharedPreferences.edit()
                editor.putString(PREFERENCE_PHONE_NUMBER, value)
                editor.apply()
            }
        }
    }

    fun loadNumberFromSharePreference() =
        sharedPreferences.getString(PREFERENCE_PHONE_NUMBER, NO_PHONE_NUMBER).toString()
}