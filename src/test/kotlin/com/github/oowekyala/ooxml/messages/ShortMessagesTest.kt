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
