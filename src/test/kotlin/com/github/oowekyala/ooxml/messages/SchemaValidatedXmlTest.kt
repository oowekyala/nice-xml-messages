package com.github.oowekyala.ooxml.messages

import io.kotlintest.matchers.collections.shouldContainExactly
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

    fun domBuilder(): DocumentBuilder =
            DocumentBuilderFactory.newInstance().apply {
                isNamespaceAware = true
                isValidating = true

                setFeature("http://apache.org/xml/features/validation/schema", true)
            }.newDocumentBuilder()

    fun String.parseStr(handler: TestMessagePrinter): PositionedXmlDoc =
            XmlErrorUtils.getInstance().parse(domBuilder(), InputSource(reader()), handler)


    test("Test wrong schema") {

        val expected = """
$HEADER
<note xmlns="$schemaNs" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="$schemaNs $schemaLoc">
    <foo />

</note>
        """.trimIndent()

        val printer = TestMessagePrinter()

        expected.parseStr(printer)

        printer.err.map { it.toString() }.shouldContainExactly("""
            Error (XML parsing)
                1| <?xml version="1.0" encoding="UTF-8" standalone="no"?>
                2| <note xmlns="https://com.github.oowekyala/ooxml/tests" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="https://com.github.oowekyala/ooxml/tests file:/home/clifrr/Documents/Git/ooxml/target/test-classes/com/github/oowekyala/ooxml/messages/SampleSchema.xsd">
                3|     <foo />
                              ^ cvc-complex-type.2.4.a: Invalid content was found starting with element '{"https://com.github.oowekyala/ooxml/tests":foo}'. One of '{to}' is expected.


                4| 
                5| </note>
        """.trimIndent()
        )

    }

})
