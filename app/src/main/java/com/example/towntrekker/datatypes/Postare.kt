package com.example.towntrekker.datatypes

import com.google.firebase.storage.StorageReference

data class Postare(val id: String, val user: String, val numeUser: String, val numeLocatie: String, var adresaLocatie: String,
                   val tipLocatie: String, val categorieLocatie: String, var aprecieri: Int, var descriere: String,
                   var comentarii: List<Pair<String, String>>, var media: Boolean, var iconUser: Boolean, val tipRecenzie: String,
                   val scorRecenzie: Double, var lista_media: List<StorageReference> = listOf()) : java.io.Serializable