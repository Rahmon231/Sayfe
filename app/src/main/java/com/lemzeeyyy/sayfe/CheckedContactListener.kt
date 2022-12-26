package com.lemzeeyyy.sayfe

import com.lemzeeyyy.sayfe.model.RecipientContact

interface CheckedContactListener {
    fun onContactClick(contacts : MutableList<RecipientContact>)
}