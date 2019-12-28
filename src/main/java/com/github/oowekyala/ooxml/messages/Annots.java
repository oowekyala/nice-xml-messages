package com.github.oowekyala.ooxml.messages;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

final class Annots {

    @Target(ElementType.TYPE_USE)
    @Retention(RetentionPolicy.SOURCE)
    @Documented
    @interface Nullable {

    }

    @Target(ElementType.TYPE_USE)
    @Retention(RetentionPolicy.SOURCE)
    @interface ZeroBased {
    }

    @Target(ElementType.TYPE_USE)
    @Retention(RetentionPolicy.SOURCE)
    @interface OneBased {
    }
}
