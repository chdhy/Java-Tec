* [What](#what)
* [Why](#why)
* [Where](#where)
* [How](#how)
   * [没有泛型的世界](#没有泛型的世界)
   * [泛型来拯救](#泛型来拯救)
   * [泛型的原理](#泛型的原理)
   * [泛型深入](#泛型深入)
   * [Kotlin上的泛型](#kotlin上的泛型)
* [Summary](#summary)

## What

**Generic**：一般的，通用的，泛型

泛型是一种将类型参数化的技术，在使用时才指定具体的类型，声明的泛型代表的是一种抽象的类型，不是具体的对象，也不是具体的 Class

## Why

​		使用泛型可以使编写的代码具有更广泛的普适性，在编译器对代码的类型进行约束，减少代码的强转以及由此可能产生的 ClassCastException

## Where

​		泛型可以使用在类，接口和方法处，以使其具有更广泛的通用性（合理地接受更多类型的参数）

## How

### 没有泛型的世界

​		先来看看没有泛型的时候，我们是怎么做的：

```java
public class Goods {}

public class Fruit extends Goods{}

public class Poison extends Goods {}

public class Apple extends Fruit {}

public class Banana extends Fruit {}

public class GreenApple extends Apple{}

public class RedApple extends Fruit {}

public class PoisonApple extends Poison {} // 要搞事
```

<center>用到的 bean<center/>

```java
List basket = new ArrayList();

// add by seven dwarfs
basket.add(new RedApple());
basket.add(new GreenApple());
basket.add(new Banana());

// princess eat goods
Princess princess = new Princess();
for (Object o : basket) {
    princess.eat((Goods) o);  // force transfer
}
```

七个小矮人摘了水果，拿给公主吃。公主不知道篮子里到底装的是什么，所以在吃的时候每次取出来都要强转一下，才能吃下去。强转一下，看起来也不算难以接受，那看一下接下来发生的事情：

```java
List basket = new ArrayList();

// add by seven dwarfs
basket.add(new RedApple());
basket.add(new GreenApple());
basket.add(new Banana());

// add by witch
basket.add(new PoisonApple()); // die

// princess eat goods
Princess princess = new Princess();
for (Object o : basket) {
    princess.eat((Goods) o);  // force transfer
}
```

公主还是吃照常吃下篮子里的东西，然后公主 dead，发生了什么？仔细看中间的代码，原来是女巫悄悄在篮子放了一个毒苹果。因为篮子里可以放下任何东西，所以这个篮子是不安全的，不能取出来然后放心的吃下去。所以，矮人们又接了一个公主回来，并做了一个只能放下水果的篮子(magic😋)：

```java
public class FruitBasket {

    private List fruitBasket = new ArrayList();

    public void addFruit(Fruit fruit) {
        fruitBasket.add(fruit);
    }

    public Fruit getFruit() {
        if (fruitBasket.size() > 0) {
            Object o = fruitBasket.get(0);
            fruitBasket.remove(0);
            return (Fruit) o;
        } else {
            return null;
        }
    }

}
```

<center>FruitBasket<center/>

```java
FruitBasket fruitBasket = new FruitBasket();

fruitBasket.addFruit(new Apple());
fruitBasket.addFruit(new Apple());
fruitBasket.addFruit(new Banana());

Princess princess = new Princess();
while (fruitBasket.canGetFruit()) {
    // princess eat
    princess.eat(fruitBasket.getFruit());
}
```

看起来不错，公主 2 号可以方便地吃到水果了。但仔细想一想，吃各种水果的方式其实不同的，吃香蕉要先剥皮，吃苹果要先削皮，吃草莓要先清洗，所以 Princess 其实有各种不同的 eat 方法 `eatApple(Apple apple),eatBanana(Banana banana)`等,需要传入具体的 Apple，Banana，所以我们的代码变成了这样：

```java
/*省略部分代码*/
while (fruitBasket.canGetFruit()) {
    // princess eat
    Fruit fruit = fruitBasket.getFruit();
    if (fruit instanceof Banana) {
        princess.eatBanana((Banana) fruit);
    }
    if (fruit instanceof Apple) {
        princess.eatApple((Apple) fruit);
    }
}
```

公主又正常的吃上了水果，Good。But，not enough！！！公主觉得这样吃水果一点也不优雅，强转又出现了，还增加了类型判断。公主应该每种水果都有一个专属篮子，苹果篮子装苹果，香蕉篮子装香蕉，想吃什么就从对应的篮子里拿，做自己的~~女王~~,噢不,公主。于是我们增加了`AppleBasket,BananaBasket`，事情好像开始变得不对劲了，这几个 basket 的代码是高度相似的，只是为了装下一种具体的东西，本质都是一个篮子，有什么办法可以只写一个类就当成不同种类的 basket 来用吗？有的有的

### 泛型来拯救

​		让我们用泛型来重构一下刚才的 Basket：

```java
public class GenericBasket<T> {
    private List fruitBasket = new ArrayList();

    public void addFruit(T fruit) {
        fruitBasket.add(fruit);
    }

    public T getFruit() {
        if (fruitBasket.size() > 0) {
            Object o = fruitBasket.get(0);
            fruitBasket.remove(0);
            return (T) o;
        } else {
            return null;
        }
    }

    public boolean canGetFruit() {
        return !fruitBasket.isEmpty();
    }
}
```

<center>GenericBasket<center/>

使用泛型：

```java
GenericBasket<Apple> apples = new GenericBasket<>();
apples.add(new RedApple());
apples.add(new GreenApple());
apples.add(new Apple());
// magic
//apples.add(new Banana()); //  compile error - Required type:Apple Provided: Banana
GenericBasket<Banana> bananas = new GenericBasket<>();
bananas.add(new Banana());
Princess princess = new Princess();
while (apples.canGetMore()) {
    princess.eatApple(apples.get()); // elegant: no more force transfer,no more instance
}
while (bananas.canGetMore()) {
    princess.eatBanana(bananas.get());
}
```

嗯，优雅！在使用的时候才指定类型，系统将帮你约束类型的添加，取出时则为指定的类型，无须类型强转。等等，这个 GenericBasket 不就是一个 List 吗？没错，上面的代码其实可以替换为：

```java
List<Apple> apples = new ArrayList<>();
apples.add(new RedApple());
apples.add(new GreenApple());
apples.add(new Apple());

List<Banana> bananas = new ArrayList<>();
bananas.add(new Banana());

Princess princess = new Princess();
while (!apples.isEmpty()) {
    princess.eatApple(apples.get(0));
    apples.remove(0);
}
while (!bananas.isEmpty()) {
    princess.eatBanana(bananas.get(0));
    bananas.remove(0);
}
```

<center>😯<center/>

### 泛型的原理

​		泛型于 Java1.5 登场，之前的集合类(以及其他类)是没有泛型的，在 1.5 为集合类与其他类添加了泛型。桥豆麻袋！那以前的代码呢，以前的代码都没有泛型，那不是还要修改成泛型的调用方式？其实，Java 允许泛型 raw 类型，即可以不指定泛型类型，像最开始的 basket 就是一个 raw 的泛型 List，什么都可以放，但是只能取出 Object 来，因为 Java 所有的对象都集成自 Object，所以取出来的对象一定是个 Object。而因为原始类型（int等）不是对象，所以泛型的类型不能是原始类型，可以是对应的包装类（int to Integer）

​		看看下面的代码：

```java
GenericBasket<Apple> apples = new GenericBasket<>();
GenericBasket<Banana> bananas = new GenericBasket<>();
System.out.println(apples.getClass());
System.out.println(bananas.getClass());

// 运行结果
class com.hunter.generic.GenericBasket
class com.hunter.generic.GenericBasket
```

​		`GenericBasket<Apple> 和 GenericBasket<Banana>`在运行时是同样的类型，并且声明的泛型类型不见了，这被称作**类型擦除**。既然都没有具体的`GenericBasket<Apple> 或者GenericBasket<Banana> 类型` ，那泛型类本身呢，会被编译成什么样子呢，来看一下`GenericBasket`编译后的字节码：

```sh
public void add(T);
    descriptor: (Ljava/lang/Object;)V // #1.参数T被替换成了Object
    flags: ACC_PUBLIC
    Code:
      stack=2, locals=2, args_size=2
         0: aload_0
         1: getfield      #4                  // Field fruitBasket:Ljava/util/List;
         4: aload_1
         5: invokeinterface #5,  2            // InterfaceMethod java/util/List.add:(Ljava/lang/Object;)Z  // #2.List.add的参数fruit也是Object
        10: pop
        11: return
      LineNumberTable:
        line 13: 0
        line 14: 11
      LocalVariableTable:
        Start  Length  Slot  Name   Signature
            0      12     0  this   Lcom/hunter/generic/GenericBasket;
            0      12     1 fruit   Ljava/lang/Object; // #3.本地变量表里的fruit也是Object
      LocalVariableTypeTable:
        Start  Length  Slot  Name   Signature
            0      12     0  this   Lcom/hunter/generic/GenericBasket<TT;>;
            0      12     1 fruit   TT;
    Signature: #29                          // (TT;)V
```

可以看到编译后的 add 方法 T 类型实际被替换成了 Object。既然 add 方法的参数被编译成了 Object，那为什么在 `GenericBasket<Apple>` 类型里传入 Banana 编译器会报错呢，是哪里出了问题呢？编译器为啥会报错呢？没错，就是编译器本器了。一旦在使用了泛型时声明了具体类型，那么编译器将会检查这个类型后续的动作，一旦尝试做出非法的操作，那么编译就会失败，这就是 **编译时检查**

​		既然泛型 T 出现的地方，实际编译时被替换成了 Object，get 方法也是一样。那为啥`GenericBasket<Apple>` get 的结果可以直接传入`eatApple(Apple apple)`呢，我们再来看看调用方法这部分代码编译后的样子：

```sh
  public void genericBasket();
    descriptor: ()V
    flags: ACC_PUBLIC
    Code:
    # 省略部分编码
        77: invokevirtual #21                 // Method com/hunter/generic/GenericBasket.get:()Ljava/lang/Object;
        80: checkcast     #14                 // class com/hunter/generic/bean/Apple
        83: invokevirtual #22                 // Method com/hunter/generic/Princess.eatApple:(Lcom/hunter/generic/bean/Apple;)V
        98: invokevirtual #21                 // Method com/hunter/generic/GenericBasket.get:()Ljava/lang/Object;
       101: checkcast     #16                 // class com/hunter/generic/bean/Banana
       104: invokevirtual #23                 // Method com/hunter/generic/Princess.eatBanana:(Lcom/hunter/generic/bean/Banana;)V
```

没错，又是编译器，在 `GenericBasket.get() Object`后又给插入了强转成对应具体类型的代码 #14 转成 Apple 类型，#16 转成 Banana 类型，所以能够调用对应的具体 eatXxx 方法，巧妙。但是，随后我们察觉到，有些事情做不到了，不能 `new T(), new T[]`，因为 T 相当于只是一个类型占位符，在编译时类型被擦除了，所以做不到 new 对象

​		所以为什么 Java 要通过类型擦除来实现泛型功能呢？因为**兼容**，彼时 Java 已经发展到 1.4，使用者甚众，必须要考虑兼容性。老的代码在升级新的 Java 版本时要无需更改代码即可以编译通过，并且不能因为出现了泛型就把原来写的类都写一个泛型版本，所以需要在原来的类上增加泛型特性。为了兼容老的代码，Java 允许不强制声明泛型，即 raw 类型泛型，这保证了原来的代码可以不用修改。同时我们也需要具有泛型的代码能使用老版没有泛型的代码，来考虑一下这样的情况：

```java
// 老代码
public void foo(List strings){

}

// 调用老代码
List<String> strings = new ArrayList<>();
foo(strings)
```

这种调用是合理的，所以需要`List strings = new ArrayList<String>()`成立，而单纯的 List 是没有泛型类型的。我们假设 ArrayList<String> 有一个新的类型，比如 ArrayList@String 类，这个类需要动态生成，并继承 ArrayList 类，且不说编译时生成的类造成的字节码文件的增加，万一 ArrayList 是 final 的类呢？这样就不又不能完全兼容了并且如果泛型要使用老的没有泛型的类型，那就需要 ArrayList@String = ArrayList，由于集成的顺序，这显然是不合理的！ 🙃 所以"一切为了兼容",Java 采用了 使用泛型声明时类型擦除 + 具体泛型类型声明处替换上界 + 使用处类型强转 的方式达成了新增泛型并兼容老代码的成就！原来泛型就是编译器给 Java 开发者的一颗语法糖 😯



### 泛型深入

​		随着使用泛型的逐渐深入，你写出了这样的代码,却突发发现编译器报错了！

```java
List<Fruit> fruits = new ArrayList<Apple>(); // 编译错误 Required type:List<Fruit> Provided:ArrayList<Apple>
```

WFT？为什么装苹果的篮子不是装水果的篮子？从现实世界的语义上来讲，没错，但在程序的世界里，的确不是！装水果的篮子可以装苹果，也可以装香蕉，但是装苹果的篮子却只能装苹果，像上面的代码，为了保证相 ArrayList<Apple> 的正确性，那 fruits 就不能放进去除了苹果以外的水果，这相当于将装水果的篮子装东西的能力**窄化**了！那我这样呢：

```java
List<Apple> fruits = new ArrayList<Fruit>(); // 编译错误 Required type:List<Apple> Provided:ArrayList<Fruit>
```

WTF? 我装水果的篮子还不能装苹果了？当然可以装，但是问题也出在这，既然什么都能装，那取呢？苹果篮子从语义上来讲自然只能取出苹果，万一从篮子里取出一个香蕉当成苹果来使，那 ClassCastException 就又回来了，泛型的意义也就没了。所以这里的问题是 ArrayList<Fruit> **不正确地泛化**了苹果篮子取东西的能力



​		通过限制引用相互串用，保证了泛型功能语义的完整性和严谨性，这保证了类型转化的安全。但是这种限制可能有点过于严厉了，比如下面这段代码：

```java
public void noneGenericMethod(List<Fruit> fruits) {
    for (Fruit fruit : fruits) {
        // fruit.xxx
    }
}

// call
List<Apple> apples = new ArrayList<>();
noneGenericMethod(apples); // 编译错误 Required type:List<Fruit> Provided:ArrayList<Apple>
```

调用处出现了类型不匹配的编译错误。嗯，没毛病，刚才说了`List<Apple>`会窄化`List<Fruit>`装东西的能力，所以不能这样调用啊，但我们仔细观察一下 noneGenericMethod 方法的 fruits 参数，它只是取出了数据，并没有装进新的数据，所以没用到的能力不存在窄化这一说对吧，既然只是取出水果，我`List<Apple>`也可以做到啊。那...有什么办法可以传入`List<Apple>`参数呢，我都只是取出 Fruit 而已了啊！这时，一个声音出现了："你能保证 fruits 参数只取不存吗？这样我就让你传入`List<Apple>`参数"，noneGenericMethod 回答到:"我保证！"，声音再次说道:"既然你保证过了，那违反承诺将会付出代价..."，一道绿光过后，noneGenericMethod 升级成了`public <T extends Fruit> void genericMethod(List<T> fruits)`后 获得了传入`List<Apple>`参数的能力(这叫做**协变Covariant**)

​		后来的某一天 genericMethod 做了一点维护，添加了一点代码：

```java
public <T extends Fruit> void genericMethod(List<T> fruits) {
    // 添加的代码
    fruits.add(new Banana()); // 编译错误 Required type:T Provided:Fruit
    
    for (Fruit fruit : fruits) {
        // xxx
    }
}
```

纳尼，我从 fruits 里取水果，我自己不能再添加一个？"你忘记你的承诺了？只取不存！"一个声音出现了，没错，这个声音就是编译器的！"你自己用是没问题，但是，传进来的参数不只是你自己会用，别人还会用，别人传一个 List<Apple> 你存个 Banana 进去，会把别人搞崩的啊，香蕉你个臭**！" genericMethod 恍然大悟，删除了添加的代码，维护了自己当初的承诺(被动的)

​		随着代码的不断增加，后面又写出了这样的代码：

```java
public List<Apple> noneGenericMethod2(List<Apple> apples) {
    ArrayList<Apple> otherApples = new ArrayList<>();
    // 省略代码
    apples.addAll(otherApples);
    return otherApples;
}

// call
List<Fruit> fruits = new ArrayList<>();
noneGenericMethod2(fruits); // 编译错误 Required type:List<Apple> Provided:ArrayList<Fruit>
```

同样的，不出所料，报错了，因为 fruits 不恰当地泛化了`ArrayList<Apple>`取东西的能力。但这里同样也没有取东西，只是放进了 Apple，照理来说 fruits 是可以胜任的，这时一个声音出现了...咳，不演了，noneGenericMethod2 直接承诺不取数据，所以升级成了`public List<Apple> genericMethod2(List<? super Apple> apples)`，这样就可以传入 List<Fruit> 了(这叫做**逆变Invariant**)。同上，在genericMethod2 方法里，同样不能从取出 fruit 参数里取出 Apple，因为 fruits 啥水果都可以装，不一定只是苹果，所以会有 ClassCastException 风险

​		这里再补充一点，仔细的同学可能看出了 genericMethod 和 genericMethod2 方法的格式不太一样,genericMethod 方式是声明泛型 <T extends Fruit>，然后在参数列表使用 T，而genericMethod2 方法是直接在参数列表处使用 <? super Apple>，这两种方式有什么区别呢？没啥区别，除了声明了 T 之后这个类型就有**名字(引用方式)**了，之后就可以在其他地方再次使用，比如其他的参数也需要 T，方法内部需要 T，返回值需要 T 等，所以 genericMethod 方法其实可以替换成`public void genericMethod(List<? extends Fruit> fruits)`,genericMethod2 方法同样可以替换成对应的表达方式

​		

​		有时候，我们会遇到实际并不关心泛型实际类型的情况，只关注泛型“宿主”，也就是不限定泛型参数的类型，我们有几种做法：

```java
public void listObject(List<Object> list){}

public void listRaw(List list){}

public void listExtendsObject(List<? extends Object> list){}

public void listWildCard(List<?> list){}
```

这几种情况有什么区别呢？第一种，我们通过将 Object 作为泛型类型接收参数，这样就什么都能装了，因为 Object 是一切类型的父类，但是，我们却不能直接传递一个非 Object 的具体类型的 List<XXX>，原因参见上面。第二种，我们直接不限定 List 的泛型类型，这样就随便什么具体类型的 List 都可以传递了，bingo！可是，这是 Java 不推荐的。为啥呢？看看这段代码:

```java
ArrayList listRawType = new ArrayList();
ArrayList<Fruit> fruitList = new ArrayList<>();
fruitList.add(new Fruit());
fruitList.add(new Banana());
listRawType = fruitList;
listRawType.add(new PoisonApple()); // WTF
```

代码最后一行做了坏事，把毒苹果装进了水果篮子！为什么编译器不发出警告呢？因为 raw type 为了为了兼容可以直接引用具有泛型的实例，为了兼容老代码 raw type 本身就是不做类型约束的，所以 raw type 存放任何类型的实例都是没问题的，raw,no law! 所以在第三中方式里，我们活学活用了上面介绍过的 extends 大法，解决了上面两个存在的问题。既可以传递任意具体的的泛型实例，又能够防止添加错误的类型,即使是存入 Object 对象也是不被允许的，防止了 ClassCastException 的发生。但是感觉写起来有点，嗯，麻烦了。 我们再来看看最后一种类型，这个是啥？这叫做通配符，有啥作用？它等价于第三种类型。嗯，写起来挺简单的！But，这里也有一个缺陷，listWildCard 的参数`list.add(null)`是被允许的，因为 Java 的空和非空类型没有做区分，所以即使是存入 null，也不会引起 ClassCastException 的问题，然而却可能会导致意外的 NullPointException(第三种方式也有这个问题)，Java 的阿喀琉斯之踵！



### Kotlin上的泛型

​		因为 Kotlin 和 Java 的完全兼容，我们可以设想上面的每一个特性都有对应的 Kotlin 实现，这里不做展开，说几个重要的点。



​		还记得上面 genericMethod 和 genericMethod2 吗，genericMethod 需要一个能取出 Fruit 的 List，genericMethod2 需要一个能存入 Apple 的 list，他们的泛型声明分别是 <? extends Fruit> 和 <? super Apple>，感觉这俩不够**直观**呢，一不小心就会用混。少侠，我看你骨骼惊奇，来这里有一个口诀，你记一下：

> PECS：Producer extends,Customer super

生产者 extends，消费者 super，取出(out)是生产者，存入(in)是消费者，取出 extends，存入 super。先背诵，再按口诀转换！有没有一点懵逼？那先不用记了，我们来看看 Kotlin 是如何做的

```kotlin
fun genericMethod(t: List<out Apple>) { }

fun genericMethod2(t: MutableList<in Fruit>) { }
```

‼️这么简单？见名知义啊！out 就是只能往外取，in 就是只能往里存。能再简单点吗？😀 再来看看通配符泛型在 kotlin 上是怎样的：

```kotlin
fun listStarProjection(t: MutableList<*>) { }
```

看起来差别不大，就是 ？换成了 *，那实际上呢？实际差别也不大，哈，不过有一个重大的不同点，就是 t 参数不能存入 null，堵死了方法拿到 t 后乱写东西的最后一条路，这样不仅不怕 ClassCastException，也不怕意外的 NullPointException 了，perfect！为什么 kotlin 和 Java 会有这样的区别呢？因为这对 kotlin 来说从语义上来讲是正确的，因为 Kotlin 的空和非空是分离的的不同类型，所以给 t 传入的机可能是空的也可能是非空的，所以同样是为了保证类型的安全性，Kotlin 要禁止 null 的写入



​		再来看个新鲜的东西，`reified generic`.直接上代码：

```kotlin
inline fun <reified T> reifiedGeneric() {
    println(T::class.java.simpleName)
}

// call 
reifiedGeneric<Fruit>()
reifiedGeneric<Poison>()

// result
Fruit
Poison
```

这... T 不是只相当于一个占位符，类型会被擦除吗？为什么这里可以获取到运行时的 `T::class.java`。秘密在于 inline 和 reified 关键字，inline 是会把函数内联的关键字，即在编译时把函数的调用替换成被调用函数的实际内容，这样**动态**的部分其实就变成了**静态**的了。那也不能这样写啊！别急，还有一个 reified 关键字呢，reified 关键字做了啥，看看调用字节码反编译后的样子：

```java
  public static final void main() {
    int $i$f$reifiedGeneric = 0;
    String str = Fruit.class.getSimpleName();
    boolean bool = false;
    System.out.println(str);
    $i$f$reifiedGeneric = 0;
    str = Poison.class.getSimpleName();
    bool = false;
    System.out.println(str);
  }
```

😯 原来是直接替换成了 具体类型.class 的调用啊，那这样就没问题了，又是一颗语法糖，真甜！



## Summary

​		泛型，就是将类型参数化，并泛化，这样可以大大提升接口的通用性。但在使用泛型的时候也要遵守泛型的使用原则，从语义出发，不能不恰当地泛化或者窄化泛型类型的职责。Java 出于兼容的原因实际是用类型擦除+编译器检查的方式实现了泛型，这也在实际使用时带来了一些限制，主要是运行时的限制。kotlin 由于是后来的语言，少了一些历史的包袱，借鉴了更多现代化语言的特性，在语法的可读性和语义的严谨性上都会更胜一筹，还带来了 reified generic 特性，嗯，真香！