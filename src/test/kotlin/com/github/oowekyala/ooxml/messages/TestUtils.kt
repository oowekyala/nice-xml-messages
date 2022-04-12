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

import io.kotest.assertions.fail
import io.kotest.matchers.collections.shouldBeEmpty


const val HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>"


class TestMessagePrinter(private val useColors: Boolean = false) : XmlMessageHandler {

    val warn = mutableListOf<XmlException>()
    val err = mutableListOf<XmlException>()
    val out = mutableListOf<XmlException>()
    val debug = mutableListOf<XmlException>()


    override fun printMessageLn(kind: XmlMessageKind, severity: XmlException.XmlSeverity, message: String) {
        TODO()
    }

    override fun accept(entry: XmlException) {
        when (entry.severity) {
            XmlException.XmlSeverity.INFO    -> out += entry
            XmlException.XmlSeverity.DEBUG   -> debug += entry
            XmlException.XmlSeverity.WARNING -> warn += entry
            XmlException.XmlSeverity.ERROR   -> err += entry
            XmlException.XmlSeverity.FATAL   -> err += entry
            else                                                                                    -> fail("impossible")
        }
    }


    override fun supportsAnsiColors(): Boolean = useColors

    fun shouldBeEmpty() {
        warn.shouldBeEmpty()
        err.shouldBeEmpty()
        out.shouldBeEmpty()
    }
}
