package com.lemzeeyyy.sayfe.model

sealed class ContactsState {
    class Success(val contacts : List<PhonebookContact>): ContactsState()
    object Failure : ContactsState()
    object Empty : ContactsState()
}