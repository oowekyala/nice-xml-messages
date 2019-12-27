package com.github.oowekyala.ooxml.messages

import com.github.oowekyala.ooxml.messages.more.MessagePrinter
import io.kotlintest.matchers.collections.shouldBeEmpty
import org.xml.sax.InputSource


val HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>"


fun String.parseStr(handler: XmlMessageHandler = TestMessagePrinter()): PositionedXmlDoc =
        XmlErrorUtils.getInstance().parse(InputSource(reader()), handler)


class TestMessagePrinter : MessagePrinter, XmlMessageHandler {

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

    override fun accept(entry: XmlException) {
        error(entry.toString())
    }

    override fun supportsAnsiColors(): Boolean = false

    fun shouldBeEmpty() {
        warn.shouldBeEmpty()
        err.shouldBeEmpty()
        out.shouldBeEmpty()
    }
}
