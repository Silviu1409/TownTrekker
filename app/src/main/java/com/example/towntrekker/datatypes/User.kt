package com.example.towntrekker.datatypes

data class User(val uid: String, val email: String, var alias: String, var parola: String = "") : java.io.Serializable