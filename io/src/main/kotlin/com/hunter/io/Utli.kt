package com.hunter.io

fun performance(runnable: () -> Any): Long {
    val startTimeMillis = System.currentTimeMillis()
    runnable()
    return System.currentTimeMillis() - startTimeMillis
}