* [What](#what)
* [Why](#why)
* [Where](#where)
* [How](#how)
   * [字节流](#字节流)
   * [字符流](#字符流)
   * [Byte 与 Char](#byte-与-char)
   * [编码与解码](#编码与解码)
   * [字节流到字符流](#字节流到字符流)
* [Summary](#summary)
* [Reference](#reference)

## What

IO: input/output 输入/输出



## Why

​	我们为什么需要 IO？众所周知，程序是运行在内存中的，而程序是安装在磁盘中的。
​	为什么程序是安装在磁盘中而不是内存中的呢？因为内存是"易失性存储"，断电数据即清空，显然内存是不适合安装程序的。

​	为什么程序是运行在内存中而不是磁盘中的呢？因为内存比磁盘速度快很多，这样程序的运行速度会快很多。

​	而磁盘到内存，或者内存到磁盘这个过程就叫做 IO，因为程序运行在内存中，所以我们站在内存的角度来讲，把磁盘数据读入到内存中，就叫做 Input，从内存中把数据写出到磁盘中，就叫做 Output。



## Where

​		哪里会用到 IO 呢？当程序启动时"加载"的工程，就会把磁盘中的数据给 Input 到内存中。当需要"读取"磁盘中的文件到内存中处理时，也需要 Input。而当程序在内存中产生了数据需要保存以便之后使用时，也需要 Output。或者程序需要从网络上下载文件，则需要网络的 Input 和本地的 Output 等等



## How

​		IO 的据数据来源大致有两种，一种是来自于本地磁盘，数据在磁盘的唯一最小描述就是文件，所以本地 IO 绕不开的就是文件，在 Java 中即是`File`类。另外一种数据来自于网络，网络数据的读写底层是 socket，在 Java 中即是`Socket`类。在这两种数据上都会有 IO 操作，IO 操作在 Java 中的基础抽象有两种，分别是`InputStream`和`OutputStream`，以及 `Reader` 和`Writer`，两套 API 的区别在于是以读取字节为单位还是以读取字符为单位。Input 和 Output 的操作基本是一一对应的，根据操作的需求和方式的不同，Input 和 Output 都有一套体功能体系



### 字节流

先来瞄一眼 Stream 的接口里读取和写入的方法

```java
// 读取
public abstract int read() throws IOException; // 读单个字节
public int read(byte b[]) throws IOException {
    return read(b, 0, b.length);
}
public int read(byte b[], int off, int len) throws IOException {
		// [多次]调用 int read() 方法
}

// 写入
public abstract void write(int b) throws IOException;
public void write(byte b[]) throws IOException {
    write(b, 0, b.length);
}
public void write(byte b[], int off, int len) throws IOException {
		// [多次]调用 write(int b) 方法  
}
```

再来看看两种流的部分主要继承体系

![](https://raw.githubusercontent.com/chdhy/Java-Tec/master/io/resource/InputStream.png)

<center>InputStream<center/>

![](https://raw.githubusercontent.com/chdhy/Java-Tec/master/io/resource/OutputStream.png)


<center>OutputStream<center/>

可以看到两者体系非常相似。以 InputStream 为例，主要分为两类，一类是 FilterInputStream(更像是一个MapperInputStream，或者 DecoratorInputStream)，我称其为**装饰流**，一类是像 FileInputStream 和 ByteArrayInputStream 这样的，我称其为**原始流**。原始流是数据的最终来源，装饰流是对原始流或者其他装饰流进行装饰的流，以达到增强其功能或性能等目的，**装饰流不能脱离原始流而存在**，脱离原始流，那便是"**无根之木,无源之水**",因为最终需要原始流提供数据来源。而 ObjectInputStream 虽然未继承 FilterInputStream，但其却是对 InputStream 的增强，实为装饰流。

```kotlin
fun readFromByteArray() {
    ByteArrayInputStream("hello,world".encodeToByteArray()).use {
        var read = it.read()
        while (read != -1) {
            print(read.toChar())
            read = it.read()
        }
    }
}

fun readFromFile() {
    FileInputStream("./io/byte_test.txt").use {
        val byteArray = ByteArray(1024)
        while (it.read(byteArray) != -1) {
            byteArray.toList()
    						.filter { byte -> byte >= 0 }
    						.forEach { byte -> print(byte.toChar()) }
        }
    }
}
```

<center>原始流<center/>

```kotlin
// 使用LineInputStream提供的 readLine 方法一次读取一行
fun readForLine() {
    LineInputStream(FileInputStream("./io/byte_test.txt")).use {
        var index = 0
        var line = it.readLine()
        while (line != null) {
            if (line.isBlank()) {
                println(line)
            } else {
                println("${++index}: $line") // add index for each not blank line
            }
            line = it.readLine()
        }
    }
}

// 读取 zip 压缩文件中的内容
fun readZipFile() {
    val zipFile = ZipFile("./io/archive.zip")
    ZipInputStream(FileInputStream("./io/archive.zip")).use {
        var nextEntry = it.nextEntry
        while (nextEntry != null) {
            println("${nextEntry.name} -----------------------")
            val byteArray = ByteArray(1024)
            val inputStream = zipFile.getInputStream(nextEntry)
            while (inputStream.read(byteArray) != -1) {
                println(String(byteArray))
            }
            println()
            nextEntry = it.nextEntry
        }
    }
}
```

<center>装饰流<center/>

可以看到装饰流其内部最终需要一个原始流提供数据，然后通过提供新的接口或者内部机制增强原始流。这里特别把 BufferedInputStream 拿出来说一说，根据该类的介绍，是通过增加内部缓存来增强读取性能，那究竟能提升多少性能呢？做一个对比：

```kotlin
fun readBufferInputStream() {
    val file = File("../../video.mp4") // about 600MB,replace to your big file

    val noBufferPerformance = performance {
        FileInputStream("../../video1.mp4").use {
            val byteArray = ByteArray(1024)
            while (it.read(byteArray) != -1) {
                // just read
            }
        }
    }
    println("noBufferPerformance: $noBufferPerformance")

    val defaultBufferPerformance = performance {
        BufferedInputStream(FileInputStream("../../video2.mp4")).use {
            val byteArray = ByteArray(1024)
            while (it.read(byteArray) != -1) {
                // just read
            }
        }
    }
    println("defaultBufferPerformance: $defaultBufferPerformance")

    val sameBufferPerformance = performance {
        BufferedInputStream(FileInputStream("../../video3.mp4"), 1024).use {
            val byteArray = ByteArray(1024)
            while (it.read(byteArray) != -1) {
                // just read
            }
        }
    }
    println("sameBufferPerformance: $sameBufferPerformance")

    val noBufferPerformance2 = performance {
        FileInputStream("../../video4.mp4").use {
            val byteArray = ByteArray(8192)
            while (it.read(byteArray) != -1) {
                // just read
            }
        }
    }
    println("noBufferPerformance2: $noBufferPerformance2")

    val bigBufferPerformance = performance {
        BufferedInputStream(FileInputStream("../../video5.mp4"), 16 * 1024 * 1024).use {
            val byteArray = ByteArray(1024)
            while (it.read(byteArray) != -1) {
                // just read
            }
        }
    }
    println("bigBufferPerformance: $bigBufferPerformance")

    val bigReadNoBufferPerformance = performance {
        FileInputStream("../../video6.mp4").use {
            val byteArray = ByteArray(16 * 1024 * 1024)
            while (it.read(byteArray) != -1) {
                // just read
            }
        }
    }
    println("bigReadNoBufferPerformance: $bigReadNoBufferPerformance")
}

// 运行结果
noBufferPerformance: 2215
defaultBufferPerformance: 1158
sameBufferPerformance: 2153
noBufferPerformance2: 1154
bigBufferPerformance: 810
bigReadNoBufferPerformance: 803
```

通过分析结果，我们可以看到：

1. 对比 1 和 3，没有 buffer 的情况，性能和 buffer 大小正好和读取大小相同时是差不多的
2. 对比 1 和 2，默认 buffer 确实比没有 buffer 性能更好
3. 对比 2 和 4，默认 buffer 和没有 buffer，但读取大小为 8k(8192)时性能是差不多的
4. 对比 2 和 5，稍大一点的 buffer 是比默认 buffer 性能会更好一些，但是提升不大
5. 对比 5 和 6，大的 buffer 和大的读取大小相同时性能是差不多的

由此，我们基本可以得出结论，buffer 是有用的，但是没有 buffer 也能实现（增大一次性读取大小），这...好像和 BufferedInputStream 的介绍有些出入啊，感觉稍微有些鸡肋了，我们来看看 BIS 的关键源码：

```kotlin
private int read1(byte[] b, int off, int len) throws IOException {
    int avail = count - pos;
    if (avail <= 0) {
        // 如果一次读取的长度大于等于 buffer 的长度，那就直接用被装饰的流直接读取，不使用 buffer
        if (len >= getBufIfOpen().length && markpos < 0) {
            return getInIfOpen().read(b, off, len);
        }
        fill();
        avail = count - pos;
        if (avail <= 0) return -1;
    }
    int cnt = (avail < len) ? avail : len;
    System.arraycopy(getBufIfOpen(), pos, b, off, cnt);
    pos += cnt;
    return cnt;
}
```

read1 方法是 read 多个 byte 时会最终调用的方法，关键行已经注释，当 一次读取长度>= buffer 长度时直接绕过了 buffer，使用原始流读取，而 buffer 默认的大小即是 8192，所以当指定 8192 的 读取大小时使用的是原始流直接读取方式，而其性能和默认 buffer 大小的性能是相似的，而当指定 buffer 大小为一次读取大小 1024 时，性能也和没有 buffer 时相似，所以读取的性能其实最终来自己一次读取的大小，一次读取更多的数据，可以减少 IO 的次数，增强 IO 性能(也不是越大越好)，所以这个 BIS 其实用处不大 😀



### 字符流

同样看看 Reader 和 Writer 的主要读写方法

```java
// 读
public int read() throws IOException {
    char cb[] = new char[1];
    if (read(cb, 0, 1) == -1)
        return -1;
    else
        return cb[0];
}
public int read(char cbuf[]) throws IOException {
    return read(cbuf, 0, cbuf.length);
}
abstract public int read(char cbuf[], int off, int len) throws IOException;

// 写
public void write(int c) throws IOException {
    synchronized (lock) {
        if (writeBuffer == null){
            writeBuffer = new char[WRITE_BUFFER_SIZE];
        }
        writeBuffer[0] = (char) c;
        write(writeBuffer, 0, 1);
    }
}
public void write(char cbuf[]) throws IOException {
    write(cbuf, 0, cbuf.length);
}
abstract public void write(char cbuf[], int off, int len) throws IOException;
```

可以看到 read 和 write 方法同样是高度对称，并且与 Stream 里的读写方法也高度类似，主要区别在于读写的最小单位由 byte 变成了 char，也即字面意思，字节流与字符流的区别。同样再来看看 Reader 和 Writer 的继承体系




![](https://raw.githubusercontent.com/chdhy/Java-Tec/master/io/resource/Reader.png)

<center>Reader<center/>




![](https://raw.githubusercontent.com/chdhy/Java-Tec/master/io/resource/Writer.png)

<center>Writer<center/>



心细的同学可能看到一个叫 InputStreamReader 和 OutputStreamWriter 的东西，难道这个字符流和字节流有什么关系吗？关子滞销，帮帮笔者！先来看看其他的，这个时候心细的同学又注意到了，Stream 里重要的 Filter 系列在字符流图里面没有了！难道字符流不需要装饰吗？当然不是了，只是出于未知的原因，那些常用的装饰流都没有继承 FilterReader/Writer，所以图里就没有体现出来了。话不多说，先来点代码，看看字符流是怎么工作的

```kotlin
fun readFromCharArray() {
    CharArrayReader("寒雨連江夜入吳，平明送客楚山孤。\n洛陽親友如相問，一片冰心在玉壺。".toCharArray()).use {
        val charArray = CharArray(1024)
        it.read(charArray)
        println(charArray.joinToString(""))
    }
}

fun readFromFile() {
    // 长恨歌
    FileReader("./io/char_test.txt").use {
        val charArray = CharArray(1024)
        it.read(charArray)
        println(charArray.joinToString(""))
    }
}
// output
/**
长恨歌
唐:白居易

汉皇重色思倾国,御宇多年求不得
杨家有女初长成,养在深闺人未识
天生丽质难自弃,一朝选在君王侧
...
*/

fun readForLine() {
    LineNumberReader(FileReader("./io/char_test.txt")).use {
        println(it.readLine())
        println(it.readLine())
        println(it.readLines().joinToString("\n")) // read rest all lines
    }
}
```

可以看到，和 Stream 用法基本相同，也遵循原始流和装饰流的规则。感觉 So Easy！嗯，可是，为什么要分字节流和字符流呢？字节流可以读取字符流读取的文件吗？反之呢？让我们分别试试看

```kotlin
// 用字节流读取 char_test.txt
fun readFromFile2() {
    FileInputStream("./io/char_test.txt").use {
        val byteArray = ByteArray(1024)
        while (it.read(byteArray) != -1) {
            byteArray.toList()
                .filter { byte -> byte.toInt() != -1 }
                .forEach { byte -> print((byte.toInt() and 0xFF).toChar()) }
        }
    }
}

// output
/**
é¿æ¨æ­
å:ç½å±æ

æ±çéè²æå¾å½,å¾¡å®å¤å¹´æ±ä¸å¾
æ¨å®¶æå¥³åé¿æ,å»å¨æ·±éºäººæªè¯
å¤©çä¸½è´¨é¾èªå¼,ä¸æéå¨åçä¾§
åç¸ä¸ç¬ç¾åªç,å­å®«ç²é»æ é¢è²
...
*/

// 用字符流读取 byte_test.txt
fun readFromFile2() {
    FileReader("./io/byte_test.txt").use {
        val charArray = CharArray(1024)
        it.read(charArray)
        println(charArray.joinToString(""))
    }
}
// output
/**
So early it's still almost dark out.
I'm near the window with coffee,
and the usual early morning stuff
that passes for thought.
...
*/
```

用字节流读取 char_test.txt，翻车了！再来看看用字符流读取 byte_test.txt，good，可以正常输出。好，那我们就重点关注一下前面的问题，下一节来讨论一下为啥只能正常输出标点符号



### Byte 与 Char



在 Java 中，除开表示逻辑值的 boolean 以外，byte 是值范围最小的类型，8 位 bit，而 bit 是计算机存储的最小单元了，就是我们所熟知的计算机最终的存储二进制的 0 和 1。char 是 16 位，即为两个 byte 的长度，这些都是在内存中的表示。在文件中，我们通常用字节，MB，GB 等表示文件内容的大小，字节可以和 MB，GB 等进行换算。这里的字节和 Java 中的 byte 是对应的概念，表示的大小也是一样的，都是 8bit。



我们回到上面的问题，用字节流读 char_test.txt会出现异常，用字符流读取 byte_test.txt则没有问题，出现问题的是"**以小读大**",这貌似和 byte 与 char 的容量大小有关系。我们知道 byte 是 8bit 的，可以存储 2 的 8 次方即 258 个不同的值，然后...桥豆麻袋，256，我们的常用汉字都有几千个，256 不够装啊！要怎么能装下更多的字符呢？合...合体!~~悟天克斯~~ Char!我们用两个 byte 合起来表示一个字符，这样我们就能够表示 2 的 16 次方即 65536 个不同的值,这样我们的汉字就能被装下了。OK，大概明白了，我用小的 byte 去装本来应该两个连在一起读的 bytes，那意思肯定被扭曲了，出错是肯定的。让我们直接打印读出来的原始 byte 看看

```kotlin
fun readFromFile3() {
    FileInputStream("./io/char_test.txt").use {
        val byteArray = ByteArray(1024)
        while (it.read(byteArray) != -1) {
            byteArray.toList()
                .filter { byte -> byte.toInt() != -1 }
                .forEach { byte -> print("${byte.toInt() and 0xFF} ") } // 转换成无正负值的byte值大小
        }
    }
}

// output 
233 149 191 230 129 168 230 173 140 10 229 148 144 58 231 153 189 229 177 133 230 152 147 10 10 230 177 137 231 154 135 233 135 141 232 137 178 230 128 157 229 128 190 229 155 189 44 229  ...
```

值都挺大的，很多大于 128 的，小于 128 的只有 58，10，44 三个值，这三个值对应的 ASCII 表里的冒号，换行和逗号。我们知道 ASCII 只有 128 个字符，超出这个表的 ASCII 就无能为力了，那超出的部分怎么表示呢？其实还有一个**ISO 8859-1**码表，主要是一些西欧的字符，其范围为 128-255，所以打印出来的乱码就都落在了这个码表，打印出来的第一个数字是 233，在这个码表中对应的就是打印出来的第一个字符`é`，结案！而为什么字符流可以正确打印出 byte_test.txt 中的内容呢，因为 byte_test.txt 中的字符都是落在 ASCII 码表中的，本来就是一个字节对应文件中的一个字符，所以用 char 去读取 byte 并不会导致 byte 的信息丢失，这就是能正确打印出内容的原因。



上面大家知道了我们可以通过**"组合多个byte"**来表示一个字符的方式增加可展示的字符范围，这样我们就能展示出中文汉字了。上面打印出的乱码中，原本的"长恨歌"三个中文汉字被输出为了 9 个字符(包括未能打印出来的空格等),可上面不是说了是一个 Char 对应两个 byte 吗，为什么会输出三个字符呢？



### 编码与解码



最开始的时候，只有美国人使用计算机，他们使用计算机需要把他们要用到的字符用计算机表达出来，所以就有了 ASCII 码表，里面包含一些常见的控制字符，数字，标点，大小写字母，一共 128 个，用一个 byte 就能表示 (0x01111111)。后来计算机发展到欧洲，原来的 ASCII 表并不能完全满足他们的需求，于是他们就增加了 ISO 8859-1 码表，增加了一些控制符，西欧字母和符号，并兼容了 ASCII 码表，加上 ASCII 的字符也能用一个 byte 表示 (0x11111111)。



后来计算机逐渐发展到世界各地，拿中国大陆来说，就发展出了一系列的 GB 标准，台湾有 Big5 等，日本有 Shift_JIS 等等，因为前面的两种码表已经占满了一个 byte 的容量，所以后面的编码都得使用更多的 byte 来表示一个符号，于是大陆的 GB 系列就用两个字节的编码来表示更多的简体汉字，台湾的 Big5 就用来表达繁体汉字，日本的就用来表示一些平假名，片假名和一些符号等。这些标准的制定并没有考虑其他地区的标准，于是不可避免的，同样的两个字节合起来所表达的字符，在不同的标准解释出来是不同的字符，所以如果用日文的编码写入的字符(双字节)如果用中文的编码来解释，那就会出现乱码，这就是为什么早先我们玩~~工口~~日文游戏的时候多会乱码的原因，因为**编码规则和解码规则不匹配**，等于鸡同鸭讲。



计算机继续往后发展，更多的不常见的字符也被编码进来了，比如从 GB2312 到 GBK 到GB18030，汉字逐渐增加到了 70,244 个，一个 Char 最大的表示范围是 65536 个，这个,  所以一个 Char 就已经装不下了，又要扩容了，一个 Char(两个字节) 变为两个 Char(四个字节) 的范围。OK，**容量危机**解决了！为了能够表示更多的字符，我们就统一使用 GB18030 编码来存储内容好了。但是解决了表示问题后，我们发现，原来一个 Char 就能表示的字符，我们需要用两个 Char 来表示了，文件大小直接加倍，当文件需要传输时大小也是直接加倍，甚至是 4 倍。god，这个成本难以接受！但是我们需要这么表示这么多东西啊，怎么办呢？



上面的问题，我们可以从编码方式上下手。对于能用一个 Char(65536) 能表示的字符，我们就用一个 Char(两个字节) 表示，对于超出这部分范围的，我们就用两个 Char 来表示，这样不就可以节省空间了吗，毕竟常用的字符都靠前，大多可以直接用一个 Char 表示出来。这其实就叫做**可变长编码**，对应前面的 ASCII，GBK 等编码方式，无论一个 byte 还是两个 byte，都叫做定长编码。通过使用可变长编码的方式，我们又解决编码过长的问题！



随着互联网的发展，各个国家的网络都逐渐连到了一起，各个国家之间的网络都可以**完全自由**？？？的互联互通。这时候，编码不兼容的问题就显得越发突出了，因为我们通常都是直接使用我们国家内部的编码格式和码表，而不会动态地为不同地区的文件采用不同的编码。这个时候一个全球统一的编码方式和码表就呼之欲出了，[Unicode](https://zh.wikipedia.org/wiki/Unicode)诞生了，Unicode 又称 **万国码**、**国际码**、**统一码**、**单一码**。见名知义，这是一个融合全球所有已知字符的编码，各个国家直接使用这一套码表，就可以不用担心码表不兼容的问题了。



码表变了，只是编码和解码对应的"词典"发生了变化，不足以完全解决问题，配套的编解码方式也要统一，这样在词典和编解码的方式上就完全统一了，也就不会有乱码的问题了。基于 Unicode 码表的编码方式有UFT-8,UFT-16,UFT-32 等。而我们系统中基本使用 UTF-8 作为默认方式，基于 Unicode 码表，具有全球统一编码，可变长编码节省空间，所以成为了最流行的编码方式。这里介绍一下 UTF-8 的编码规则：

1. 单字节的符号最高有效位 0,即单字节最高可表示范围是 0x01111111，127，加上 0，为 128 个字符，覆盖 ASCII 码表

2. 多字节的符号根据字节的数量编码方式有所不同，规则为，多字节字符首字节的前缀为字节个数的 1 和一个 0 组成，例如有三个字节的字符，首字节的前缀为 1110，还剩下 4 个 bit 为有效内容编码，多字节的后续字节都以 10 开头(与随机读写时的单字节做区分)，即后续字节的有效内容为 6bit


所以理论上双字节的 UTF-8 编码有效容量为 2 的 (16 - 3(110) - 2(10)) = 11 次方，2048 个字符，三个字节的有效容量为 65536 个字符，四个字节为 2097152。我们常用的汉字都落在 2048 到 65536 的范围内，所以虽然是可变长编码，但是汉字基本上都会被编码为 3 个字节,这就是"长恨歌"三个字被编码为 9 个 byte 的原因，而多字节字符至少是 10 开头的 byte，所以都大于 128，这就是最终这么多的汉字被编码并拆分后都落在  ISO 8859-1 表的原因



提问。char_test.txt 文件大小为多大？长恨歌全文 840 字，标题和作者名 7 个字，和一些符号，（840 + 7）* 3 + 60  + 60 + 3 = 2664 字节（可使用 ls -l char_test.txt命令查看）。要一次全部读到内存中，需要多少内存？因为字节是相互对应的，所以如果是字节流需要 2664 个字节，字符流则为 2664/2 = 1332 个char。有些奇怪❓❓❓实际上应该是（840 + 7） + 60 + 60 + 3 = 970 啊！为啥换算出来是 1332，哪里出了问题？我们来看看：

```kotlin
fun readForLine2() {
    LineInputStream(FileInputStream("./io/char_test.txt")).use {
        var lineNumber = 0
        var byteNumber = 0

        var line = it.readLine()
        while (line != null) {
            lineNumber++
            byteNumber += line.length
            line = it.readLine()
        }

        println("lineNumber：$lineNumber")
        println("byteNumber：$byteNumber")
        println("realByteNumber：${byteNumber + lineNumber - 1}") // 读行会去掉换行符，需要加回去，最后一行没有换行符，需要减一个
    }

    LineNumberReader(FileReader("./io/char_test.txt")).use {
        val allString = it.readLines().joinToString("\n")
        println("charNumber:${allString.toCharArray().size}")
    }
}
// output
lineNumber：63
byteNumber：2602
realByteNumber：2664
charNumber:970
```

字节流 2664，和计算吻合，字符流是确实是 970，为啥计算出来是 1332 呢？因为计算算的是字符流直接拼接成字符流的个数，比如一个汉字被编码成 3 个字节后，直接折算后是一个半的 Char，怪怪的哈，明明一个汉字就是一个 Char 可以表示的嘛。没错，确实一个汉字可以由一个 Char 表示，但是存储的确实也是三个字节，"存储"到"表示"是需要**解码**的，三个byte 的字符有效表示容量为 2 的 16 次方，即 65536，恰好是一个 Char 的容量，所以解码就是把存储的 byte 中的有效表示位给提取出来，转换成一个 Char 的过程。比如 "长" 字,三个无符号的 byte 值分别是 233 149 191，转换成二进制就是 1110 **1001**，10**01 0101**，10**11 1111**。我们去掉标识位后并拼接在一起的二进制是 1001 0101 0111 1111,这就恰好是一个 char 的容量，转换成 10 进制就是 38271，查找一下 [Unicode表](https://unicode-table.com/cn/) 38271 对应的编码即为"长"字。



### 字节流到字符流

我们知道了字符其实也是字节解码过去的，所以我们的底层读取单位一定是字节，而不是字符。而字符流系列的接口都是直接读取的字符，所以其内部一定有这样的解码过程。前面我们提到了 InputStreamReader，名字即包含 Stream，也有 Reader ，看起来应该是关键。来看看 InputStreamReader 的一些重要的代码

```java
private final StreamDecoder sd;

// 构造方法
public InputStreamReader(InputStream in) {
    super(in);
    try {
        sd = StreamDecoder.forInputStreamReader(in, this, (String)null); // ## check lock object
    } catch (UnsupportedEncodingException e) {
        // The default encoding should always be available
        throw new Error(e);
    }
}

// read 方法
public int read() throws IOException {
    return sd.read();
}

public int read(char cbuf[], int offset, int length) throws IOException {
    return sd.read(cbuf, offset, length);
}
```

构造方法有多个，都需要传入 InputStream，嗯，对了！其他几个构造方法需要传入与 Charset 相关的参数，都会被用来构造 StreamDecoder。看 read 方法的实现，真正做事的应该就是这个 StreamDecoder 了。继续往下跟 StreamDecoder.read(char cbuf[])->read(char cbuf[], int offset, int length) -> int implRead(char[] cbuf, int off, int end) ，这里看下

```java
private CharsetDecoder decoder;
private ByteBuffer bb; // byte array，用来装从stream读取的数据
private InputStream in; // 传入的字节输入流

int implRead(char[] cbuf, int off, int end) throws IOException {

  	// cbuf 包装
    CharBuffer cb = CharBuffer.wrap(cbuf, off, end - off);

    for (;;) {
    CoderResult cr = decoder.decode(bb, cb, eof); // ❤解码工作
    if (cr.isUnderflow()) {
        int n = readBytes(); // 预读byte数据到 bb
        continue;
    }
}
  
private int readBytes() throws IOException {
    ...
    int n = in.read(bb.array(), bb.arrayOffset() + pos, rem); // 读取数据
    ...
}
```

看起来重点在于这个 decoder.decode() 方法了，原始的字节数据已经由readBytes()准备好了，我们直接进去 CharsetDecoder 的 decode 方法看看

```java
public final CoderResult decode(ByteBuffer in, CharBuffer out,
                                boolean endOfInput)
{
	  ...
    for (;;) {
    CoderResult cr;
    try {
        cr = decodeLoop(in, out);
    }
    ...
}
```

还有一层，再次跟进 decodeLoop 方法，decodeLoop 是抽象方法，实现在 CharsetDecoder 的具体实现类里，这里是在 UTF_8类 的内部类 Decoder 里，跟进去看看 decode() -> decodeArrayLoop(src, dst) ，这里就是最终进行解码的地方了

```java
private CoderResult decodeArrayLoop(ByteBuffer src,
                                    CharBuffer dst)
{
		byte[] sa = src.array(); // 源 byte 数据
    char[] da = dst.array(); // 目标 char 数据
  	
    // ASCII only loop
    while (dp < dlASCII && sa[sp] >= 0) // 如果是 ASCII 字符，就直接转换单个字节为 char
        da[dp++] = (char) sa[sp++];
  
    ...
      
    // 多字节字符转换
    int b1 = sa[sp]; // byte1
    if (if ((b1 >> 4) == -2)){ // 截取三个字节一个字符的情况
      	// 3 bytes, 16 bits: 1110xxxx 10xxxxxx 10xxxxxx
        int b2 = sa[sp + 1]; // byte2
			  int b3 = sa[sp + 2]; // byte3
        
        // ❤ 将三个字节拼装并取出有效字节位
        char c = (char)
            ((b1 << 12) ^
             (b2 <<  6) ^
             (b3 ^
              (((byte) 0xE0 << 12) ^
               ((byte) 0x80 <<  6) ^
               ((byte) 0x80 <<  0))));

        // 将转换的的 char 放到转换后的目标 char 数组中
        da[dp++] = c;
    }
  
    ...
}
```

还是以上面的"长"字为例介绍一下核心的转换流程，"长"字编码后的三个 byte 的二进制分别是 b1: 1110 **1001**，b2: 10**01 0101**，b3: 10**11 1111**，加粗部分是有效位，我们需要把这些有效位合并到一个 char 的两个字节中，我们大致需要几步，

1. 分别将有效位放置到指定位置
2. 将多余的标志位恰当地抠除
3. 合并三个字节
4. 转化为 char，

下面将上面的算法以这个步奏**等效**分解一下

```java
三字节 	               0x0000,0000 xxxx,xxxx xxxx,xxxx // xxxx 为最终的有效指定位置

1.移动

(b1 << 12)             0x0000,1110 1001,0000 0000,0000 // 将b1有效位移动到双字节的 1-4 位
(b2 <<  6)             0x0000,0000 0010,0101 0100,0000 // 将b2有效位移动到双字节的 5-10 位
b3                     0x0000,0000 0000,0000 1011,1111 // 将b3有效位留在双字节的 11-16 位

2.抠除
#1(byte) 0xE0 << 12    0x0000,1110 0000,0000 0000,0000 // 用于抠除b1标志位的掩码
#2(byte) 0x80 << 6     0x0000,0000 0010,0000 0000,0000 // 用于抠除b2标志位的掩码
#3(byte) 0x80 << 0     0x0000,0000 0000,0000 1000,0000 // 用于抠除b3标志位的掩码
  
b3 ^ #3                0x0000,0000 0000,0000 1011,1111
                       0x0000,0000 0000,0000 1000,0000
                       0x0000,0000 0000,0000 0011,1111 // 将b3标志位抠除，保留11-16位有效位

b2 ^ #2                0x0000,0000 0010,0101 0100,0000
                       0x0000,0000 0010,0000 0000,0000
                       0x0000,0000 0000,0101 0100,0000 // 将b2标志位抠除，保留5-10位有效位
  
b1 ^ #1                0x0000,1110 1001,0000 0000,0000
                       0x0000,1110 0000,0000 0000,0000
                       0x0000,0000 1001,0000 0000,0000 // 将b1标志位抠除，保留1-4位有效位
3.合并  
b1 ^ b2 ^ b3           0x0000,0000 1001,0101 0111,1111 // 将b1,b2,b3合并

4.转化
(char)                           0x1001,0101 0111,1111 // 38271->'长'

```

通过上面的几步，就得到了'长'字，其余两字节、四字节字符的道理相同。至此，我们就得到了整个字节流到字符流的核心流程 InputStreamReader.read -> StreamDecoder.read -> CharsetDecoder.decode。所以，最后看起来，字符流最终也是字节流的一次包装，本质就就是一种对字节流的**装饰流**，而因为提供的核心接口不同，并且屏蔽了字节流的接口，所以最终和字节流分成了两套接口体系



## Summary

​	因为数据的状态不同，适合存储数据的介质也有所不同，数据在不同介质间的传递的行为，就是 IO。在 Java 中，根据读取数据的单位不同，IO 流有两套 API 体系，一个是字节流，一个是字符流。字节流主要用在非文本文件的读写，或者纯 ASCII 字符的文本文件，(即不需要由多个字节来表示一个字符的场景)，亦或是不需要关心文件内容的场景(比如文件整体传输等)，而字符流主要用在包含非 ASCII 字符的文本文件中，需要读取文本内容的场景。根据流是否是真正的数据来源，可以分为装饰流或者原始流。而每一套 API 体系(字符或者字节)都有根据其自身属性所衍生的装饰流等，用于增强流的功能或者性能，字符流本质也是通过字节流转换而成，从实现方式来说也可以看成是对字节流的一种装饰。



## Reference

1. [深入分析 Java I/O 的工作机制](https://developer.ibm.com/zh/articles/j-lo-javaio/)
2. [深入分析 Java 中的中文编码问题](https://developer.ibm.com/zh/articles/j-lo-chinesecoding/)
3. [Unicode table](https://unicode-table.com/cn/)