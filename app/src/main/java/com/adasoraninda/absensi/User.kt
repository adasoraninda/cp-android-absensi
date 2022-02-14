package com.adasoraninda.absensi

import java.io.Serializable


data class User(
    val name: String? = null,
    val time: String? = null,
) : Serializable