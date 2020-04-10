package com.github.oowekyala.ooxml.messages

import com.github.oowekyala.ooxml.MinXPath
import io.kotlintest.matchers.haveSize
import io.kotlintest.matchers.numerics.shouldBeGreaterThan
import io.kotlintest.matchers.withClue
import io.kotlintest.should
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import io.kotlintest.specs.FunSpec
import org.w3c.dom.Node
import org.xml.sax.InputSource
import java.lang.IllegalArgumentException
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.streams.toList

/**
 *
 */
class MinXPathTest : FunSpec({

    fun String.parseStr(builderConfig: DocumentBuilderFactory.() -> Unit = {}): PositionedXmlDoc {


        val builder =
                DocumentBuilderFactory.newInstance()
                        .apply(builderConfig)
                        .newDocumentBuilder()


        val isource = InputSource(reader()).apply {
            systemId = "/test/File.xml"
        }

        return XmlErrorUtils.getInstance().parse(builder, isource, TestMessagePrinter())
    }


    test("Test simple minixpath") {


        val doc = """
<?xml version="1.0" encoding="UTF-8"?>

<bookstore>
  <book stub="true" />
  <book>
    <title lang="en">Harry Potter</title>
    <author>J K. Rowling</author>
    <year>2005</year>
    <price>29.99</price>
  </book>
</bookstore>
        """.trimIndent()

        fun PositionedXmlDoc.fetch(xpath: String, expectedSize: Int): List<Node> {
            return MinXPath.parse(xpath)
                    .evaluate(document)
                    .toList().also {
                        withClue("Result of XPath expr $xpath") {
                            it.should(haveSize(expectedSize))
                        }
                    }

        }

        with(doc.parseStr()) {

            fetch("bookstore/book[1]", 1).let { (book) ->
                book.childNodes.length shouldBe 0
            }

            fetch("bookstore/book[2]", 1).let { (book) ->
                book.childNodes.length.shouldBeGreaterThan(0)
            }

            fetch("bookstore/book[@stub ='true']", 1).let { (book) ->
                book.childNodes.length shouldBe 0
            }
        }
    }


    test("Test parse errors") {


        withClue("Empty string") {
            shouldThrow<IllegalArgumentException> {
                MinXPath.parse("")
            }
        }

        withClue("Incomplete expr") {
            shouldThrow<IllegalArgumentException> {
                MinXPath.parse("a/")
            }
        }
        withClue("Trailing stuff in expr") {
            shouldThrow<IllegalArgumentException> {
                MinXPath.parse("a $")
            }
        }
        withClue("Unclosed string") {
            shouldThrow<IllegalArgumentException> {
                MinXPath.parse("a[@a='b]")
            }
        }
        withClue("Unclosed predicate") {
            shouldThrow<IllegalArgumentException> {
                MinXPath.parse("a[@a='b'")
            }
        }
    }


})