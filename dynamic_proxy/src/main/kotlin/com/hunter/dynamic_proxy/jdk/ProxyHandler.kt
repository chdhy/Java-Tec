package com.hunter.dynamic_proxy.jdk

import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method

class ProxyHandler internal constructor(private val target: Any) : InvocationHandler {

    @Throws(Throwable::class)
    override fun invoke(proxy: Any, method: Method, args: Array<Any>?): Any {
        val currentTimeMillis = System.currentTimeMillis()
        val result = method.invoke(target)
        val spendTime = System.currentTimeMillis() - currentTimeMillis
        println("method executed in $spendTime")
        return result
    }

}