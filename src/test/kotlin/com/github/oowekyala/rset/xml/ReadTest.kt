package com.github.oowekyala.rset.xml

import io.kotlintest.shouldBe
import io.kotlintest.specs.FunSpec


class ReadTest : FunSpec({


    test("Test simple string conversions") {
        val doc = DomUtils().makeDoc("oha!", defaultSer<String>())

        doc.toStr() shouldBe """
            $HEADER
            <str>oha!</str>
        """.trimIndent()
    }

    test("Test composition") {
        val doc = DomUtils().makeDoc(
                listOf("oha", "what", "are", "you"),
                defaultSer<String>().toSeq<List<String>> { mutableListOf() }
        )

        doc.toStr() shouldBe """
$HEADER
<seq>
    <str>oha</str>
    <str>what</str>
    <str>are</str>
    <str>you</str>
</seq>
        """.trimIndent()
    }

    test("Test read/write") {

        val ser = defaultSer<String>().toList().toList()
        val list = listOf(
                listOf("oha", "what"),
                listOf("are", "you")
        )
        val doc = DomUtils().makeDoc(list, ser)

        val expected = """
$HEADER
<list>
    <list>
        <str>oha</str>
        <str>what</str>
    </list>
    <list>
        <str>are</str>
        <str>you</str>
    </list>
</list>
        """.trimIndent()

        doc.toStr() shouldBe expected


        val printer = TestMessagePrinter()
        DomUtils.parse(expected.reader(), ser, DefaultErrorReporter(printer))
                .shouldBe(list)

        printer.shouldBeEmpty()


    }


    test("Test list item wrong name") {

        val ser = defaultSer<String>().toList().toList()

        val expected = """
$HEADER
<list>
    <list>
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
        DomUtils.parse(expected.reader(), ser, DefaultErrorReporter(printer))
                .shouldBe(listOf(listOf("oha", "what")))

        printer.warn.shouldBe(
                listOf(
                        """
    5|         <str>what</str>
    6|     </list>
    7|     <moh>
           ^ Wrong name, expecting 'list'


    8|         <str>are</str>
    9|         <str>you</str>
   10|     </moh>
                        """.trimIndent()
                )
        )
    }

})

