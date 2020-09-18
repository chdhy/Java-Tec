package com.hunter.generic

import com.hunter.generic.bean.Apple
import com.hunter.generic.bean.Fruit
import com.hunter.generic.bean.Poison

fun main() {
    reifiedGeneric<Fruit>()
    reifiedGeneric<Poison>()
}

fun genericMethod(t: List<out Apple>) {

}

fun genericMethod2(t: MutableList<in Fruit>) {

}

fun listStarProjection(t: MutableList<*>) {

}

inline fun <reified T> reifiedGeneric() {
    println(T::class.java.simpleName)
}

