package com.hunter.dynamic_proxy.jdk

class FooImpl : Foo {
    override fun foo(): String {
        try {
            Thread.sleep(500)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        return "call foo at FooImpl"
    }
}