package com.lemzeeyyy.sayfe

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.lemzeeyyy.sayfe.HomeFragment.Companion.recipientContacts
import com.lemzeeyyy.sayfe.model.RecipientContact

class PhonebookViewModel : ViewModel() {



}




class PhonebookViewModelFactory() :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PhonebookViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PhonebookViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
