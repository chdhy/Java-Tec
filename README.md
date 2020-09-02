   * [APT:注解处理工具 annotation process tool](#apt注解处理工具-annotation-process-tool)
      * [What](#what)
      * [Why](#why)
      * [Where](#where)
      * [How](#how)
         * [举一](#举一)
      * [Tips](#tips)
      * [Links](#links)

# APT:注解处理工具 annotation process tool


## What

APT: 注解处理工具（Annotation **Process** Tool），是`javac`的一个工具，用来在编译时扫描和处理注解

AP: 注解处理器 （Annotation **Processor**），是 ```javax.annotation.processing``` 包和```javax.lang.model```包中的一套处理注解的工具集，用来实际处理注解，由 APT 调用

## Why

​	注解处理工具搭配注解的特性可以做到配置与代码更加内聚，便于维护，编写的代码更加 declarative，从逻辑上更内聚，代码编写上更 SOC。 借助注解和注解处理器能够生成的辅助代码让配置更加简单，减少模板代码编写量（**关心做什么，分离怎么做**）

## Where

​	目前典型的使用场景是数据库 ORM 库（Room），RPC 库（Retrofit），DI库 （ButterKnife）等

## How

![注解module结构图](https://raw.githubusercontent.com/chdhy/Java-Tec/apt/resource/apt-module-struct.svg)

<center>典型注解module依赖图</center>	



在 annotation module 中定义注解

```kotlin
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.SOURCE)
annotation class BindView(
    val resId: Int = 0
)
```

在 annotation processor module 中实现Processor：

通常继承 AbstractProcessor

1. 注册 Processor

手动注册：在 res/META-INF/service 目录下添加文件 ```javax.annotation.processing.Processor``` ，文件中添加注解处理器类的 ```CanonicalName```

使用注解注册：

添加 google 注解处理器 ```kapt "com.google.auto.service:auto-service:1.0-rc7"```

给注解处理器加上 ```@AutoService(Processor::class)```

2. 声明支持处理的注解

覆写方法：```getSupportedAnnotationTypes```，返回注解的```CanonicalName```

或使用注解声明：```@SupportedAnnotationTypes(CanonicalNames)```

3. 声明兼容的 Java 版本

覆写方法：```getSupportedSourceVersion()```，返回```SourceVersion.latest()```

或使用注解声明：```SupportedSourceVersion(SourceVersion.RELEASE_8)```

4. 覆写 process 方法：

```kotlin
    override fun process(annotations: MutableSet<out TypeElement>, roundEnv: RoundEnvironment): Boolean {
        // do process
        return false // false表示独占处理，处理完这个注解后不会交给其他 Processor 再处理
    }	
```

这里介绍一下在处理注解的过程中会用到的几个工具：


|         工具         |                          用途                          |
| :------------------: | :----------------------------------------------------: |
| ProcessingEnviroment |      处理注解的工具集，可以获取到接下来的几个工具      |
|       Messager       | 用来输出日志的工具，以良好的格式打印出更多的上下文信息 |
|        Types         |            用来操作类型（TypeMirror）的工具            |
|       Elements       |             用来操作节点（Element）的工具              |
|        Filer         |      用来生成新的 class，source，或辅助文件的工具      |
|   RoundEnvironment   |             获取当前轮处理上下文信息的工具             |

下面介绍具体如何处理注解，生成文件



### 举一

![butterknife-logo](https://raw.githubusercontent.com/chdhy/Java-Tec/apt/resource/butterknife-logo.png)

以 ButterKnife 原理为例：

```kotlin
private val bindViewMap: MutableMap<TypeElement, List<Element>> = mutableMapOf()
private val enclosingElements: MutableSet<TypeElement> = mutableSetOf()

override fun process(annotations: MutableSet<out TypeElement>, roundEnv: RoundEnvironment): Boolean {
    if (annotations.isNullOrEmpty()) return false // 注解处理器会处理多轮，但后续的处理可能没有实际的注解需要处理
    messager.printMessage(Diagnostic.Kind.NOTE, "real processing-----------------------")

    enclosingElements.clear()
    analyzeAnnotation(roundEnv) // 分析需要处理的注解
    enclosingElements.forEach {
        createAnnotationFile(it) // 以注解所在的类为单位生成新的文件
    }

    return false
}
```

<center>process方法<center/>

```kotlin
private fun analyzeAnnotation(roundEnv: RoundEnvironment) {
    val bindView = roundEnv.getElementsAnnotatedWith(BindView::class.java)
    val groupedBindView = bindView.groupBy { it.enclosingElement as TypeElement }
    groupedBindView.forEach { bindViewMap[it.key] = it.value }
    enclosingElements.addAll(groupedBindView.keys)
}
```

<center>analyzeAnnotation方法<center/>

```kotlin
private fun createAnnotationFile(element: TypeElement) {
    val sourceBo = createSourceBO(element) // 获取创建文件需要用到的填充信息

    // write file
    val kotlinFile = filer.createResource(StandardLocation.SOURCE_OUTPUT, sourceBo.packageStr, sourceBo.fileName)
    val kotlinFileWriter = kotlinFile.openWriter()
    kotlinFileWriter.write(fillFileTemplate(sourceBo))
    kotlinFileWriter.close()
}
```

<center>createAnnotationFile方法<center/>

```kotlin
// 获取创建文件需要用到的填充信息
private fun createSourceBO(element: TypeElement): SourceBO {
    val packageStr = elements.getPackageOf(element).toString()
    val originalClassName = element.simpleName.toString()
    val targetClassSimpleName = element.simpleName.toString()
    val targetClassQualifiedName = element.qualifiedName.toString()
    val className = "${originalClassName}_ButterKnife"
    val fileName = "${className}.kt"
    val targetVar = "target"

    val importCode = bindViewMap[element]?.toSet()?.joinToString(separator = "\n") {
        "import ${it.asType()}"
    } ?: ""

    val bindViewCode = bindViewMap[element]?.joinToString(separator = "\n       ") {
        val resId = it.getAnnotation(BindView::class.java).resId
        "target.${it.simpleName} = ${it.asType().toString().split(".").last()}($resId)"
    } ?: ""

    val onClickCode = onClickMap[element]?.joinToString(separator = "\n       ") {
        val name = it.getAnnotation(OnClick::class.java).name
        "$targetVar.$name.setOnclickListener{ $targetVar.${it.simpleName}() }"
    } ?: ""

    return SourceBO(
        fileName, packageStr, importCode, className, targetClassQualifiedName,
        targetClassSimpleName, targetVar, bindViewCode, onClickCode
    )
}
```

<center>createSourceBO方法<center/>

```kotlin
// 写文件的内容，将 sourceBO 填充到模板里
private fun fillFileTemplate(sourceBo: SourceBO): String {
    return """|package ${sourceBo.packageStr}
                |
                |import ${sourceBo.targetClassQualifiedName}
                |${sourceBo.importCode}
                |
                |class ${sourceBo.className} {
                |   
                |   lateinit var ${sourceBo.targetVar}: ${sourceBo.targetClassSimpleName}
                |   
                |   fun bind(${sourceBo.targetVar}: ${sourceBo.targetClassSimpleName}){
                |       this.${sourceBo.targetVar} = ${sourceBo.targetVar}
                |       bindView()
                |       onClick()
                |   }
                |
                |   fun bindView() {
                |       ${sourceBo.bindViewCode}
                |   }
                |   
                |   fun onClick(){
                |       ${sourceBo.onClickCode}
                |   }
                |
                |}"""
        .trimMargin()
}
```

<center>fillFileTemplate方法<center/>

使用注解：

```groovy
// 添加注解依赖
implementation project(':butterknife-lib')
// 配置注解处理器
kapt project(':apt')
```

<center>添加依赖<center/>

```kotlin
class Activity1 {

    @BindView(1)
    lateinit var textView: TextView

    @OnClick("textView")
    fun foo() {
        println("Activity1 $textView onClick")
    }

    fun onCreate() {
        ButterKnife.inject(this)
        println(textView.toString())
        textView.onClick?.invoke()
    }

}
```

<center>使用Butterknife<center/>

ButterKnife源码：

```kotlin
companion object {
    fun inject(target: Any) {
        val className = "${target::class.java.canonicalName}_ButterKnife"
        val clazz = Class.forName(className)
        val obj = clazz.newInstance()

        val bindMethod = clazz.getMethod("bind", target::class.java)
        bindMethod.invoke(obj, target)
    }
}
```

<center>inject方法<center/>

运行测试代码：

```kotlin
fun main() {
    Activity1().onCreate()
}
```

运行结果：

```shell
TextView(resId=1)
Activity1 TextView(resId=1) onClick
```

在 Butterknife 的 inject 方法中调用了 Activity1_Butterknife 的 bind 方法，这个类就是由 apt 生成的，看一下生成的代码（在module/build/generated/source文件夹内）：

```kotlin
class Activity1_ButterKnife {
   
   lateinit var target: Activity1
   
   fun bind(target: Activity1){
       this.target = target
       bindView()
       onClick()
   }

   fun bindView() {
       target.textView = TextView(1)
   }
   
   fun onClick(){
       target.textView.setOnclickListener{ target.foo() }
   }

}
```

核心流程为：

```
inject(this)->bind(target)->target.bindView(),target.OnClick()
```

这样就完成了通过 注解 + 注解处理器 完成了对 View 的绑定和 Onclick 事件的设置，这里是 IOC（控制反转） 的思想的应用，把原来是主动设置的代码交给生成的代码来处理，由外部来控制，这也是 Butterknife bind 的 view 属性不能为 private 的原因，这样会导致在生成的代码中无法访问到这个属性。



## Tips

1. 在配置注解处理器时，Java 使用 annotationProcessor，kotlin 使用 kapt
2. 生成的代码，Java 的注解处理器生成的在```module/build/generated/source/apt```里，kotlin 的注解处理器生成的在```module/build/generated/source/kapt```
3. 调试注解处理器：

- 在 libraries.gradle 中添加```org.gradle.jvmargs=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005```
- 编辑运行任务，点击 Edit Configurations，添加 "Remote"任务，命名"APT"，Port选择 5005，保存

debug 时先运行 "APT" debug 任务，再运行使用注解处理器的 module 即可



## Links

[1.源码级注解](https://www.jianshu.com/p/6db8ad1b89c6)

[2.javadoc8](https://docs.oracle.com/javase/8/docs/api/index.html)