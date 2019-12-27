package com.github.oowekyala.ooxml.messages

import io.kotlintest.shouldBe
import io.kotlintest.specs.FunSpec
import org.xml.sax.InputSource
import javax.xml.XMLConstants
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.validation.SchemaFactory

/*
   Error messages are localized, locale is set to english in pom.xml
 */


class SchemaValidatedXmlTest : FunSpec({


    val schemaNs = "https://com.github.oowekyala/ooxml/tests"

    val schemaLoc = SchemaValidatedXmlTest::class.java.getResource("SampleSchema.xsd")
    val sampleSchema =
            SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)
                    .newSchema(schemaLoc)

    fun domBuilder(): DocumentBuilder =
            DocumentBuilderFactory.newInstance().apply {
                isNamespaceAware = true
                schema = sampleSchema
                isValidating = true

                setFeature("http://apache.org/xml/features/validation/schema", true)
            }.newDocumentBuilder()

    fun String.parseStr(handler: TestMessagePrinter): PositionedXmlDoc =
            XmlErrorUtils.getInstance().parse(domBuilder(), InputSource(reader()), handler.asMessageHandler())


    test("Test wrong schema") {

        val expected = """
$HEADER
<note xmlns="$schemaNs" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="$schemaNs $schemaLoc">
    <foo />

</note>
        """.trimIndent()

        val printer = TestMessagePrinter()

        "".reader().readText()

        expected.parseStr(printer)

        printer.err.shouldBe(listOf(
                ""
        ))

    }

})
