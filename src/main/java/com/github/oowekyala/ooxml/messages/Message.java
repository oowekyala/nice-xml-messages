package com.github.oowekyala.ooxml.messages;

/**
 * @author Clément Fournier
 */
abstract class Message {

    private final MessageKind kind;

    protected Message(MessageKind kind) {
        this.kind = kind;
    }

    MessageKind getKind() {
        return kind;
    }

    public abstract String toString();

    public static class Templated extends Message {

        private final String template;
        private final Object[] args;

        public Templated(MessageKind kind, String template, Object... args) {
            super(kind);
            this.template = template;
            this.args = args;
        }

        @Override
        public String toString() {
            return String.format(template, args);
        }
    }

    static class Wrapper extends Message {

        private final Message base;
        private final String eval;

        public Wrapper(Message base, String eval) {
            super(base.getKind());
            this.base = base;
            this.eval = eval;
        }

        @Override
        public String toString() {
            return eval;
        }
    }
}
