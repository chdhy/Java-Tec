package com.hunter.dynamic_proxy.room.annotation

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Query(
    val statement: String = ""
)