package com.github.oowekyala.ooxml.messages

import io.kotest.assertions.fail
import io.kotest.matchers.collections.shouldBeEmpty


const val HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>"


class TestMessagePrinter(private val useColors: Boolean = false) : XmlMessageHandler {

    val warn = mutableListOf<XmlException>()
    val err = mutableListOf<XmlException>()
    val out = mutableListOf<XmlException>()
    val debug = mutableListOf<XmlException>()


    override fun printMessageLn(kind: XmlMessageKind, severity: XmlException.Severity, message: String) {
        TODO()
    }

    override fun accept(entry: XmlException) {
        when (entry.severity) {
            XmlException.Severity.INFO -> out += entry
            XmlException.Severity.DEBUG -> debug += entry
            XmlException.Severity.WARNING -> warn += entry
            XmlException.Severity.ERROR -> err += entry
            XmlException.Severity.FATAL -> err += entry
            else -> fail("impossible")
        }
    }


    override fun supportsAnsiColors(): Boolean = useColors

    fun shouldBeEmpty() {
        warn.shouldBeEmpty()
        err.shouldBeEmpty()
        out.shouldBeEmpty()
    }
}
