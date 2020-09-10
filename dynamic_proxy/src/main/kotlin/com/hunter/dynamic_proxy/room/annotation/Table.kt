package com.hunter.dynamic_proxy.room.annotation

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
annotation class Table(
    val name: String = ""
)