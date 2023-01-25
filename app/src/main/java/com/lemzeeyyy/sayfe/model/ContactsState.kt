package com.lemzeeyyy.sayfe.model

sealed class ContactsState {
    class Success(val contacts : List<RecipientContact>): ContactsState()
    object Failure : ContactsState()
    object Empty : ContactsState()
}