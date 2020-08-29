package com.github.oowekyala.ooxml.messages

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import org.xml.sax.InputSource
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory

/*
   Error messages are localized, locale is set to english in pom.xml
 */


class MalformedXmlTest : FunSpec({


    fun domBuilder(): DocumentBuilder =
            DocumentBuilderFactory.newInstance().newDocumentBuilder()

    fun String.parseStr(handler: TestMessagePrinter): PositionedXmlDoc =
            XmlMessageUtils.getInstance().parse(domBuilder(), InputSource(reader()), handler)

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

        val ex = shouldThrow<XmlException> {
            expected.parseStr(printer)
        }

        ex.toString().shouldBe(
                """
Fatal error (XML parsing)
 2| <list>
 3|     <list
 4|         <str>oha</str>
            ^ Element type "list" must be followed by either attribute specifications, ">" or "/>".

 5|         <str>what</str>
 6|     </list>
""".trimIndent()

        )

        printer.err.shouldContainExactly(ex)
    }

    test("Test malformed entities") {

        val expected = """
$HEADER
<list>
    <list foo="&amb;"/>
</list>
        """.trimIndent()

        val printer = TestMessagePrinter()

        val ex = shouldThrow<XmlException> {
            expected.parseStr(printer)
        }

        ex.toString().shouldBe(
                """
Fatal error (XML parsing)
 1| $HEADER
 2| <list>
 3|     <list foo="&amb;"/>
                        ^ The entity "amb" was referenced, but not declared.

 4| </list>
""".trimIndent()

        )

        printer.err.shouldContainExactly(ex)

    }

    test("Test no root node") {

        val expected = """
$HEADER
""".trimIndent()

        val printer = TestMessagePrinter()

        val ex = shouldThrow<XmlException> {
            expected.parseStr(printer)
        }

        ex.toString().shouldBe(
"""Fatal error (XML parsing)
 1| <?xml version="1.0" encoding="UTF-8" standalone="no"?>
                                                          ^ Premature end of file.

""".trim()
        )

        printer.err.shouldContainExactly(ex)
    }
    test("Test empty document") {

        val code = ""

        val printer = TestMessagePrinter()

        val ex = shouldThrow<XmlException> {
            code.parseStr(printer)
        }

        ex.toString().shouldBe(
"""Fatal error (XML parsing)
 1| 
    ^ Premature end of file.

""".trim()
        )

        printer.err.shouldContainExactly(ex)
    }

})
