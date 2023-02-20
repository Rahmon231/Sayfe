package com.lemzeeyyy.sayfe.viewmodels

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.lemzeeyyy.sayfe.model.*
import com.lemzeeyyy.sayfe.repository.SayfeRepository
import kotlinx.coroutines.launch

const val BUSY = 1
const val PASSED = 2
const val EMPTY = 3
const val FAILED = 4

class MainActivityViewModel(application: Application) : AndroidViewModel(application) {

    private val fAuth = Firebase.auth
    private var currentUser = fAuth.currentUser
    private var currentUserid = currentUser?.uid

    private val _guardianLiveData = MutableLiveData<GuardianData>()
    val guardianLiveData: LiveData<GuardianData> get() = _guardianLiveData

    private val _triggerApp = MutableLiveData<Boolean>()
    val triggerApp : LiveData<Boolean> get() = _triggerApp

    private var _contactStatus = MutableLiveData<Int>()
    val contactStatus: LiveData<Int> get() = _contactStatus

    private var _guardianAngelsStatus = MutableLiveData<Int>()
    val guardianAngelsStatus: LiveData<Int> get() = _guardianAngelsStatus

    private var _incomingDataStatus = MutableLiveData<Int>()
    val incomingDataStatus: LiveData<Int> get() = _incomingDataStatus

    private var _outgoingDataStatus = MutableLiveData<Int>()
    val outgoingDataStatus: LiveData<Int> get() = _outgoingDataStatus

    private val _userImageUri = MutableLiveData<Uri>()
    val userImageUri : LiveData<Uri> get() = _userImageUri

    private var _phoneNumber = MutableLiveData<String>()
    val phoneNumber: LiveData<String> get() = _phoneNumber

    private val _userContactsLiveData = MutableLiveData<MutableList<PhonebookContact>?>()
    val userContactsLiveDataList : LiveData<MutableList<PhonebookContact>?> get() = _userContactsLiveData

    private val _users = MutableLiveData<MutableList<Users>?>()
    val users : LiveData<MutableList<Users>?> get() = _users

    private val _outgoingAlertListLiveData = MutableLiveData<MutableList<OutgoingAlertData>?>()
    val outgoingAlertListLiveData : LiveData<MutableList<OutgoingAlertData>?> get() = _outgoingAlertListLiveData

    private val _incomingAlertListLiveData = MutableLiveData<MutableList<IncomingAlertData>?>()
    val incomingAlertListLiveData : LiveData<MutableList<IncomingAlertData>?> get() = _incomingAlertListLiveData

    private val _isRegistered = MutableLiveData<Boolean>()
    val isRegistered: LiveData<Boolean> get() = _isRegistered

    init {

        viewModelScope.launch {

            _userImageUri.value = currentUserid?.let { SayfeRepository.getImageUriFromDb(it) }

            _users.value = currentUserid?.let { SayfeRepository.getRegisteredGuardianAngels(it) }

           // _outgoingAlertListLiveData.value = currentUserid?.let { SayfeRepository.getOutgoingAlertList(it) }

           // _incomingAlertListLiveData.value = currentUserid?.let { SayfeRepository.getIncomingAlertList(it) }

        }
    }

    fun updateShouldTriggerApp(navigate : Boolean){
        _triggerApp.value = navigate
    }

    fun checkDuplicateRegisteredNumber(phoneNumber: String){
        viewModelScope.launch {
            _isRegistered.value = SayfeRepository.checkDuplicateRegisteredNumber(phoneNumber)
        }
    }

    fun getPhoneBook(){
        viewModelScope.launch {
            _userContactsLiveData.value =
                currentUserid?.let { SayfeRepository.getPhoneBook(getApplication(), it) }
            if (_userContactsLiveData.value?.isNotEmpty() == true){
                _contactStatus.value = PASSED
            }else{
                _contactStatus.value = EMPTY
            }
        }
    }

    fun getGuardianAngels(){
        viewModelScope.launch {
            try {
                _guardianAngelsStatus.value = BUSY
                _guardianLiveData.value = currentUserid?.let {
                    SayfeRepository.getGuardianAngelsListFromDb(it) }
                if (_guardianLiveData.value?.guardianInfo?.isEmpty() == true){
                    _guardianAngelsStatus.value = EMPTY
                }else{
                    _guardianAngelsStatus.value = PASSED
                }
            }catch (e:Exception){
                _guardianAngelsStatus.value = FAILED
            }

        }
    }

    fun incomingDataList(){
        _incomingDataStatus.value = BUSY
        viewModelScope.launch {
            try {
                _incomingAlertListLiveData.value = currentUserid?.let { SayfeRepository.getIncomingAlertList(it) }
                if (_incomingAlertListLiveData.value?.isEmpty() == true){
                    _incomingDataStatus.value = EMPTY
                }else{
                    _incomingDataStatus.value = PASSED
                }
            }catch (e:Exception){
                _incomingDataStatus.value = FAILED
            }

        }
    }

    fun outgoingDataList(){
        _outgoingDataStatus.value = BUSY
        viewModelScope.launch {
            try {
                _outgoingAlertListLiveData.value = currentUserid?.let { SayfeRepository.getOutgoingAlertList(it) }
                if (_outgoingAlertListLiveData.value?.isEmpty() == true){
                    _outgoingDataStatus.value = EMPTY
                }else{
                    _outgoingDataStatus.value = PASSED
                }
            }catch (e:Exception){
                _outgoingDataStatus.value = FAILED
            }

        }
    }

}
