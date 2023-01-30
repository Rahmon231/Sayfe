package com.lemzeeyyy.sayfe.model

data class RecipientContact (val id : String = "", val name : String = "", var number : String= "", var phoneNumber : String = "")

data class PhonebookContact (val id : String = "", val name : String = "", var number : String= "", var phoneNumber : String = "", var isChecked : Boolean = false)

