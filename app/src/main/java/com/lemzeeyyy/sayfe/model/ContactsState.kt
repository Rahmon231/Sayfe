package com.lemzeeyyy.sayfe.model

sealed class ContactsState {
    class Success(val contacts : List<PhonebookContact>): ContactsState()
    object Failure : ContactsState()
    object Empty : ContactsState()
}

sealed class GuardianState{
    class Success(val contacts : List<PhonebookContact>): GuardianState()
    class Failure(val exception: Exception) : GuardianState()
    object Empty : GuardianState()
    object Loading : GuardianState()
}

sealed class Resource<out R>{
    data class Success<out R>(val result: R) : Resource<R>()
    data class Failure(val exception: Exception): Resource<Nothing>()
    object Loading : Resource<Nothing>()
}

sealed class IncomingAlertState{
    class Success(val incomingDataList : List<IncomingAlertData>) : IncomingAlertState()
    class Failure(val exception: Exception) : IncomingAlertState()
    object Empty : IncomingAlertState()
    object Loading : IncomingAlertState()
}

sealed class OutgoingAlertState{
    class Success(val outgoingDataList : List<OutgoingAlertData>) : OutgoingAlertState()
    class Failure(val exception: Exception) : OutgoingAlertState()
    object Empty : OutgoingAlertState()
    object Loading : OutgoingAlertState()
}