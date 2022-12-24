package com.lemzeeyyy.sayfe

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.lemzeeyyy.sayfe.model.GuardianData

class GuardianAngelsViewModel : ViewModel() {
    private val database = Firebase.firestore
    private lateinit var fAuth: FirebaseAuth
    private val collectionReference = database.collection("Guardian Angels")

      private val _guardianLiveData = MutableLiveData<GuardianData>()
    val guardianLiveData : LiveData<GuardianData> get() = _guardianLiveData

     fun getGuardianAngelsListToDb(currentUserid : String){

        collectionReference.document(currentUserid)
            .get()
            .addOnSuccessListener {

              val data =  it.toObject(GuardianData::class.java)
                data?.let {guardianData->
                    _guardianLiveData.value = guardianData
                }
            }
            .addOnFailureListener {

            }
    }

}
