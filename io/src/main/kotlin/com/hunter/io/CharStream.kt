package com.hunter.io

import com.sun.xml.internal.messaging.saaj.packaging.mime.util.LineInputStream
import java.io.*
import java.nio.charset.Charset

class CharStream {

    fun readFromCharArray() {
        CharArrayReader("寒雨連江夜入吳，平明送客楚山孤。\n洛陽親友如相問，一片冰心在玉壺。".toCharArray()).use {
            val charArray = CharArray(1024)
            it.read(charArray)
            println(charArray.joinToString(""))
        }
    }

    fun readFromFile() {
        FileReader("./io/char_test.txt").use {
            val charArray = CharArray(1)
            it.read(charArray)
            println(charArray.joinToString(""))
        }
    }

    fun readFromFile2() {
        FileReader("./io/byte_test.txt").use {
            val charArray = CharArray(1024)
            it.read(charArray)
            println(charArray.joinToString(""))
        }
    }

    fun readForLine() {
        LineNumberReader(FileReader("./io/char_test.txt")).use {
            println(it.readLine())
            println(it.readLine())
            println(it.readLines().joinToString("\n")) // read all lines
        }
    }

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
            println("realByteNumber：${byteNumber + lineNumber - 1}")
        }

        LineNumberReader(FileReader("./io/char_test.txt")).use {
            val allString = it.readLines().joinToString("\n")
            println("charNumber:${allString.toCharArray().size}")
        }
    }

    fun readFromInputStreamReader() {
        val charArray = CharArray(1)
        InputStreamReader(ByteArrayInputStream("长".toByteArray(Charset.forName("UTF-8")))).read(charArray)
        println(charArray[0])
    }

}