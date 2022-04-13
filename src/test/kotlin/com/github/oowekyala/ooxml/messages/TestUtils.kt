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

import com.github.oowekyala.ooxml.messages.XmlSeverity.*
import io.kotest.matchers.collections.shouldBeEmpty
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.condition.EnabledIfSystemProperty
import org.junit.jupiter.api.fail
import org.xml.sax.InputSource
import java.text.MessageFormat
import java.util.function.Consumer
import javax.xml.parsers.DocumentBuilderFactory


const val HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>"


class OoxmlFixture(val ooxml: OoxmlFacade = OoxmlFacade()) {

    val printer: TestMessagePrinter = TestMessagePrinter().also { ooxml.withPrinter(it) }

    fun newReporter(
        positioner: XmlPositioner
    ): TestXmlReporter = TestXmlReporter(ooxml, positioner)

    fun String.parseStr(
        builderConfig: DocumentBuilderFactory.() -> Unit = {}
    ): PositionedXmlDoc {

        val builder =
            DocumentBuilderFactory.newInstance()
                .apply(builderConfig)
                .newDocumentBuilder()


        val isource = InputSource(this.reader()).apply {
            systemId = "/test/File.xml"
        }

        return ooxml.parse(builder, isource)
    }

}

class SimpleMessageFacade(
    val ooxml: OoxmlFacade,
    val pos: XmlPosition,
    val positioner: XmlPositioner,
    val callback: (XmlException) -> Unit
) {


    fun warn(message: String, vararg args: Any?) {
        message(WARNING, message, args)
    }

    fun error(message: String, vararg args: Any?) {
        message(ERROR, message, args)
    }

    fun message(severity: XmlSeverity, message: String, vararg args: Any?) {
        val spec = NiceXmlMessageSpec(pos, MessageFormat.format(message, args))
        spec.withSeverity(severity)
        spec.withKind(null)
        val fullMessage = ooxml.formatter.formatSpec(spec, positioner)
        callback(XmlException(spec, fullMessage))
    }
}


class TestMessagePrinter : XmlMessageHandler {

    val warn = mutableListOf<XmlException>()
    val err = mutableListOf<XmlException>()


    override fun printMessageLn(kind: @Annots.Nullable String, severity: XmlSeverity, message: String) {
        TODO()
    }

    override fun accept(entry: XmlException) {
        when (entry.severity) {
            WARNING -> warn += entry
            ERROR   -> err += entry
            else    -> fail("impossible")
        }
    }


    fun shouldBeEmpty() {
        warn.shouldBeEmpty()
        err.shouldBeEmpty()
    }
}

class TestXmlReporter(
    ooxml: OoxmlFacade,
    positioner: XmlPositioner,
) : XmlMessageReporterBase<SimpleMessageFacade>(ooxml, positioner) {

    override fun create2ndStage(
        position: XmlPosition,
        positioner: XmlPositioner,
        handleEx: Consumer<XmlException>
    ): SimpleMessageFacade {
        return SimpleMessageFacade(ooxml, position, positioner) { handleEx(it) }
    }

}

interface IntelliMarker {
    @EnabledIfSystemProperty(named = "wibble", matches = "wobble")
    @TestFactory
    fun primer() {
    }
}
