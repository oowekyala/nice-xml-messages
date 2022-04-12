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
import io.kotest.matchers.types.shouldBeInstanceOf
import org.w3c.dom.Element


class UserValidationTest : IntelliMarker, FunSpec({


    test("Test attribute node") {

        val expected = """
$HEADER
<list>
    <list foo="&amp;"/>
</list>
        """.trimIndent()

        with(OoxmlFixture()) {

            val xmlDoc = expected.parseStr()

            val reporter = newReporter(xmlDoc.positioner)


            val attr =
                xmlDoc.document
                    .documentElement
                    .childNodes
                    .item(1)
                    .attributes
                    .getNamedItem("foo")

            reporter.at(attr).error("Give better names plz")


            printer.err[0].message shouldBe """
Error at /test/File.xml:3:11
 1| $HEADER
 2| <list>
 3|     <list foo="&amp;"/>
              ^^^ Give better names plz

 4| </list>
""".trimIndent()
        }


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

        with(OoxmlFixture()) {

            val xmlDoc = expected.parseStr()

            val reporter = newReporter(xmlDoc.positioner)


            val attr =
                xmlDoc.document
                    .documentElement
                    .childNodes
                    .item(1)
                    .attributes
                    .getNamedItem("xsi:foo")!!

            reporter.at(attr).error("Give better names plz")


            printer.err[0].message shouldBe """
Error at /test/File.xml:5:9
 3|     <list xmlns:xsi="fooo" xmlns:foo="ah!"
 4|             foo:oha ='a'
 5|         xsi:foo="&amp;" />
            ^^^^^^^ Give better names plz

 6| </list>
""".trimIndent()

        }
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

        with(OoxmlFixture()) {

            val xmlDoc = expected.parseStr()

            val reporter = newReporter(xmlDoc.positioner)


            val attr =
                xmlDoc.document
                    .documentElement
                    .childNodes
                    .item(1)
                    .attributes
                    .getNamedItem("foo")!!

            reporter.at(attr).error("Give better names plz")


            printer.err[0].message shouldBe """
Error at /test/File.xml
 4|           xmlns="ah!"
 5|           foo:oha ='a'
 6|           foo="&amp;" />
              ^^^ Give better names plz

 7| </list>
""".trimIndent()
        }

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

        with(OoxmlFixture()) {

            val xmlDoc = expected.parseStr()

            val reporter = newReporter(xmlDoc.positioner)


            val attr =
                xmlDoc.document
                    .documentElement
                    .childNodes
                    .item(1)!!

            reporter.at(attr).error("Give better names plz")


        printer.err[0].message shouldBe """
Error at /test/File.xml
 1| <?xml version="1.0" encoding="UTF-8" standalone="no"?>
 2| <list xmlns:xsi="fooo" xmlns:foo="ah!">
 3|     <foo:list 
        ^^^^^^^^^ Give better names plz

 4|             foo:foo ='a'
 5|             xsi:foo="&amp;" />
""".trimIndent()


        }
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

        with(OoxmlFixture()) {

            val xmlDoc = expected.parseStr()

            val reporter = newReporter(xmlDoc.positioner)

            val text =
                xmlDoc.document
                    .documentElement
                    .also {
                        it.tagName shouldBe "list"
                    }
                    .childNodes
                    .item(0)!!

            reporter.at(text).error("Give better names plz")

            printer.err[0].message shouldBe """
Error at /test/File.xml
 1| $HEADER
 2| <list>
          ^ Give better names plz

 3|     text
 4|     <![CDATA[
""".trimIndent()

        }
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

        with(OoxmlFixture()) {

            val xmlDoc = expected.parseStr()

            val reporter = newReporter(xmlDoc.positioner)

            val text =
                xmlDoc.document
                    .documentElement
                    .childNodes
                    .item(1)!!

            reporter.at(text).error("Give better names plz")


            printer.err[0].message shouldBe """
Error at /test/File.xml
 2| <list>
 3|     text
 4|     <![CDATA[
        ^ Give better names plz

 5|     cdata
 6|     ]]>
        """.trimIndent()

        }
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

        with(OoxmlFixture()) {

            val xmlDoc = expected.parseStr {
                isCoalescing = true
            }

            val reporter = newReporter(xmlDoc.positioner)

            val mixedElt =
                xmlDoc.document
                    .documentElement
                    .childNodes
                    .item(1)

            mixedElt.shouldBeInstanceOf<Element>()

            reporter.at(mixedElt).error("Give better names plz")


            printer.err[0].message shouldBe """
Error at /test/File.xml
 5|     cdata
 6|     ]]>
 7|     <mixed />
        ^^^^^^ Give better names plz

 8| </list>
""".trimIndent()
        }
    }


})
