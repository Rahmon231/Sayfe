package com.lemzeeyyy.sayfe.model

data class GuardianData(var guardianInfo : MutableList<RecipientContact> = mutableListOf() )

data class GuardianAngelData (val id : String = "", val name : String = "", var number : String= "", var phoneNumber : String = "", var isChecked : Boolean = false)
