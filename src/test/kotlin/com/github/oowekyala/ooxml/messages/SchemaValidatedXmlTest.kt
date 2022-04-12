/*
 * MIT License
 *
 * Copyright (c) 2022 Clément Fournier
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.github.oowekyala.ooxml.messages

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.string.shouldStartWith
import org.xml.sax.InputSource
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory

/*
   Error messages are localized, locale is set to english in pom.xml
 */


class SchemaValidatedXmlTest : FunSpec({


    val schemaNs = "https://com.github.oowekyala/ooxml/tests"

    val schemaLoc = javaClass.getResource("SampleSchema.xsd")

    fun domBuilder(): DocumentBuilder =
            DocumentBuilderFactory.newInstance().apply {
                isNamespaceAware = true
                isValidating = true

                setFeature("http://apache.org/xml/features/validation/schema", true)
            }.newDocumentBuilder()

    fun String.parseStr(handler: TestMessagePrinter): PositionedXmlDoc =
        OoxmlFacade().withPrinter(handler).parse(domBuilder(), InputSource(reader()))


    test("Test wrong schema") {

        val expected = """
$HEADER
<note xmlns="$schemaNs" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="$schemaNs $schemaLoc">
    <foo />

</note>
        """.trimIndent()

        val printer = TestMessagePrinter()

        expected.parseStr(printer)

        printer.err.map { it.toString() }.single().shouldStartWith("""
Error (Schema validation)
 1| <?xml version="1.0" encoding="UTF-8" standalone="no"?>
 2| <note xmlns="$schemaNs" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="$schemaNs $schemaLoc">
 3|     <foo />
               ^ cvc-complex-type.2.4.a: Invalid content was found
        """.trimIndent()
        )

    }

})
