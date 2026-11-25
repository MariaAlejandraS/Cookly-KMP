package com.cookly.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** Cuenta local. La contraseña NUNCA se guarda: solo su sal y su huella (hash). */
@Entity(tableName = "users", indices = [Index(value = ["username"], unique = true)])
data class User(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val username: String,
    val salt: String,
    val passwordHash: String,
)
