package com.hunter.dynamic_proxy.cglib

open class Foo {
    open fun foo(name: String): String {
        return "hello $name"
    }
}