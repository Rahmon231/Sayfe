package com.lemzeeyyy.sayfe.repository

import android.annotation.SuppressLint
import android.app.Application
import android.net.Uri
import android.provider.ContactsContract
import android.util.Log
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.lemzeeyyy.sayfe.model.*
import kotlinx.coroutines.tasks.await

class SayfeRepository {

    suspend fun getOutgoingAlertList(currentUserid: String)  : MutableList<OutgoingAlertData> {
        val outgoingAlertDb = Firebase.database
        val myRef = outgoingAlertDb.getReference("OutgoingAlerts")
        var outgoingDataList = mutableListOf<OutgoingAlertData>()
        val key = mutableListOf<String>()

        try {
            val result = myRef.get().await()
            result.children.forEach {
                if (currentUserid == it.key) {
                    outgoingDataList = it.getValue<MutableList<OutgoingAlertData>>()!!
                }
              //  it.key?.let { it1 -> key.add(it1) }
            }

        }catch (e:Exception){
            Log.d("Outgoing exception", "getOutgoingAlertList: ${e.message} ")
        }
       return outgoingDataList
    }

    suspend fun  getIncomingAlertList(currentUserid: String) : MutableList<IncomingAlertData> {
         val incomingAlertDb = Firebase.database
         val myRef = incomingAlertDb.getReference("IncomingAlerts")
        var incomingDataList = mutableListOf<IncomingAlertData>()
        val key  = mutableListOf<String>()
        try {
            val result = myRef.get().await()
            result.children.forEach {
                if (currentUserid == it.key) {
                    incomingDataList = it.getValue<MutableList<IncomingAlertData>>()!!
                }
                //it.key?.let { it1 -> key.add(it1) }
            }


        }catch (e:Exception){
            Log.d("Incoming exception", "getOutgoingAlertList: ${e.message} ")
        }
        Log.d("incomingDataList", "getOutgoingAlertList: $incomingDataList ")
        Log.d("incomingDataList", "getOutgoingAlertList: ${key.size} ")
        return incomingDataList

    }

    suspend fun checkDuplicateRegisteredNumber(phoneNumber: String) : Boolean{
        var isUserRegistered = false

        val database = Firebase.firestore
         val userReference = database.collection("Users")
        isUserRegistered = false
        try {
            val result = userReference.get().await()
            result.forEach {
                val users = it.toObject(Users::class.java)
                if (users.phoneNumber == phoneNumber){
                    isUserRegistered = true
                    return@forEach
                }
            }

        }catch (e:Exception){
            Log.d("Duplicate Registered Exception", "checkDuplicateRegisteredNumber:${e.message} ")
        }

        userReference.get().addOnSuccessListener { querySnapshot ->
            querySnapshot.forEach { queryDocumentSnapshot ->
                val users = queryDocumentSnapshot.toObject(Users::class.java)

                // Log.d("PHONE NUMBS", "CheckDuplicateRegisteredNumber: ${users.phoneNumber}")
                if (users.phoneNumber == phoneNumber){
                    isUserRegistered = true
                    return@forEach
                }
            }

        }
        return isUserRegistered

    }

    suspend fun getGuardianAngelsListFromDb(currentUserid: String) : GuardianData {
        val database = Firebase.firestore
        val collectionReference = database.collection("Guardian Angels")
        var guardianDataFromDb  = GuardianData(mutableListOf())
        try {
           val result =  collectionReference.document(currentUserid)
                .get()
                .await()
            if (result!=null){
                guardianDataFromDb = result.toObject(GuardianData::class.java)!!
            }



        } catch (e:Exception){
            Log.d("TAG", "getGuardianAngelsListFromDb: ${e.message}")
        }
        Log.d("CHECKGUARDIAN REPO2", "getGuardianAngelsListFromDb: ${guardianDataFromDb.guardianInfo.size}")
        return guardianDataFromDb
    }

    private suspend fun getGetCountryCode(currentUserid: String) : String {
        val database = Firebase.firestore
        val userReference = database.collection("Users")
        var userCountryCode = ""

        try {
           val result =  userReference.whereEqualTo("currentUserId",currentUserid)
                .get()
                .await()
            result.forEach {
                val users = it.toObject(Users::class.java)
                userCountryCode = users.countryCode
            }

        }catch (e:Exception){
            Log.d("Country Code Exception", "getGetCountryCode: ${e.message} ")
        }
        return userCountryCode
    }

     suspend fun getUserPhone(currentUserid: String) : String {
        val database = Firebase.firestore
        val userReference = database.collection("Users")
        var userPhoneNumber= ""

        try {
            val result =  userReference.whereEqualTo("currentUserId",currentUserid)
                .get()
                .await()
            result.forEach {
                val users = it.toObject(Users::class.java)
                userPhoneNumber = users.phoneNumber
            }

        }catch (e:Exception){
            Log.d("Country Code Exception", "getGetCountryCode: ${e.message} ")
        }
        return userPhoneNumber
    }

    suspend fun getImageUriFromDb(currentUserid: String) : Uri {
        val storageRef = FirebaseStorage.getInstance().getReference();
        val imagesRef: StorageReference = storageRef.child("profile_images").child(currentUserid)
        var imageUri = Uri.EMPTY
        try {
            val result = imagesRef.downloadUrl.await()
            if (result!=null){
                imageUri = result
            }
        }
         catch (e:Exception){
             Log.d("Image exception", "getImageUriFromDb: ${e.message}")
         }
        return imageUri
    }

    suspend fun saveImageUri(imageUri: Uri,currentUserid: String){
        val storageRef = FirebaseStorage.getInstance().getReference();
        val imagesRef: StorageReference = storageRef.child("profile_images")
        imagesRef.child(currentUserid).putFile(imageUri).await()

    }

    suspend fun getCurrentUserEmail(currentUserid: String) : String{
        val fAuth = Firebase.auth
        var userEmail = ""
        try {
           userEmail = fAuth.currentUser?.email.toString()
        }catch (e:Exception){
            Log.d("EMAIL EXCEPTION", "getCurrentUserEmail: ${e.message}")
        }
        return userEmail
    }

    @SuppressLint("Range")
    suspend fun getPhoneBook(context: Application, currentUserid: String) : MutableList<PhonebookContact> {
        val countryCode = getGetCountryCode(currentUserid)
        val contacts = mutableListOf<PhonebookContact>()
        var distinctContact = mutableListOf<PhonebookContact>()

            val cr = context.contentResolver

            try {
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
                    var number = query.getString(query.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                    number = if (number.startsWith("+")){
                        query.getString(query.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                    }
                    else{
                        "+$countryCode${query.getString(query.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))}"
                    }
                    contacts.add(PhonebookContact(id,name, number))
                    distinctContact = contacts
                }
            }
            query.close()
        }
        catch (e:Exception){
            Log.d("TAG", "getPhoneBook: ${e.message} ")
        }

        return distinctContact
    }

    suspend fun getGuardianList(currentUserid: String) : MutableList<PhonebookContact>{
         val database = Firebase.firestore
         val collectionReference = database.collection("Guardian Angels")
        var guardianAngelsList = mutableListOf<PhonebookContact>()
        try {
            val data = collectionReference.document(currentUserid).get().await().toObject(GuardianData::class.java)
            if (data!=null){
               guardianAngelsList = data.guardianInfo
            }
        }catch (e:Exception){
            Log.d("Guardian List Exception", "getGuardianList: ${e.message} ")
        }
       return guardianAngelsList
    }

    suspend fun getRegisteredGuardianAngels(currentUserid: String) : MutableList<Users>{
        val database = Firebase.firestore
        val collectionReference = database.collection("Guardian Angels")
        val usersCollection = database.collection("Users")
        var users = Users()
        val usersList = mutableListOf<Users>()
        try {
            val data = collectionReference.document(currentUserid).get().await().toObject(GuardianData::class.java)
            data?.let {
                it.guardianInfo.forEach { guardians ->
                    val querySnapshot = usersCollection.whereEqualTo("phoneNumber",guardians.number).get().await()
                    querySnapshot.forEach { queryDocSnapshot ->
                        users = queryDocSnapshot.toObject(Users::class.java)
                        usersList.add(users)
                    }
                }
            }
        }catch (e:Exception){
            Log.d("Registered Guardian Users Exception", "getGuardianList: ${e.message} ")
        }
        return usersList
    }

    suspend fun getNotificationSender(currentUserid: String) : String{
        val database = Firebase.firestore
        val usersCollection = database.collection("Users")
        var userName = ""
        try {
            val querySnapshot = usersCollection.whereEqualTo("currentUserId",currentUserid).get().await()

            querySnapshot.forEach { queryDocSnapshot ->
                val users = queryDocSnapshot.toObject(Users::class.java)
                userName = users.fullName
            }
        }catch (e:Exception){
            Log.d("Notification Sender UserName Exception", "getNotificationSender: ${e.message}")
        }
        return userName
    }

    suspend fun getUserSosText(currentUserid: String) : String{
        val database = Firebase.firestore
        val usersCollection = database.collection("Users")
        var sos = ""
        try {
            val querySnapshot = usersCollection.whereEqualTo("currentUserId",currentUserid).get().await()

            querySnapshot.forEach { queryDocSnapshot ->
                val users = queryDocSnapshot.toObject(Users::class.java)
                sos = users.userSOSText
            }
        }catch (e:Exception){
            Log.d("Notification Sender UserName Exception", "getNotificationSender: ${e.message}")
        }
        return sos
    }

    suspend fun saveOutgoingData(currentUserid: String, outgoingAlertDataList: MutableList<OutgoingAlertData>){
         val outgoingAlertDb = Firebase.database
         val myRef = outgoingAlertDb.getReference("OutgoingAlerts")
        myRef.child(currentUserid).setValue(outgoingAlertDataList)
    }

    suspend fun saveIncomingData(currentUserid: String, incomingAlertDataList: MutableList<IncomingAlertData>){
        val incomingAlertDb = Firebase.database
        val myRef = incomingAlertDb.getReference("IncomingAlerts")
        myRef.child(currentUserid).setValue(incomingAlertDataList)
    }

    suspend fun getCurrentUid():String{
        val fauth = Firebase.auth
        val currentUser = fauth.currentUser
        var currentUserId = ""
       if (currentUser!=null){
            currentUserId = currentUser.uid
       }
        return currentUserId
    }

    suspend fun getCurrentUser(): FirebaseUser? {
        val fauth = Firebase.auth
        return fauth.currentUser
    }

    suspend fun savePhoneNumber(phoneNumber: String, countryCode : String) : Boolean{
         val database = Firebase.firestore
         val usersCollection = database.collection("Users")
        var saved = false
        try {
            val snapshot =  usersCollection.whereEqualTo("currentUserId", getCurrentUid()).get().await()
            snapshot.forEach {
                val users = it.toObject(Users::class.java)
                users.phoneNumber = phoneNumber.filter { whiteSpace->
                    !whiteSpace.isWhitespace()
                }
                users.number = users.phoneNumber.filter { white->
                    !white.isWhitespace()
                }.takeLast(10)
                users.countryCode = countryCode

             usersCollection.document(it.id).set(users).await()
                saved = true
            }
        }catch (e:Exception){
            Log.d("Add Phone Number Exception", "savePhoneNumber: ${e.message}")
            saved = false
        }
    return saved
    }

    suspend fun saveGuardianList(checkedList: MutableList<PhonebookContact>) : Boolean{
        var saved = false
        try {
            val database = Firebase.firestore
            val collectionReference = database.collection("Guardian Angels")
            val data = collectionReference.document(getCurrentUid()).get().await()
            if (data!=null){
                collectionReference.document(getCurrentUid()).set(GuardianData(checkedList)).await()
                saved = true
            }
        }catch (e:Exception){
            Log.d("Save Guardian Exception", "saveGuardianList: ${e.message}")
        }
        return saved
    }

    suspend fun emptyGuardianList() : Boolean{
        val docData = GuardianData(mutableListOf())
        var deleted = false
        try {
            val database = Firebase.firestore
            val collectionReference = database.collection("Guardian Angels")
            collectionReference.document(getCurrentUid()).set(docData).await()
            deleted = true

        }catch (e:Exception){
            deleted = false
            Log.d("Empty Guardian List Exception", "emptyGuardianList:${e.message} ")
        }
        return deleted
    }

    suspend fun saveSosText(sosText: String) : Boolean{
        val database = Firebase.firestore
        var saved = false
        try {
            val usersCollection = database.collection("Users")
            val snapshot = usersCollection.whereEqualTo("currentUserId", getCurrentUid()).get().await()
            snapshot?.forEach {
                val users = it.toObject(Users::class.java)
                users.userSOSText = sosText
                usersCollection.document(it.id).set(users)
                saved = true
            }
        }catch (e:Exception){
            saved = false
            Log.d("Save Sos Exception", "saveSosText: ${e.message} ")
        }
        return saved
    }

    suspend fun changePhoneNumber(phoneNumber: String) : Boolean{
        val database = Firebase.firestore
        val usersCollection = database.collection("Users")
        var saved = false
        try {
            val snapshot =  usersCollection.whereEqualTo("currentUserId", getCurrentUid()).get().await()
            snapshot.forEach {
                val users = it.toObject(Users::class.java)
                users.phoneNumber = phoneNumber.filter { whiteSpace->
                    !whiteSpace.isWhitespace()
                }
                users.number = users.phoneNumber.filter { white->
                    !white.isWhitespace()
                }.takeLast(10)

                usersCollection.document(it.id).set(users).await()
                saved = true
            }
        }catch (e:Exception){
            Log.d("Change Phone Number Exception", "savePhoneNumber: ${e.message}")
            saved = false
        }
        return saved
    }

    suspend fun signInUser(email : String , password : String) : Boolean{
        val  fAuth = Firebase.auth
        var signInSuccess = false
        try {
         fAuth.signInWithEmailAndPassword(email,password).await()
            signInSuccess = true
        }catch (e:Exception){
            signInSuccess = false
            Log.d("Sign in exception", "signInUser: ${e.message} ")
        }
        return signInSuccess
    }

    suspend fun signOutUser(){
        val  fAuth = Firebase.auth
        try {
            fAuth.signOut()
        }catch (e:Exception){
            Log.d("Sign in exception", "signInUser: ${e.message} ")
        }
    }

}