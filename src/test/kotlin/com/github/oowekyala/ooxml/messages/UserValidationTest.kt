package com.github.oowekyala.ooxml.messages

import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.specs.FunSpec
import org.xml.sax.InputSource
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory


class UserValidationTest : FunSpec({


    fun domBuilder(): DocumentBuilder =
            DocumentBuilderFactory.newInstance().newDocumentBuilder()

    fun String.parseStr(handler: TestMessagePrinter): PositionedXmlDoc =
            XmlErrorUtils.getInstance().parse(domBuilder(), InputSource(reader()).apply { systemId = "/test/File.xml" }, handler)

    test("Test attribute node") {

        val expected = """
$HEADER
<list>
    <list foo="&amp;"/>
</list>
        """.trimIndent()

        val printer = TestMessagePrinter()

        val xmlDoc = expected.parseStr(printer)

        val reporter = DefaultXmlErrorReporter(printer, xmlDoc.positioner)


        val attr =
                xmlDoc.document
                        .documentElement
                        .childNodes
                        .item(1)
                        .attributes
                        .getNamedItem("foo")

        reporter.error(attr, "Give better names plz")


        printer.err.map { it.message }.shouldContainExactly(
                """
Error (XML validation) in /test/File.xml
    1| $HEADER
    2| <list>
    3|     <list foo="&amp;"/>
                 ^ Give better names plz


    4| </list>
""".trimIndent()
        )

    }


    test("Test attribute node with qnames") {

        val expected = """
$HEADER
<list>
    <list xmlns:xsi="fooo" xmlns:foo="ah!"
            foo:oha ='a'
        xsi:foo="&amp;" />
</list>
        """.trimIndent()

        val printer = TestMessagePrinter()

        val xmlDoc = expected.parseStr(printer)

        val reporter = DefaultXmlErrorReporter(printer, xmlDoc.positioner)


        val attr =
                xmlDoc.document
                        .documentElement
                        .childNodes
                        .item(1)
                        .attributes
                        .getNamedItem("xsi:foo")!!

        reporter.error(attr, "Give better names plz")


        printer.err.map { it.message }.shouldContainExactly(
                """
Error (XML validation) in /test/File.xml
    3|     <list xmlns:xsi="fooo" xmlns:foo="ah!"
    4|             foo:oha ='a'
    5|         xsi:foo="&amp;" />
               ^ Give better names plz


    6| </list>
""".trimIndent()
        )

    }


})
