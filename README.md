   * [动态代理](#动态代理)
      * [What](#what)
      * [Why](#why)
      * [Where](#where)
      * [How](#how)
         * [JDK 动态代理](#jdk-动态代理)
         * [CGLIB 动态代理](#cglib-动态代理)
      * [举一](#举一)

# 动态代理


## What

代理：代为处理 因原对象不适合，或不方便直接处理时委托他人代为处理

**动态**代理是在**运行期**对对象进行代理的一种技术，对应的是静态代理，需要预先定义好委托对象并设置好代理关系

## Why

动态代理相对于静态代理更加灵活，可以编写更少的代码，不必为每个对象编写委托对象

动态代理可以以方法为单位编写代理处理，不必编写委托对象，也没有必要实现每个代理方法

动态代理可以批量处理代理对象的方法，因为每个方法都会走到统一的入口方便处理

## Where

动态代理通常应用在 批量校验（鉴权，校验参数，校验前置条件），懒加载，智能引用（昂贵对象的无感回收和重建），模板代码生成（Retrofit）

## How

![代理模式](/Users/dhy/IdeaProjects/Java-Tec/resource/代理.png)



<center>代理模式<center/>



下面介绍两种常用的动态代理方式

### JDK 动态代理

定义基础接口

```kotlin
interface Foo {
    fun foo(): String
}
```

实现需要被代理的接口

```kotlin
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
```

使用代理

```kotlin
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

// ProxyHandler
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
```

执行结果

```
call foo at FooImpl
com.sun.proxy.$Proxy0
method executed in 501
call foo at FooImpl
```

来看一下动态代理是如何达成的：

动态代理生成的类

```kotlin
// 截取部分类
public final class $Proxy0 extends Proxy implements Foo {
		private static Method m3; 
  
    public final String foo() throws  {
        try {
            return (String)super.h.invoke(this, m3, (Object[])null);
        } catch (RuntimeException | Error var2) {
            throw var2;
        } catch (Throwable var3) {
            throw new UndeclaredThrowableException(var3);
        }
    }
  
    static {
        m3 = Class.forName("com.hunter.dynamic_proxy.jdk.Foo").getMethod("foo");
    }
}
```

这是 proxy.newProxyInstance() 返回的实际类，调用 foo 方法时会执行 ```(String)super.h.invoke(this, m3, (Object[])null)```，这个 h 是继承的 proxy 类内部的的 InvocationHandler，这个 handler 就是上面 newProxyInstance 调用是传入的 ProxyHandler，最终也就走到了自定义的 InvocationHandler 的 invoke 方法中去

动态代理的核心流程就此清晰：

1. newProxyInstance(动态生成继承 Proxy 实现接口的类并加载此类，反射返回此类的实例)
2. 调用代理类的方法
3. 进入 InvocationHandler 反射处理该方法

可以看到这里动态代理生成的方法继承了 Proxy，因为 Java 不能多继承，所以动态代理只能代理接口，对象的代理则需要 cglib



### CGLIB 动态代理

定义基础类

```kotlin
open class Foo {
    open fun foo(name: String): String {
        return "hello $name"
    }
}
```

代理该类

```kotlin
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
```

运行结果

```
CGLIB debugging enabled, writing to './' // 由 cglib 库打印
call toString
-1
true
before call foo
call foo(): hello yeah
after call foo
```

看一下为什么 cglib 能代理类：

cglib 生成的代理类

```kotlin
public class Foo$$EnhancerByCGLIB$$6ef3ac0c extends Foo implements Factory {
		private static final Method CGLIB$foo$0$Method; // foo 方法
    private MethodInterceptor CGLIB$CALLBACK_0; // 方法拦截器，enhancer.setCallback
  
    static {
        CGLIB$STATICHOOK1();
    }
  
    static void CGLIB$STATICHOOK1() {
        Class var0 = Class.forName("com.hunter.dynamic_proxy.cglib.Foo$$EnhancerByCGLIB$$6ef3ac0c");
        CGLIB$foo$0$Method = ReflectUtils.findMethods(new String[]{"foo", "(Ljava/lang/String;)Ljava/lang/String;"}, (var1 = Class.forName("com.hunter.dynamic_proxy.cglib.Foo")).getDeclaredMethods())[0];
    }
  
    public final String foo(String var1) {
        MethodInterceptor var10000 = this.CGLIB$CALLBACK_0;
        if (var10000 == null) {
            CGLIB$BIND_CALLBACKS(this);
            var10000 = this.CGLIB$CALLBACK_0;
        }
        // 从 foo 方法进入拦截器执行
        return var10000 != null ? (String)var10000.intercept(this, CGLIB$foo$0$Method, new Object[]{var1}, CGLIB$foo$0$Proxy) : super.foo(var1);
    }
}
```

Enhancer 返回的对象为生成的代理类，foo 方法会进入设置的 callback 对应的处理方法。因为 cglib 生成的类没有继承 proxy 类，而是继承了被代理类，所以可以代理普通类。因为 static 和 final 的方法不能被继承，所以 cglib 不能代理 static 和 final 的方法



## 举一

以动态代理来模仿 Android 平台的数据库工具 room 库为例，看一下使用方法：

```kotlin
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
annotation class Table(
    val name: String = ""
)

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
annotation class Dao

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Query(
    val statement: String = ""
)

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Insert
```

<center>用到的部分注解<center/>

```kotlin
@Table
data class Employee(
    val id: Int,
    val name: String,
    val age: Int,
    val address: String?,
    val salary: Float?
)
```

<center>数据表的定义<center/>

```kotlin
@Dao
interface EmployeeDao {

    @Insert
    fun create(employees: List<Employee>)

    @Query
    fun getAllEmployees(): List<Employee>

}
```

<center>DAO的定义<center/>

```kotlin
fun room() {
    val employeeDao = Room.create(EmployeeDao::class.java) // 动态代理
    employeeDao.create(
        listOf(
            Employee(5, "tom", 18, "foo", 15000f),
            Employee(6, "cook", 19, "bar", 25000f)
        )
    )
    val allEmployees = employeeDao.getAllEmployees()
    println(allEmployees.joinToString("\n"))
}
```

<center>Room使用<center/>

```
Employee(id=5, name=tom, age=18, address=foo, salary=15000.0)
Employee(id=6, name=cook, age=19, address=bar, salary=25000.0)
```

<center>执行结果<center/>

来看看对于 dao 类代理的核心代码：

```kotlin
fun <reified T> create(clazz: Class<T>): T {
    return Proxy.newProxyInstance(clazz.classLoader, arrayOf(clazz), InvocationHandler { _, method, args ->
        when (val annotation = method.annotations[0]) { // 根据方法的注解来和方法的定义做处理
            is Query -> query(method, annotation)
            is Insert -> insert(args)
            else -> Unit
        }
    }) as T
}
```

<center>代理入口<center/>

```kotlin
// 这里使用 sqlite-jdbc 库作为数据库工具
// implementation "org.xerial:sqlite-jdbc:3.32.3.2"

fun query(method: Method, annotation: Query): Any {
    /** 省略部分代码 */

  	// 根据代理的方法定义构建查询语句
    val queryStr = if (annotation.statement.isEmpty()) getDefaultQueryStr(actualType, declaredFields)
    else annotation.statement

    // 执行查询并解析为对应返回值
    val queryResult = getQueryResult(queryStr, declaredFields, actualType)

    return if (rawType == null /*单参数，非集合参数*/) queryResult[0] else queryResult
}

private fun getQueryResult(queryStr: String, declaredFields: Array<Field>, actualType: Type): MutableList<Any> {
    // 执行查询
    val resultSet = statement.executeQuery(queryStr)
    while (resultSet.next()) {
        val fieldValues = declaredFields.map {
            // 解析结果
            when (it.type) {
                String::class.java -> resultSet.getString(it.name)
                Int::class.java, Integer::class.java -> resultSet.getInt(it.name)
                Float::class.java, java.lang.Float::class.java -> resultSet.getFloat(it.name)
                Long::class.java, java.lang.Long::class.java -> resultSet.getLong(it.name)
                else -> Unit
            }
        }
        // 返回为指定类型
        val constructor = Class.forName(actualType.typeName)
            .getDeclaredConstructor(*declaredFields.map { it.type }.toTypedArray())
        val element = constructor.newInstance(*fieldValues.toTypedArray())
        result.add(element)
    }
}
```

<center>query核心代码<center/>

insert 方法类似，主体都是根据方法的定义和注解拼出 sql 语句执行，再解析结果包装返回。至此，简单的 room 核心功能解析完成。