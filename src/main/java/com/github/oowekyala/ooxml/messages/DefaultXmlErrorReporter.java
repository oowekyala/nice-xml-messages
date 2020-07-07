package com.github.oowekyala.ooxml.messages;

import static com.github.oowekyala.ooxml.messages.XmlMessageKind.StdMessageKind.USER_VALIDATION;

import java.text.MessageFormat;

import org.w3c.dom.Node;

import com.github.oowekyala.ooxml.messages.Annots.Nullable;
import com.github.oowekyala.ooxml.messages.XmlException.Severity;

/**
 * Simple implementation of an error reporter. Only needs a
 * {@link XmlMessageHandler} and an {@link XmlPositioner}.
 */
public class DefaultXmlErrorReporter implements XmlErrorReporter {

    protected final XmlPositioner positioner;
    protected final XmlMessageHandler printer;

    public DefaultXmlErrorReporter(XmlMessageHandler printer, XmlPositioner positioner) {
        this.positioner = positioner;
        this.printer = printer;
    }

    protected void handle(XmlException ex, String message) {
        printer.accept(ex);
    }


    @Override
    public void warn(Node node, String message, Object... args) {
        InternalUtil.assertParamNotNull("message", message);
        XmlException ex = createEntry(node, XmlException.Severity.WARNING, template(message, args), null);
        handle(ex, message);
    }

    @Override
    public XmlException error(Node node, String message, Object... args) {
        InternalUtil.assertParamNotNull("message", message);
        XmlException ex = createEntry(node, XmlException.Severity.ERROR, template(message, args), null);
        handle(ex, message);
        return ex;
    }

    @Override
    public XmlException error(Node node, Throwable cause) {
        InternalUtil.assertParamNotNull("cause", cause);
        XmlException ex = createEntry(node, XmlException.Severity.ERROR, cause.getMessage(), cause);
        handle(ex, cause.getMessage());
        return ex;
    }


    @Override
    public XmlException error(@Nullable Node node, Throwable cause, String message, Object... args) {
        InternalUtil.assertParamNotNull("cause", cause);
        XmlException ex = createEntry(node, XmlException.Severity.ERROR, template(message, newArr(args, cause.getMessage())), cause);
        handle(ex, cause.getMessage());
        return ex;
    }


    @Override
    public XmlException fatal(Node node, String message, Object... args) {
        InternalUtil.assertParamNotNull("message", message);
        XmlException ex = createEntry(node, XmlException.Severity.FATAL, template(message, args), null);
        handle(ex, message);
        throw ex;
    }

    @Override
    public XmlException fatal(Node node, Throwable cause) {
        InternalUtil.assertParamNotNull("cause", cause);
        XmlException ex = createEntry(node, XmlException.Severity.FATAL, cause.getMessage(), cause);
        handle(ex, cause.getMessage());
        throw ex;
    }

    /**
     * Template the message with the given args. By default uses {@link MessageFormat#format(String, Object...)
     * MessageFormat::format}. Override to use a different method. If no arguments
     * are present, templating is applied regardless. This avoids differences
     * in special character escaping when arguments are added to a message
     * that had none.
     *
     * @param message Template
     * @param args    Template arguments
     *
     * @return The templated string
     */
    protected String template(String message, Object... args) {
        return MessageFormat.format(message, args);
    }


    private static Object[] newArr(Object[] fst, Object other, Object... rem) {
        Object[] res = new Object[fst.length + 1 + rem.length];
        System.arraycopy(fst, 0, res, 0, fst.length);
        res[fst.length] = other;
        if (rem.length > 0) {
            System.arraycopy(rem, 0, res, fst.length + 1, rem.length);
        }
        return res;
    }


    private XmlException createEntry(@Nullable Node node,
                                     XmlException.Severity level,
                                     String message,
                                     Throwable cause) {

        XmlPosition pos = positioner.startPositionOf(node);
        String fullMessage = positioner.makePositionedMessage(pos, printer.supportsAnsiColors(), USER_VALIDATION, level, message);

        return createException(pos, fullMessage, message, level, cause);
    }

    protected XmlException createException(XmlPosition position, String fullMessage, String simpleMessage, Severity severity, Throwable cause) {
        return new XmlException(position, fullMessage, simpleMessage, USER_VALIDATION, severity, cause);
    }

    @Override
    public void close() {

    }

}
