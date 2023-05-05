package com.example.towntrekker.datatypes


data class Recomandare(val id: String, val logo: String, val nume: String, val adresa: String, val rating: String,
                       val tip: String, val categorie: String, val descriere: String, val geoLocatie: String) : java.io.Serializable