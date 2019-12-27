package com.github.oowekyala.ooxml.messages

import io.kotlintest.fail
import io.kotlintest.matchers.collections.shouldBeEmpty


const val HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>"


class TestMessagePrinter(private val useColors: Boolean = false) : XmlMessageHandler {

    val warn = mutableListOf<XmlException>()
    val err = mutableListOf<XmlException>()
    val out = mutableListOf<XmlException>()
    val debug = mutableListOf<XmlException>()


    override fun accept(entry: XmlException) {
        when (entry.severity) {
            Severity.INFO -> out += entry
            Severity.DEBUG -> debug += entry
            Severity.WARNING -> warn += entry
            Severity.ERROR -> err += entry
            Severity.FATAL -> err += entry
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
