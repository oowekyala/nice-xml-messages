/*
 * Copyright 2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package com.github.oowekyala.ooxml.internal_not_api;

import static java.lang.Integer.max;

import com.github.oowekyala.ooxml.messages.Annots.OneBased;


public final class InternalUtil {

    static final Object[] EMPTY_OBJ_ARRAY = new Object[0];
    private static final char CARET = '^';
    private static final char SPACE = ' ';


    private InternalUtil() {

    }


    public static void assertParamNotNull(String paramName, Object value) {
        if (value == null) {
            throw new NullPointerException(paramName + " should not be null");
        }
    }


    public static String buildCaretLine(String message, @OneBased int column, int rangeLen) {
        StringBuilder builder = new StringBuilder();
        repeatChar(builder, SPACE, column);
        repeatChar(builder, CARET, max(rangeLen, 1));
        return builder.append(SPACE).append(message).toString();
    }

    private static void repeatChar(StringBuilder builder, char c, int n) {
        final int start = builder.length();
        while (n > 0) {
            int lenAdded = builder.length() - start;
            if (n >= lenAdded && lenAdded > 0) {
                builder.append(builder, start, builder.length());
                n -= lenAdded;
            } else {
                builder.append(c);
                n--;
            }
        }
    }


}
