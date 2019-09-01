package com.github.oowekyala.rset.xml

import io.kotlintest.shouldBe
import io.kotlintest.specs.FunSpec
import org.w3c.dom.Document


class OutputTest : FunSpec({


    test("Test simple string conversions") {
        val doc = DocumentMaker().makeDoc("oha!", defaultSer<String>())

        doc.toStr() shouldBe """
            $HEADER
            <str>oha!</str>
        """.trimIndent()
    }

    test("Test composition") {
        val doc = DocumentMaker().makeDoc(
                listOf("oha", "what", "are", "you"),
                defaultSer<String>().toSeq<List<String>> { mutableListOf() }
        )

        doc.toStr() shouldBe """
$HEADER
<seq>
    <str>oha</str>
    <str>what</str>
    <str>are</str>
    <str>you</str>
</seq>
        """.trimIndent()
    }

})

val HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>"

fun Document.toStr(): String = DocumentMaker.writeToString(this).trimIndent()

inline fun <reified T> defaultSer(): XmlSerializer<T> =
        SerializerRegistrar.getInstance().getSerializer(T::class.java)
