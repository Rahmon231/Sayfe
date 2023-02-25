package com.lemzeeyyy.sayfe.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.lemzeeyyy.sayfe.model.Resource
import com.lemzeeyyy.sayfe.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    val currentUser : FirebaseUser?
    get() = authRepository.currentUser

    private val _loginFlow = MutableLiveData<Resource<FirebaseUser>?>(null)
    val loginFLow : LiveData<Resource<FirebaseUser>?> = _loginFlow

    private val _signupFlow = MutableLiveData<Resource<FirebaseUser>?>(null)
    val signupFlow : LiveData<Resource<FirebaseUser>?> = _signupFlow

    init {
        if (currentUser!=authRepository.currentUser){
            _loginFlow.value = Resource.Success(authRepository.currentUser!!)
        }
    }

    fun logInUser(email : String, password : String){
        _loginFlow.value = Resource.Loading
        viewModelScope.launch {
           val result = authRepository.loginUser(email,password)
            _loginFlow.value = result
        }

    }

    fun signup(email : String, password : String, name : String){
        _signupFlow.value = Resource.Loading
        viewModelScope.launch {
            val result = authRepository.signUpUser(name,email,password)
            _signupFlow.value = result
        }

    }

    fun logout(){
        authRepository.signOutUSer()
        _signupFlow.value = null
        _loginFlow.value = null
    }
}