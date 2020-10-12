package com.hunter.io

import com.sun.xml.internal.messaging.saaj.packaging.mime.util.LineInputStream
import java.io.BufferedInputStream
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileInputStream
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream

class ByteStream {

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
            val byteArray = ByteArray(1024) { -1 }
            while (it.read(byteArray) != -1) {
                byteArray.toList()
                    .filter { byte -> byte >= 0 }
                    .forEach { byte -> print(byte.toChar()) }
            }
        }
    }

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

        // noBufferPerformance: 2215
        // defaultBufferPerformance: 1158
        // sameBufferPerformance: 2153
        // noBufferPerformance2: 1154
        // bigBufferPerformance: 810
        // bigReadNoBufferPerformance: 803
    }

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

}