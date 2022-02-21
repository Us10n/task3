package com.example.task3

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.task3.database.PhoneContact
import com.example.task3.databinding.ActivityMainBinding
import com.example.task3.viewmodel.MainViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    companion object {
        private const val READ_CONTACTS_REQUEST_CODE = 100
        private const val SUCCESSFUL_SAVE = "Сохранение успешно"
        private const val FIRST_NAME = 0
        private const val LAST_NAME = 1
    }

    private val binding: ActivityMainBinding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        checkPermission()

        binding.chooseContact.setOnClickListener {
            openContacts()
        }
        binding.showContactsDatabase.setOnClickListener {
            findSavedPhoneContacts()
        }
        binding.showContactSP.setOnClickListener {
            showPhoneNumberFromSP()
        }

        mainViewModel.phoneNumbers.observe(this) {
            showDatabaseSavedNumbers(it)
        }
        mainViewModel.phoneContact.observe(this) {
            showChosenPhoneContact(it)
        }
    }

    private fun checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_CONTACTS),
                READ_CONTACTS_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode != READ_CONTACTS_REQUEST_CODE ||
            grantResults.isEmpty() ||
            grantResults[0] != PackageManager.PERMISSION_GRANTED
        ) {
            checkPermission()
        }
    }

    @SuppressLint("Range")
    private val launchSomeActivity =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val contactData = result.data?.data
                if (contactData != null) {
                    val contactCursor = contentResolver.query(contactData, null, null, null, null)
                    if (contactCursor?.moveToFirst() == true) {
                        val contactId =
                            contactCursor.getString(contactCursor.getColumnIndex(ContactsContract.Contacts._ID))

                        val phoneNumber: String? = findPhoneNumber(contactCursor, contactId)
                        val email: String? = findEmail(contactId)
                        val firstName: String? = findNames(contactId)[FIRST_NAME]
                        val lastName: String? = findNames(contactId)[LAST_NAME]

                        mainViewModel.saveContactToDatabase(firstName, lastName, phoneNumber, email)
                        Toast.makeText(this, SUCCESSFUL_SAVE, Toast.LENGTH_SHORT).show()
                    }
                    contactCursor?.close()
                }
            }
        }

    @SuppressLint("Range")
    private fun findPhoneNumber(contactCursor: Cursor, contactId: String): String? {
        var phoneNumber: String? = ""
        val hasPhone =
            contactCursor.getString(contactCursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))
        if (hasPhone.equals("1")) {
            val phones = contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null,
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId,
                null,
                null
            )
            if (phones?.moveToFirst() == true) {
                phoneNumber =
                    phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
            }
            phones?.close()
        }
        return phoneNumber
    }

    @SuppressLint("Range")
    private fun findEmail(contactId: String): String? {
        var email: String? = ""
        val emails: Cursor? = contentResolver.query(
            ContactsContract.CommonDataKinds.Email.CONTENT_URI,
            null,
            ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = " + contactId,
            null,
            null
        )
        if (emails?.moveToFirst() == true) {
            email =
                emails.getString(emails.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA))
        }
        emails?.close()
        return email
    }

    @SuppressLint("Range")
    private fun findNames(contactId: String): List<String?> {
        var firstName: String? = ""
        var lastName: String? = ""
        val whereName =
            ContactsContract.Data.MIMETYPE + " = ? AND " + ContactsContract.CommonDataKinds.StructuredName.CONTACT_ID + " = " + contactId
        val whereNameParams =
            arrayOf(ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
        val nameCur = contentResolver.query(
            ContactsContract.Data.CONTENT_URI,
            null,
            whereName,
            whereNameParams,
            ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME
        )
        if (nameCur?.moveToFirst() == true) {
            firstName =
                nameCur.getString(nameCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME))
            lastName =
                nameCur.getString(nameCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME))
        }
        nameCur?.close()
        return listOf(firstName, lastName)
    }

    private fun openContacts() {
        val intent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
        launchSomeActivity.launch(intent)
    }

    private fun showDatabaseSavedNumbers(phoneNumbersList: List<String>) {
        val dialog = MaterialAlertDialogBuilder(this)
            .setTitle(resources.getString(R.string.saved_contacts))
            .setItems(phoneNumbersList.toTypedArray()) { _, which ->
                findPhoneContact(which)
                mainViewModel.saveNumberToSharedPreference(which)
            }
            .show()
    }

    private fun findPhoneContact(phoneNumberIndex: Int) {
        mainViewModel.findPhoneContactByArrayIndex(phoneNumberIndex)
    }

    private fun showChosenPhoneContact(phoneContact: PhoneContact) {
        Toast.makeText(
            this, "Имя: ${phoneContact.firstName}\n" +
                    "Фамилия: ${phoneContact.lastName}\n" +
                    "Телефон: ${phoneContact.phoneNumber}\n" +
                    "Почта: ${phoneContact.email}", Toast.LENGTH_LONG
        ).show()
    }

    private fun findSavedPhoneContacts() {
        mainViewModel.findAllPhoneNumbers()
    }

    private fun showPhoneNumberFromSP() {
        val message = mainViewModel.loadNumberFromSharePreference()
        Snackbar.make(binding.snackbar, message, Snackbar.LENGTH_SHORT)
            .show()
    }

    override fun onDestroy() {
        mainViewModel.phoneContact.removeObservers(this)
        mainViewModel.phoneNumbers.removeObservers(this)
        super.onDestroy()
    }
}