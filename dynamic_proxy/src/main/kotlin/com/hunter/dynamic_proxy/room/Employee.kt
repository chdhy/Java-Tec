package com.hunter.dynamic_proxy.room

import com.hunter.dynamic_proxy.room.annotation.Table

@Table
data class Employee(
    val id: Int,
    val name: String,
    val age: Int,
    val address: String?,
    val salary: Float?
)