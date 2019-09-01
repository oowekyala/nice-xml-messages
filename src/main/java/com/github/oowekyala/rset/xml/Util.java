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

final class Util {

    public static String padRight(String s, int n) {
        return String.format("%-" + n + "s", s);
    }

    public static String padLeft(String s, int n) {
        return String.format("%" + n + "s", s);
    }

    static String enquote(String it) {return "'" + it + "'";}

    static class MessageTextBuilder {

        private static final String CARET = "^^^ ";
        private final int first;
        private final int last;
        private final int errorIdx;
        private List<String> lines;

        MessageTextBuilder(List<String> lines, int first, int last, int errorIdx) {
            this.lines = lines;
            this.first = first;
            this.last = last;
            this.errorIdx = errorIdx;
        }

        public String make(Position position, Message message) {

            List<String> withLineNums = IntStream.range(0, lines.size())
                                                 .mapToObj(this::addLineNum)
                                                 .collect(Collectors.collectingAndThen(Collectors.toList(), ArrayList::new));

            String rline = addLineNum(errorIdx);
            int offset = rline.length() - lines.get(errorIdx).length();

            String messageLine =
                Util.padLeft(CARET, position.getColumn() + offset + CARET.length()) + message.toString();
            withLineNums.add(errorIdx, messageLine);
            withLineNums.add(errorIdx + 1, "\n"); // skip a line


            return String.join("\n", withLineNums);
        }

        private String addLineNum(int i) {
            return (i + first) + " :" + lines.get(i);
        }

        private static class IntRef {

            int i;
        }
    }
}
