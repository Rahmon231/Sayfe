package com.lemzeeyyy.sayfe.model

data class RecipientContact (val id : String = "", val name : String = "", var number : String= "", var phoneNumber : String = "")

data class PhonebookContact (var id : String = "", var name : String = "", var number : String= "", var phoneNumber : String = "", var isChecked : Boolean = false)

