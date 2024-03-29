package com.github.oowekyala.ooxml.messages

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.w3c.dom.Element
import org.xml.sax.InputSource
import javax.xml.parsers.DocumentBuilderFactory


class UserValidationTest : FunSpec({

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

        val reporter = DefaultXmlErrorReporter(printer, xmlDoc.positioner)


        val attr =
                xmlDoc.document
                        .documentElement
                        .childNodes
                        .item(1)
                        .attributes
                        .getNamedItem("foo")

        reporter.error(attr, "Give better names plz")


        printer.err[0].message shouldBe """
Error (XML validation) in /test/File.xml
 1| $HEADER
 2| <list>
 3|     <list foo="&amp;"/>
              ^^^ Give better names plz

 4| </list>
""".trimIndent()


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

        val reporter = DefaultXmlErrorReporter(printer, xmlDoc.positioner)


        val attr =
                xmlDoc.document
                        .documentElement
                        .childNodes
                        .item(1)
                        .attributes
                        .getNamedItem("xsi:foo")!!

        reporter.error(attr, "Give better names plz")


        printer.err[0].message shouldBe """
Error (XML validation) in /test/File.xml
 3|     <list xmlns:xsi="fooo" xmlns:foo="ah!"
 4|             foo:oha ='a'
 5|         xsi:foo="&amp;" />
            ^^^^^^^ Give better names plz

 6| </list>
""".trimIndent()


    }




    test("Test attribute node with implicit qnames") {

        val expected = """
$HEADER
<list>
    <list xmlns:xsi="fooo" 
          xmlns="ah!"
          foo:oha ='a'
          foo="&amp;" />
</list>
        """.trimIndent()

        val printer = TestMessagePrinter()

        val xmlDoc = expected.parseStr(printer)

        val reporter = DefaultXmlErrorReporter(printer, xmlDoc.positioner)


        val attr =
                xmlDoc.document
                        .documentElement
                        .childNodes
                        .item(1)
                        .attributes
                        .getNamedItem("foo")!!

        reporter.error(attr, "Give better names plz")


        printer.err[0].message shouldBe """
Error (XML validation) in /test/File.xml
 4|           xmlns="ah!"
 5|           foo:oha ='a'
 6|           foo="&amp;" />
              ^^^ Give better names plz

 7| </list>
""".trimIndent()


    }



    test("Test element nodes with qnames") {

        val expected = """
$HEADER
<list xmlns:xsi="fooo" xmlns:foo="ah!">
    <foo:list 
            foo:foo ='a'
            xsi:foo="&amp;" />
</list>
        """.trimIndent()

        val printer = TestMessagePrinter()

        val xmlDoc = expected.parseStr(printer)

        val reporter = DefaultXmlErrorReporter(printer, xmlDoc.positioner)


        val attr =
                xmlDoc.document
                        .documentElement
                        .childNodes
                        .item(1)!!

        reporter.error(attr, "Give better names plz")


        printer.err[0].message shouldBe """
Error (XML validation) in /test/File.xml
 1| <?xml version="1.0" encoding="UTF-8" standalone="no"?>
 2| <list xmlns:xsi="fooo" xmlns:foo="ah!">
 3|     <foo:list 
        ^^^^^^^^^ Give better names plz

 4|             foo:foo ='a'
 5|             xsi:foo="&amp;" />
""".trimIndent()


    }


    test("Test text nodes") {

        val expected = """
$HEADER
<list>
    text
    <![CDATA[
    cdata
    ]]>
</list>
        """.trimIndent()

        val printer = TestMessagePrinter()

        val xmlDoc = expected.parseStr(printer)

        val reporter = DefaultXmlErrorReporter(printer, xmlDoc.positioner)

        val text =
                xmlDoc.document
                        .documentElement
                        .also {
                            it.tagName shouldBe "list"
                        }
                        .childNodes
                        .item(0)!!

        reporter.error(text, "Give better names plz")

        printer.err[0].message shouldBe """
Error (XML validation) in /test/File.xml
 1| $HEADER
 2| <list>
          ^ Give better names plz

 3|     text
 4|     <![CDATA[
""".trimIndent()


    }


    test("Test non-coalescing cdata sections") {

        val expected = """
$HEADER
<list>
    text
    <![CDATA[
    cdata
    ]]>
</list>
        """.trimIndent()

        val printer = TestMessagePrinter()

        val xmlDoc = expected.parseStr(printer)

        val reporter = DefaultXmlErrorReporter(printer, xmlDoc.positioner)

        val text =
                xmlDoc.document
                        .documentElement
                        .childNodes
                        .item(1)!!

        reporter.error(text, "Give better names plz")


        printer.err[0].message shouldBe """
Error (XML validation) in /test/File.xml
 2| <list>
 3|     text
 4|     <![CDATA[
        ^ Give better names plz

 5|     cdata
 6|     ]]>
        """.trimIndent()

    }



    test("Test coalescing cdata sections") {

        val expected = """
$HEADER
<list>
    text
    <![CDATA[
    cdata
    ]]>
    <mixed />
</list>
        """.trimIndent()

        val printer = TestMessagePrinter()

        val xmlDoc =
                expected.parseStr(printer) {
                    isCoalescing = true
                }

        val reporter = DefaultXmlErrorReporter(printer, xmlDoc.positioner)

        val mixedElt =
                xmlDoc.document
                        .documentElement
                        .childNodes
                        .item(1)

        mixedElt.shouldBeInstanceOf<Element>()

        reporter.error(mixedElt, "Give better names plz")


        printer.err[0].message shouldBe """
Error (XML validation) in /test/File.xml
 5|     cdata
 6|     ]]>
 7|     <mixed />
        ^^^^^^ Give better names plz

 8| </list>
""".trimIndent()

    }


})
