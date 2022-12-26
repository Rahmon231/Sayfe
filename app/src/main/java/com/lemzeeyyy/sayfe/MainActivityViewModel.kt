package com.lemzeeyyy.sayfe

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.net.Uri
import android.provider.ContactsContract
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.lemzeeyyy.sayfe.model.GuardianData
import com.lemzeeyyy.sayfe.model.RecipientContact


class MainActivityViewModel(application: Application) : AndroidViewModel(application) {
    private val database = Firebase.firestore
    private val collectionReference = database.collection("Guardian Angels")


    private val _guardianLiveData = MutableLiveData<GuardianData>()
    val guardianLiveData: LiveData<GuardianData> get() = _guardianLiveData


    private val _userImageUri = MutableLiveData<Uri>()
    val userImageUri : LiveData<Uri> get() = _userImageUri


    private val _userContactsLiveData = MutableLiveData<MutableList<RecipientContact>?>()
    val userContactsLiveDataList : LiveData<MutableList<RecipientContact>?> get() = _userContactsLiveData


    fun getGuardianAngelsListToDb(currentUserid: String) {

        collectionReference.document(currentUserid)
            .get()
            .addOnSuccessListener {

                val data = it.toObject(GuardianData::class.java)

                data?.let { guardianData ->
                    _guardianLiveData.value = guardianData
                }
            }
            .addOnFailureListener {

            }
    }

fun getImageUriFromDb(currentUserid: String){
    var storageRef = FirebaseStorage.getInstance().getReference();

    var imagesRef: StorageReference = storageRef.child("profile_images").child(currentUserid)
    imagesRef.downloadUrl.addOnSuccessListener {

        _userImageUri.value = it
    }.addOnFailureListener {

    }

}


    @SuppressLint("Range")
     fun getPhoneBook() : MutableLiveData<MutableList<RecipientContact>?> {

        var contacts : MutableList<RecipientContact> = ArrayList()

        val cr = getApplication<Application>().contentResolver
        val query = cr.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null,
            null,
            null,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
        if (query!!.count > 0){
            while (query.moveToNext()){
                val id = query.getString(query.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NAME_RAW_CONTACT_ID))
                val name = query.getString(query.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                val number = query.getString(query.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))

                contacts.add(RecipientContact(id,name,number))

                _userContactsLiveData.value = contacts
            }
        }

        return _userContactsLiveData

    }

}
