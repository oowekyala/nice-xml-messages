package com.github.oowekyala.rset.xml

import org.w3c.dom.Document


val HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>"

fun Document.toStr(): String = DomIoUtils.writeToString(this).trimIndent()

inline fun <reified T> defaultSer(): XmlSerializer<T> =
        TypedSerializerRegistrar.getInstance().getSerializer(T::class.java)
