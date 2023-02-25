package com.lemzeeyyy.sayfe.repository

import com.google.firebase.auth.FirebaseUser
import com.lemzeeyyy.sayfe.model.Resource

interface AuthRepository {
    val currentUser : FirebaseUser?
    suspend fun loginUser(email : String,password : String): Resource<FirebaseUser>
    suspend fun signUpUser(name: String, email: String,password: String): Resource<FirebaseUser>
    fun signOutUSer()

}