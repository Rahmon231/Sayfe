package com.lemzeeyyy.sayfe.viewmodels

import android.app.Application
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.lemzeeyyy.sayfe.model.*
import com.lemzeeyyy.sayfe.repository.SayfeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

const val BUSY = 1
const val PASSED = 2
const val EMPTY = 3
const val FAILED = 4

@HiltViewModel
class MainActivityViewModel @Inject constructor(private val repository: SayfeRepository) : ViewModel() {

    private val _guardianLiveData = MutableLiveData<GuardianData>()
    val guardianLiveData: LiveData<GuardianData> get() = _guardianLiveData

    private val _currentUserid = MutableLiveData<String>()
    val currentUserID : LiveData<String> get() = _currentUserid

    private val _currentUser = MutableLiveData<FirebaseUser>()
    val currentUser : LiveData<FirebaseUser> get() = _currentUser

    private val _triggerApp = MutableLiveData<Boolean>()
    val triggerApp : LiveData<Boolean> get() = _triggerApp

    private val _signIn = MutableLiveData<Boolean>()
    val signIn : LiveData<Boolean> get() = _signIn

    private val _saveNumber = MutableLiveData<Boolean>()
    val saveNumber : LiveData<Boolean> get() = _saveNumber

    private var _contactStatus = MutableLiveData<Int>()
    val contactStatus: LiveData<Int> get() = _contactStatus

    private var _signInStatus = MutableLiveData<Int>()
    val signInStatus: LiveData<Int> get() = _signInStatus

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

    private var _userEmail= MutableLiveData<String>()
    val userEmail: LiveData<String> get() = _userEmail

    private var _userName= MutableLiveData<String>()
    val userName: LiveData<String> get() = _userName

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

    fun getCurrentUserId(){
        viewModelScope.launch {
            _currentUserid.value = repository.getCurrentUid()
        }

    }

    fun getCurrentUser(){
        viewModelScope.launch {
            _currentUser.value = repository.getCurrentUser()
        }
    }

    fun savePhoneNumber(phoneNumber: String, countryCode : String){
        viewModelScope.launch {
            _saveNumber.value = repository.savePhoneNumber(phoneNumber, countryCode)
        }
    }

    fun getImageUriFromDb(currentUserId: String){
        viewModelScope.launch {
            _userImageUri.value = repository.getImageUriFromDb(currentUserId)
        }

    }

    fun getUserName(){
        viewModelScope.launch {
            _userName.value = repository.getNotificationSender(repository.getCurrentUid())
        }

    }

    fun getUserEmail(){
        viewModelScope.launch {
            _userEmail.value = repository.getCurrentUserEmail(repository.getCurrentUid())
        }

    }

    fun signInUser(email : String, password : String){
        viewModelScope.launch {
            _signInStatus.value = BUSY
            _signIn.value = repository.signInUser(email,password)
            if (_signIn.value == true){
                _signInStatus.value = PASSED
            }else{
                _signInStatus.value = FAILED
            }
        }
    }

    fun getUserPhoneNumber(){
        viewModelScope.launch {
            _phoneNumber.value = repository.getUserPhone(repository.getCurrentUid())
        }
    }

    fun signOutUser(){
        viewModelScope.launch {
            repository.signOutUser()
        }
    }

    fun updateShouldTriggerApp(navigate : Boolean){
        _triggerApp.value = navigate
    }

    fun checkDuplicateRegisteredNumber(phoneNumber: String){
        viewModelScope.launch {
            _isRegistered.value = repository.checkDuplicateRegisteredNumber(phoneNumber)
        }
    }

    fun getPhoneBook(context : Application){
        viewModelScope.launch {
            _userContactsLiveData.value = repository.getPhoneBook(context, repository.getCurrentUid())
            if (_userContactsLiveData.value?.isNotEmpty() == true){
                _contactStatus.value = PASSED
            }else{
                _contactStatus.value = EMPTY
                 }
        }
    }

    fun getRegisteredGuardians(){
        viewModelScope.launch {
            _users.value = repository.getRegisteredGuardianAngels(repository.getCurrentUid())
        }

    }

    fun getGuardianAngels(){
        viewModelScope.launch {
            try {
                _guardianAngelsStatus.value = BUSY
                _guardianLiveData.value =  repository.getGuardianAngelsListFromDb(repository.getCurrentUid())
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
                _incomingAlertListLiveData.value =  repository.getIncomingAlertList(repository.getCurrentUid())
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
                _outgoingAlertListLiveData.value =  repository.getOutgoingAlertList(repository.getCurrentUid())
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

    fun saveImageUri(imageUri : Uri, currentUserId : String){
        viewModelScope.launch {
            repository.saveImageUri(imageUri,currentUserId)
        }

    }

    fun saveOutgoingData(currentUserId: String, outgoingAlertDataList: MutableList<OutgoingAlertData>){
        viewModelScope.launch {
            repository.saveOutgoingData(currentUserId,outgoingAlertDataList)
        }
    }

    fun saveIncomingData(currentUserId: String, incomingAlertDataList: MutableList<IncomingAlertData>){
        viewModelScope.launch {
            repository.saveIncomingData(currentUserId,incomingAlertDataList)
        }
    }

}
