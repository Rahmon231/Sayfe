package com.lemzeeyyy.sayfe

import com.lemzeeyyy.sayfe.model.PhonebookContact
interface CheckedContactListener {
    fun onContactClick(contacts : MutableList<PhonebookContact>)
}