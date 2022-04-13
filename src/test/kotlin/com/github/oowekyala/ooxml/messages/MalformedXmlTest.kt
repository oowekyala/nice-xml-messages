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


class MalformedXmlTest : IntelliMarker, FunSpec({


    fun domBuilder(): DocumentBuilder =
            DocumentBuilderFactory.newInstance().newDocumentBuilder()

    fun String.parseStr(handler: TestMessagePrinter): PositionedXmlDoc =
            OoxmlFacade()
                .withPrinter(handler).parse(domBuilder(), InputSource(reader()))

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
Error (XML parsing)
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
Error (XML parsing)
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
"""Error (XML parsing)
 1| <?xml version="1.0" encoding="UTF-8" standalone="no"?>
                                                          ^ Premature end of file.

""".trimIndent()
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
"""Error (XML parsing)
 1| 
    ^ Premature end of file.

""".trimIndent()
        )

        printer.err.shouldContainExactly(ex)
    }

})
