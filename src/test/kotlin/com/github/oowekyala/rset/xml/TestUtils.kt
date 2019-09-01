package com.github.oowekyala.rset.xml

import io.kotlintest.matchers.collections.shouldBeEmpty
import org.w3c.dom.Document


val HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>"

fun Document.toStr(): String = DomIoUtils.writeToString(this).trimIndent()

inline fun <reified T> defaultSer(): XmlSerializer<T> =
        TypedSerializerRegistrar.getInstance().getSerializer(T::class.java)

class TestMessagePrinter : MessagePrinter {

    val warn = mutableListOf<String>()
    val err = mutableListOf<String>()
    val out = mutableListOf<String>()


    override fun warn(message: String) {
        warn += message
    }

    override fun error(message: String) {
        err += message
    }

    override fun println(msg: String) {
        out += msg
    }

    fun shouldBeEmpty() {
        warn.shouldBeEmpty()
        err.shouldBeEmpty()
        out.shouldBeEmpty()
    }
}
