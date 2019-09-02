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


package com.github.oowekyala.rset.xml;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.github.oowekyala.rset.xml.ErrorReporter.Message;
import com.github.oowekyala.rset.xml.ErrorReporter.Message.Kind;

final class Util {

    public static String addNSpacesLeft(String s, int n) {
        StringBuilder builder = new StringBuilder();
        builder.append(' ');
        repeatChar(builder, ' ', n - 1);
        builder.append(s);
        return builder.toString();
    }

    // input builder must not be empty
    private static void repeatChar(StringBuilder builder, char c, int n) {
        while (n > 0) {
            if (n < builder.length()) {
                builder.append(c);
                n--;
            } else {
                int len = builder.length();
                builder.append(builder);
                n -= len;
            }
        }
    }

    static String enquote(String it) {return "'" + it + "'";}

    enum AnsiColor {
        COL_GREEN("\\e[32m"),
        COL_RED("\\e[31m"),
        COL_YELLOW("\\e[33;1m"),
        ;

        private static final String RESET = "\\e[0m";
        private final String s;

        AnsiColor(String s) {
            this.s = s;
        }

        String apply(String r) {
            return s + r + RESET;
        }
    }

    static class MessageTextBuilder {

        private static final String CARET = "^ ";
        private final int first;
        private final int errorIdx;
        private List<String> lines;

        MessageTextBuilder(List<String> lines, int first, int errorIdx) {
            this.lines = lines;
            this.first = first;
            this.errorIdx = errorIdx;
        }

        public String make(boolean useColor, Kind kind, Position position, Message message) {

            List<String> withLineNums = IntStream.range(0, lines.size())
                                                 .mapToObj(this::addLineNum)
                                                 .collect(Collectors.collectingAndThen(Collectors.toList(), ArrayList::new));

            String rline = addLineNum(errorIdx);
            int offset = rline.length() - lines.get(errorIdx).length();

            String messageLine =
                Util.addNSpacesLeft(CARET, position.getColumn() + offset -1) + message.toString();
            withLineNums.add(errorIdx, useColor ? kind.addColor(messageLine) : messageLine);
            withLineNums.add(errorIdx + 1, "\n"); // skip a line


            return String.join("\n", withLineNums);
        }

        private String addLineNum(int i) {
            return String.format("%5d| %s", 1 + i + first, lines.get(i));
        }
    }
}
