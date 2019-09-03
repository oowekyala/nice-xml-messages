package com.github.oowekyala.oxml

import io.kotlintest.matchers.collections.shouldBeEmpty
import org.w3c.dom.Document


val HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>"

fun Document.toStr(): String = Oxml.getDefault().writeToString(this).trimIndent()


fun String.parseStr(printer: MessagePrinter = TestMessagePrinter()): Oxml.LocationedDoc = Oxml.getDefault().parse(reader()) { DefaultErrorReporter(printer, it) }





class TestMessagePrinter : MessagePrinter {

    val warn = mutableListOf<String>()
    val err = mutableListOf<String>()
    val out = mutableListOf<String>()

    override fun applyAnsi(code: MessagePrinter.AnsiCode, string: String): String {
        return string
    }


    override fun warn(message: String) {
        warn += message.trimIndent()
    }

    override fun error(message: String) {
        err += message.trimIndent()
    }

    override fun println(message: String) {
        out += message.trimIndent()
    }

    fun shouldBeEmpty() {
        warn.shouldBeEmpty()
        err.shouldBeEmpty()
        out.shouldBeEmpty()
    }
}
