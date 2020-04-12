package com.github.oowekyala.ooxml.messages;

import java.util.logging.Logger;

import com.github.oowekyala.ooxml.messages.XmlException.Severity;

/**
 * Implements {@link XmlMessageHandler} with a {@link Logger}
 * as back-end.
 */
public class LoggerMessageHandler implements XmlMessageHandler {

    private final Logger logger;
    private final boolean supportsColor;


    public LoggerMessageHandler(Logger logger, boolean supportsColor) {
        this.logger = logger;
        this.supportsColor = supportsColor;
    }


    @Override
    public boolean supportsAnsiColors() {
        return supportsColor;
    }


    @Override
    public void printMessageLn(XmlMessageKind kind, Severity severity, String message) {
        logger.log(severity.toJutilLevel(), message);

    }
}
