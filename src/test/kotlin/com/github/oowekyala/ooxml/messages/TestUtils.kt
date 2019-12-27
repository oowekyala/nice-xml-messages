package com.github.oowekyala.ooxml.messages

import com.github.oowekyala.ooxml.messages.more.MessagePrinter
import io.kotlintest.matchers.collections.shouldBeEmpty
import org.xml.sax.InputSource
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory


val HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>"



class TestMessagePrinter : MessagePrinter {

    val warn = mutableListOf<String>()
    val err = mutableListOf<String>()
    val out = mutableListOf<String>()


    override fun warn(message: String) {
        warn += message.trimIndent()
    }

    override fun error(message: String) {
        err += message.trimIndent()
    }

    override fun info(message: String) {
        out += message.trimIndent()
    }

    override fun supportsAnsiColors(): Boolean = false

    fun shouldBeEmpty() {
        warn.shouldBeEmpty()
        err.shouldBeEmpty()
        out.shouldBeEmpty()
    }
}
