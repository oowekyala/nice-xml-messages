package com.github.oowekyala.rset.xml

import io.kotlintest.shouldBe
import io.kotlintest.specs.FunSpec
import org.w3c.dom.Document


class OutputTest : FunSpec({


    test("Test simple string conversions") {
        val doc = DomIoUtils().makeDoc("oha!", defaultSer<String>())

        doc.toStr() shouldBe """
            $HEADER
            <str>oha!</str>
        """.trimIndent()
    }

    test("Test composition") {
        val doc = DomIoUtils().makeDoc(
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
