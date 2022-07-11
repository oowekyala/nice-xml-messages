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
import io.kotest.matchers.collections.shouldBeEmpty
import org.xml.sax.InputSource
import java.io.Reader
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory

class CornerCases : IntelliMarker, FunSpec({


    fun domBuilder(): DocumentBuilder =
            DocumentBuilderFactory.newInstance().newDocumentBuilder()

    fun Reader.parseStr(handler: TestMessagePrinter): PositionedXmlDoc =
            OoxmlFacade()
                .withPrinter(handler).parse(domBuilder(), InputSource(this))

    test("Test stackoverflow") {

        val printer = TestMessagePrinter()

        val reader  = CornerCases::class.java
            .getResourceAsStream("stackOverflowTestCase.xml")!!
            .bufferedReader()


        val doc = reader.parseStr(printer)
        val lastChild = doc.document.documentElement.lastChild
        doc.positioner.startPositionOf(lastChild)
        printer.err.shouldBeEmpty()
    }

})
