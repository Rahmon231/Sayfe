package com.lemzeeyyy.sayfe.model

sealed class ContactsState {
    class Success(val contacts : List<PhonebookContact>): ContactsState()
    object Failure : ContactsState()
    object Empty : ContactsState()
}

sealed class GuardianState{
    class Success(val contacts : List<PhonebookContact>): GuardianState()
    object Failure : GuardianState()
    object Empty : GuardianState()
}