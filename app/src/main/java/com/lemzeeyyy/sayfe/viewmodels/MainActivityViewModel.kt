package com.lemzeeyyy.sayfe.viewmodels

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.net.Uri
import android.provider.ContactsContract
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.lemzeeyyy.sayfe.model.*
import com.lemzeeyyy.sayfe.service.AccessibilityKeyDetector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

const val BUSY = 1
const val PASSED = 2
const val EMPTY = 3
const val FAILED = 4

class MainActivityViewModel(application: Application) : AndroidViewModel(application) {

    private val database = Firebase.firestore
    private val outgoingAlertDb = Firebase.database
    private val myRef = outgoingAlertDb.getReference("OutgoingAlerts")
    private val collectionReference = database.collection("Guardian Angels")
    private val userReference = database.collection("Users")
    private val fAuth = Firebase.auth


    private val _guardianLiveData = MutableLiveData<GuardianData>()
    val guardianLiveData: LiveData<GuardianData> get() = _guardianLiveData

    private var _contactStatus = MutableLiveData<Int>()
    val contactStatus: LiveData<Int> get() = _contactStatus

    private var _guardianAngelsStatus = MutableLiveData<Int>()
    val guardianAngelsStatus: LiveData<Int> get() = _guardianAngelsStatus

    private val _userImageUri = MutableLiveData<Uri>()
    val userImageUri : LiveData<Uri> get() = _userImageUri

    private var _currentUserCountryCode = MutableLiveData<String>()
    val currentUserCountryCode: LiveData<String> get() = _currentUserCountryCode


//    private val _userContactsLiveData = MutableLiveData<MutableList<RecipientContact>?>()
//    val userContactsLiveDataList : LiveData<MutableList<RecipientContact>?> get() = _userContactsLiveData


    private val _userContactsLiveData = MutableLiveData<ContactsState>()
    val userContactsLiveDataList : LiveData<ContactsState> get() = _userContactsLiveData


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
        _guardianAngelsStatus.value = BUSY
        collectionReference.document(currentUserid)
            .get()
            .addOnSuccessListener {
                val data = it.toObject(GuardianData::class.java)
                data?.let { guardianData ->
                    _guardianLiveData.value = guardianData
                    _guardianLiveData.value?.guardianInfo?.let { guardianList->
                        if (guardianList.isEmpty()){
                            _guardianAngelsStatus.value = EMPTY
                        }else{
                            _guardianAngelsStatus.value = PASSED
                        }
                    }

                }
            }
            .addOnFailureListener {
                _guardianAngelsStatus.value = FAILED
            }
    }

    private fun getGetCountryCode(currentUserid: String) {

        userReference.whereEqualTo("currentUserId",currentUserid)
            .get()
            .addOnSuccessListener { querySnapshot ->
                querySnapshot.forEach { queryDocumentSnapshot ->
                    val users = queryDocumentSnapshot.toObject(Users::class.java)
                    _currentUserCountryCode.value = users.countryCode
                    Log.d("COUNTRY CODE FROM METHOD", "getGetCountryCode: ${_currentUserCountryCode.value} ")
                }
            }

    }


    fun getImageUriFromDb(currentUserid: String){
    val storageRef = FirebaseStorage.getInstance().getReference();

    val imagesRef: StorageReference = storageRef.child("profile_images").child(currentUserid)
    imagesRef.downloadUrl.addOnSuccessListener {

        _userImageUri.value = it
    }.addOnFailureListener {

    }

}


//    @SuppressLint("Range")
//      fun getPhoneBook() : MutableLiveData<MutableList<RecipientContact>?> {
//        //set default state
//        _contactStatus.value = BUSY
//        viewModelScope.launch {
//            val contacts : MutableList<RecipientContact> = ArrayList()
//
//            val cr = getApplication<Application>().contentResolver
//            val query = cr.query(
//                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
//                null,
//                null,
//                null,
//                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
//            if (query!!.count > 0){
//                while (query.moveToNext()){
//                    val id = query.getString(query.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NAME_RAW_CONTACT_ID))
//                    val name = query.getString(query.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
//                    val number = query.getString(query.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
//
//                    contacts.add(RecipientContact(id,name, number))
//                    val distinctContact = contacts.distinctBy {
//                        it.id
//                    }
//                    _userContactsLiveData.value = distinctContact as MutableList<RecipientContact>
//                    _contactStatus.value = PASSED
//                }
//            }
//        }
//        return _userContactsLiveData
//
//    }

    @SuppressLint("Range")
    fun getPhoneBook(context: Activity) {
        val user = fAuth.currentUser
        var countryCode : String? = ""

        viewModelScope.launch {
            val currentUserid = user?.uid
            _userContactsLiveData.value  = ContactsState.Empty
            _contactStatus.value = BUSY
            delay(1000)
            userReference.whereEqualTo("currentUserId",currentUserid)
                .get()
                .addOnSuccessListener {
                    for(snapshot : QueryDocumentSnapshot in it){
                        val doc = snapshot.toObject(Users::class.java)
                       countryCode = doc.countryCode
                        val contacts : MutableList<PhonebookContact> = ArrayList()
                        val cr = context.contentResolver
                        val query = cr.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            null,
                            null,
                            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                        if (query != null) {
                            if (query.count > 0){
                                while (query.moveToNext()){
                                    val id = query.getString(query.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NAME_RAW_CONTACT_ID))
                                    val name = query.getString(query.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                                    var number = query.getString(query.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))

                                    if (number.startsWith("+")){
                                        number = query.getString(query.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                                    }else{
                                        number = "+$countryCode${query.getString(query.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))}"
                                    }
                                    contacts.add(PhonebookContact(id,name, number))

                                    val distinctContact = contacts.distinctBy { distinct ->
                                        distinct.id
                                    }
                                    if (distinctContact.isEmpty()){
                                        _contactStatus.value = EMPTY
                                    }else{
                                        _contactStatus.value = PASSED
                                    }


                                    _userContactsLiveData.value = ContactsState.Success(distinctContact as MutableList<PhonebookContact>)

                                }
                                query.close()
                            }
                        }

                    }
                }.addOnFailureListener {
                    Log.d("TAG", "getPhoneBook: ${it.message.toString()}")
                }



        }

    }

    

}
