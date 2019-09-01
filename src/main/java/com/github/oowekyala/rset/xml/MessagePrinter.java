package com.github.oowekyala.rset.xml;

import com.github.oowekyala.rset.xml.ErrorReporter.Message;

/**
 * @author Clément Fournier
 */
public interface MessagePrinter {


    default void printlnErr(Message msg) {
        System.err.println(msg);
    }


    default void println(Message msg) {
        System.out.println(msg);
    }

}
