package com.lemzeeyyy.sayfe.viewmodels

import android.annotation.SuppressLint
import android.app.Application
import android.net.Uri
import android.provider.ContactsContract
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.lemzeeyyy.sayfe.model.GuardianData
import com.lemzeeyyy.sayfe.model.IncomingAlertData
import com.lemzeeyyy.sayfe.model.OutgoingAlertData
import com.lemzeeyyy.sayfe.model.RecipientContact
import com.lemzeeyyy.sayfe.service.AccessibilityKeyDetector


class MainActivityViewModel(application: Application) : AndroidViewModel(application) {
    private val database = Firebase.firestore
    private val outgoingAlertDb = Firebase.database
    private val myRef = outgoingAlertDb.getReference("OutgoingAlerts")
    private val collectionReference = database.collection("Guardian Angels")


    private val _guardianLiveData = MutableLiveData<GuardianData>()
    val guardianLiveData: LiveData<GuardianData> get() = _guardianLiveData


    private val _userImageUri = MutableLiveData<Uri>()
    val userImageUri : LiveData<Uri> get() = _userImageUri


    private val _userContactsLiveData = MutableLiveData<MutableList<RecipientContact>?>()
    val userContactsLiveDataList : LiveData<MutableList<RecipientContact>?> get() = _userContactsLiveData

    private val _outgoingAlertListLiveData = MutableLiveData<MutableList<OutgoingAlertData>?>()
    val outgoingAlertListLiveData : LiveData<MutableList<OutgoingAlertData>?> get() = _outgoingAlertListLiveData

    private val _incomingAlertListLiveData = MutableLiveData<MutableList<IncomingAlertData>?>()
    val incomingAlertListLiveData : LiveData<MutableList<IncomingAlertData>?> get() = _incomingAlertListLiveData


    fun getOutgoingAlertList(){
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach {

                    val outgoingDataList = it.getValue<MutableList<OutgoingAlertData>>()!!

                    _outgoingAlertListLiveData.value = outgoingDataList
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("TAG", "onCancelled: ${error.message.toString()} ")
            }
        }
        )
    }

    fun getIncomingAlertList(){
        myRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                //Update recyclerView for only targeted appID
                snapshot.children.forEach {
                    val incomingDataList = it.getValue<MutableList<IncomingAlertData>>()!!
                    _incomingAlertListLiveData.value = incomingDataList
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }


    fun getGuardianAngelsListFromDb(currentUserid: String) {

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

                contacts.add(RecipientContact(id,name, number))
                val distinctContact = contacts.distinctBy {
                    it.id
                }

                _userContactsLiveData.value = distinctContact as MutableList<RecipientContact>
            }
        }

        return _userContactsLiveData

    }

    

}
