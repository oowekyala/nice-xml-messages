package com.github.oowekyala.ooxml.messages;

import java.util.function.Consumer;

/**
 * Barebones printer for exceptions.
 *
 * @author Clément Fournier
 */
public interface XmlMessageHandler extends Consumer<XmlException> {


    boolean supportsAnsiColors();


    @Override
    void accept(XmlException entry);

}
