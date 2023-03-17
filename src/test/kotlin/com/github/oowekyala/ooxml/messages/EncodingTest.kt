/*
 * MIT License
 *
 * Copyright (c) 2023 Andreas Dangel
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
import io.kotest.matchers.collections.shouldBeEmpty
import org.xml.sax.InputSource
import java.io.InputStream
import java.nio.charset.Charset
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.test.assertEquals

class EncodingTest : IntelliMarker, FunSpec({


    fun domBuilder(): DocumentBuilder =
            DocumentBuilderFactory.newInstance().newDocumentBuilder()

    fun InputStream.parseStr(handler: TestMessagePrinter): PositionedXmlDoc =
            OoxmlFacade()
                .withPrinter(handler).parse(domBuilder(), InputSource(this))

    test("Test InputStream with encoding") {
        // make sure, the current platform encoding is UTF-8.
        // it must not be ISO-8859-1 - that's what the test file is using.
        assertEquals("UTF-8", Charset.defaultCharset().name())

        val printer = TestMessagePrinter()

        val stream  = EncodingTest::class.java.getResourceAsStream("latin1.xml")

        val doc = stream.parseStr(printer)
        assertEquals("ISO-8859-1", doc.document.xmlEncoding)
        assertEquals("With Umlauts: ä", doc.document.getElementsByTagName("message").item(0).textContent)
        printer.err.shouldBeEmpty()
    }

})
