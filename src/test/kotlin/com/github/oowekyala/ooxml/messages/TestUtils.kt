package com.github.oowekyala.ooxml.messages

import io.kotlintest.matchers.collections.shouldBeEmpty


val HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>"


fun String.parseStr(printer: MessagePrinter = TestMessagePrinter()): PositionedXmlDoc =
        XmlErrorUtils.getDefault().parse(reader()) {
            DefaultXmlErrorReporter(printer, it)
        }





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
