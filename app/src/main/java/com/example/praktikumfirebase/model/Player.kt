package com.example.praktikumfirebase.model

data class Player(
    val id: String = "", // ID diambil dari key node Firebase
    val name: String = "",
    val photoUrl: String = "",
    val number: Int = 0,
    val description: String = ""
) {
    // Constructor default agar Firebase dapat menggunakan refleksi
    constructor() : this("", "", "", 0, "")
}
