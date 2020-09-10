package com.hunter.dynamic_proxy

import com.hunter.dynamic_proxy.jdk.Foo
import com.hunter.dynamic_proxy.jdk.FooImpl
import com.hunter.dynamic_proxy.jdk.ProxyHandler
import com.hunter.dynamic_proxy.room.Employee
import com.hunter.dynamic_proxy.room.EmployeeDao
import com.hunter.dynamic_proxy.room.Room
import net.sf.cglib.proxy.Enhancer
import net.sf.cglib.proxy.MethodInterceptor
import java.lang.reflect.Proxy

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        jdk()
        cglib()
        room()
    }

    private fun jdk() {
        System.getProperties()["sun.misc.ProxyGenerator.saveGeneratedFiles"] = "true" // 将动态生成的类文件写入文件中
        val target = FooImpl()
        println(target.foo())

        val proxyInstance = Proxy.newProxyInstance(
            Foo::class.java.classLoader, arrayOf<Class<*>>(Foo::class.java),
            ProxyHandler(target)
        ) as Foo
        println(proxyInstance.javaClass.canonicalName)
        println(proxyInstance.foo())
    }

    private fun cglib() {
        System.getProperties()["cglib.debugLocation"] = "./" // 指定动态生成的类的文件保存位置

        val enhancer = Enhancer() // 代理增强器
        enhancer.setSuperclass(com.hunter.dynamic_proxy.cglib.Foo::class.java) // 设置代理类
        // 代理处理器 lambda 表达式
        enhancer.setCallback(MethodInterceptor { obj, method, args, proxy ->
            when {
                method.name == "toString" -> "call toString"
                method.returnType == Int::class.java -> -1
                method.name == "foo" -> {
                    println("before call foo")
                    val result = proxy.invokeSuper(obj, args)
                    println("call foo(): $result")
                    println("after call foo")
                    result
                }
                else -> true
            }
        })
        // 得到委托类
        val foo = enhancer.create() as com.hunter.dynamic_proxy.cglib.Foo
        println(foo.toString())
        println(foo.hashCode())
        println(foo == Any())
        foo.foo("yeah")
    }

    private fun room() {
        val employeeDao = Room.create(EmployeeDao::class.java)
        employeeDao.create(
            listOf(
                Employee(5, "tom", 18, "foo", 15000f),
                Employee(6, "cook", 19, "bar", 25000f)
            )
        )
        val allEmployees = employeeDao.getAllEmployees()
        println(allEmployees.joinToString("\n"))
    }
}