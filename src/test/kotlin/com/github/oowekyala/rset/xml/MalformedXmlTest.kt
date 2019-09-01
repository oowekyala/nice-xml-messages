package com.github.oowekyala.rset.xml

import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import io.kotlintest.specs.FunSpec


class MalformedXmlTest : FunSpec({

    test("Test malformed xml 1") {

        val ser = defaultSer<String>().toList().toList()

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

        val ex = shouldThrow<ErrorReporter.XmlParsingException> {
            DomIoUtils.parse(expected.reader(), ser, DefaultErrorReporter(printer))
        }

        // FIXME this uses French Locale!
        ex.toString().shouldBe(
                """
XML parsing error
    1| <list>
    2|     <list
                ^^^ Le type d'élément "list" doit être suivi des spécifications d'attribut, ">" ou "/>".


    3|         <str>oha</str>
    4|         <str>what</str>
    5|     </list>
""".trimIndent()

        )

        printer.err.shouldBe(emptyList<String>())
    }

})
