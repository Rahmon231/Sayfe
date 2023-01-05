package com.lemzeeyyy.sayfe.database

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences

class SharedPrefs {
    companion object{
        private var mSharedPref: SharedPreferences? = null

        fun init(context: Context) {
            if (mSharedPref == null) mSharedPref =
                context.getSharedPreferences(context.packageName, Activity.MODE_PRIVATE)
        }
        fun getBoolean(key: String?, defValue: Boolean): Boolean {
            //return mSharedPref!!.getBoolean(key, defValue)
            return mSharedPref?.let {
                it.getBoolean(key, defValue)
            }
                ?: false
        }
        fun putBoolean(key: String?, value: Boolean) {
            val prefsEditor = mSharedPref!!.edit()
            prefsEditor.putBoolean(key, value)
            prefsEditor.apply()
        }

    }


}