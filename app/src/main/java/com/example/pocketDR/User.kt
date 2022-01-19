package com.example.pocketDR

data class User(//these names must match to the ones in Cloud Firestore
    val name: String = "",
    val email: String = "",
    val care_flag: Boolean = true
)