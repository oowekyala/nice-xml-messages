package com.github.oowekyala.ooxml.messages;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Clément Fournier
 */
@Target(ElementType.TYPE_USE)
@Retention(RetentionPolicy.SOURCE)
@Documented
@interface ZeroBased {
}
