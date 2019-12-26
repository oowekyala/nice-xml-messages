package com.github.oowekyala.ooxml.messages

import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import io.kotlintest.specs.FunSpec


class MalformedXmlTest : FunSpec({

    test("Test malformed xml 1") {

        val expected = """
$HEADER
<list>
    <list
        <str>oha</str>
        <str>what</str>
    </list>
    <moh>
        <str>are</str>
        <str>you</str>
    </moh>
</list>
        """.trimIndent()

        val printer = TestMessagePrinter()

        val ex = shouldThrow<XmlParseException> {
            expected.parseStr(printer)
        }

        ex.toString().shouldBe(
                """
XML parsing error
    2| <list>
    3|     <list
    4|         <str>oha</str>
               ^ Le type d'élément "list" doit être suivi des spécifications d'attribut, ">" ou "/>".


    5|         <str>what</str>
    6|     </list>
""".trimIndent()

        )

        printer.err.shouldBe(emptyList<String>())
    }

    test("Test malformed entities") {

        val expected = """
$HEADER
<list>
    <list foo="&amb;"/>
</list>
        """.trimIndent()

        val printer = TestMessagePrinter()

        val ex = shouldThrow<XmlParseException> {
            expected.parseStr(printer)
        }

        ex.toString().shouldBe(
                """
XML parsing error
    1| $HEADER
    2| <list>
    3|     <list foo="&amb;"/>
                           ^ L'entité "amb" était référencée, mais pas déclarée.


    4| </list>
""".trimIndent()

        )

        printer.err.shouldBe(emptyList<String>())
    }

    test("Test empty document") {

        val expected = """
$HEADER
""".trimIndent()

        val printer = TestMessagePrinter()

        val ex = shouldThrow<XmlParseException> {
            expected.parseStr(printer)
        }

        ex.toString().shouldBe(
"""XML parsing error
    1| <?xml version="1.0" encoding="UTF-8" standalone="no"?>
                                                             ^ Fin prématurée du fichier.

"""
        )

        printer.err.shouldBe(emptyList<String>())
    }

})
