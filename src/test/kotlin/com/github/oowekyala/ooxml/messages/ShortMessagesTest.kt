package com.github.oowekyala.ooxml.messages

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.xml.sax.InputSource
import javax.xml.parsers.DocumentBuilderFactory


class ShortMessagesTest : FunSpec({

    fun String.parseStr(handler: TestMessagePrinter,
                        builderConfig: DocumentBuilderFactory.() -> Unit = {}): PositionedXmlDoc {


        val builder =
                DocumentBuilderFactory.newInstance()
                        .apply(builderConfig)
                        .newDocumentBuilder()


        val isource = InputSource(reader()).apply {
            systemId = "/test/File.xml"
        }

        return XmlMessageUtils.getInstance().parse(builder, isource, handler)
    }

    test("Test attribute node") {

        val expected = """
$HEADER
<list>
    <list foo="&amp;"/>
</list>
        """.trimIndent()

        val printer = TestMessagePrinter()

        val xmlDoc = expected.parseStr(printer)

        val reporter = DefaultXmlErrorReporter(printer, xmlDoc.positioner.withShortMessages())


        val attr =
                xmlDoc.document
                        .documentElement
                        .childNodes
                        .item(1)
                        .attributes
                        .getNamedItem("foo")

        reporter.error(attr, "Give better names plz")


        printer.err[0].message shouldBe "Error at /test/File.xml:3:11 - Give better names plz"
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

        val reporter = DefaultXmlErrorReporter(printer, xmlDoc.positioner.withShortMessages())


        val attr =
                xmlDoc.document
                        .documentElement
                        .childNodes
                        .item(1)
                        .attributes
                        .getNamedItem("xsi:foo")!!

        reporter.error(attr, "Give better names plz")


        printer.err[0].message shouldBe "Error at /test/File.xml:5:9 - Give better names plz"
    }

})
