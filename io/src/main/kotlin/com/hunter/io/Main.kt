package com.hunter.io

fun main() {
    val byteStream = ByteStream()
    byteStream.readFromByteArray()
    byteStream.readFromFile()
    byteStream.readFromFile2()
    byteStream.readFromFile3()
    byteStream.readForLine()
    byteStream.readBufferInputStream()
    byteStream.readZipFile()

    val charStream = CharStream()
    charStream.readFromCharArray()
    charStream.readFromFile()
    charStream.readFromFile2()
    charStream.readForLine()
    charStream.readForLine2()
    charStream.readFromInputStreamReader()
}

