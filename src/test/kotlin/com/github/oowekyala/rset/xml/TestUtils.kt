package com.github.oowekyala.rset.xml

import io.kotlintest.matchers.collections.shouldBeEmpty
import org.w3c.dom.Document


val HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>"

fun Document.toStr(): String = DomUtils.writeToString(this).trimIndent()

inline fun <reified T> defaultSer(): XmlMapper<T> =
        TypedSerializerRegistrar.getInstance().getSerializer(T::class.java)

class TestMessagePrinter : MessagePrinter {

    val warn = mutableListOf<String>()
    val err = mutableListOf<String>()
    val out = mutableListOf<String>()

    override fun supportsAnsiColor(): Boolean = false


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
