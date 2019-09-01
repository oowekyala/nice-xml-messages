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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.xml.namespace.QName;

final class Util {

    private static final Pattern XMLNS_RE = Pattern.compile("xmlns\\s*=\\s*[\"'](.*?)[\"']");
    private static final Pattern ELEMENT_RE = Pattern.compile("<([^?].*?)[\\s/>]");
    private static final Pattern TARGET_NS_RE = Pattern.compile("targetNamespace\\s*=\\s*[\"'](.*?)[\"']");

    private Util() {
        // util class
    }

    public static Set<String> extractXMLNS(final List<String> lines) {
        final Set<String> xmlnses = new TreeSet<>();
        lines.forEach(l -> {
            final Matcher m = XMLNS_RE.matcher(l);
            if (m.find()) {
                xmlnses.add(m.group(1));
            }
        });

        return xmlnses;
    }

    public static QName extractFirstElement(final List<String> lines) {
        QName name = null;
        int idx = 0;
        while (name == null &&
            idx < lines.size()) {
            final String line = lines.get(idx);
            final Matcher elm = ELEMENT_RE.matcher(line);
            if (elm.find()) {
                final String el = elm.group(1);
                final Matcher xm = XMLNS_RE.matcher(line);
                if (xm.find()) {
                    name = new QName(xm.group(1), el);
                } else {
                    name = QName.valueOf(el);
                }
            }
            idx++;
        }

        return name;
    }

    public static boolean providesXMLNS(final Set<String> xmlnses, URL url) throws IOException {
        try (final BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()))) {
            String line = reader.readLine();
            while (line != null) {
                final Matcher m = TARGET_NS_RE.matcher(line);
                if (m.find() &&
                    xmlnses.contains(m.group(1))) {

                    return true;
                }
                line = reader.readLine();
            }


        }
        return false;
    }

    public static String alternateSpelling(final String current, final Collection<String> alternates) {
        return alternateSpelling(current, alternates, dynamicThreshold(current));
    }

    // we want a smaller threshold for short words, and want a max threshold, no matter the length
    private static int dynamicThreshold(final String s) {
        final int len = s.length();

        if (len < 6) {
            return 2;
        }
        if (len < 10) {
            return 3;
        }
        if (len < 14) {
            return 4;
        }
        return 5;
    }

    public static String alternateSpelling(final String current, final Collection<String> alternates, final int threshold) {
        final String alternate = alternates.stream()
                                           .map(s -> {
                                               int dist = Levenshtein.getLevenshteinDistance(current, s, threshold);
                                               if (dist > 0) {
                                                   return String.format("%s:%s", dist, s);
                                               } else {
                                                   return null;
                                               }
                                           })
                                           .filter(x -> x != null)
                                           .sorted()
                                           .findFirst()
                                           .orElse(null);

        if (alternate != null) {
            final String[] parts = alternate.split(":");

            return parts[1];
        }

        return null;
    }


    public static String withPrefixAfterNth(final int skip, final String prefix, final String v) {
        final List<String> lines = Arrays.asList(v.split("\\n"));
        final List<String> outLines = new ArrayList<>();
        outLines.addAll(lines.subList(0, skip));
        outLines.addAll(lines.subList(skip, lines.size()).stream()
                             .map(x -> String.format("%s%s", prefix, x))
                             .collect(Collectors.toList()));

        return preserveFinalNewline(v, String.join("\n", outLines));
    }

    public static String withPrefix(final String prefix, final String v) {
        return withPrefixAfterNth(0, prefix, v);
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> asSortedList(Collection<? extends T> col) {
        if (col == null) {
            return Collections.EMPTY_LIST;
        }

        return col.stream()
                  .sorted()
                  .collect(Collectors.toList());
    }

    public static String asCommaString(Collection<?> col) {
        return String.join(", ", col.stream()
                                    .map(Object::toString)
                                    .collect(Collectors.toList()));
    }

    public static String pathToString(List<String> path) {
        return String.join(" > ", path);
    }

    public static Function<String, String> possiblyUnderscoredName(final Set<String> possibles) {
        return s -> {
            if (possibles.contains(s)) {
                return s;
            }
            final String sans_ = s.replace("_", "-");
            if (possibles.contains(sans_)) {
                return sans_;
            }

            return s;
        };
    }

    private static String wrapLine(final int width, final String line) {
        if (line.length() <= width) {

            return line;
        }

        for (int idx = width; idx > 0; idx--) {
            if (line.charAt(idx) == ' ') {

                return line.substring(0, idx) + "\n" + wrapLine(width, line.substring(idx + 1));
            }
        }

        // no spaces found in line - don't break
        return line;
    }

    public static String wrapString(final int width, final String str) {
        return preserveFinalNewline(str,
                                    String.join("\n",
                                                Arrays.stream(str.split("\\n"))
                                                      .map(l -> wrapLine(width, l))
                                                      .collect(Collectors.toList())));
    }

    public static String wrapAndIndentEachLine(final int width, final int indent, final String str) {
        return preserveFinalNewline(str,
                                    String.join("\n",
                                                Arrays.stream(str.split("\\n"))
                                                      .map(l -> wrapLine(width, l))
                                                      .map(l -> indentLinesAfterFirst(indent, l))
                                                      .collect(Collectors.toList())));
    }

    public static String indentLinesAfterFirst(final int indent, final String str) {
        return indentLinesAfterNth(1, indent, str);
    }

    public static String indentLinesAfterNth(final int skip, final int indent, final String str) {
        final StringBuilder prefix = new StringBuilder();
        for (int i = 0; i < indent; i++) {
            prefix.append(' ');
        }
        final StringBuilder out = new StringBuilder();
        final String[] lines = str.split("\\n");
        int count = 0;
        for (String line : lines) {
            if (count >= skip) {
                out.append(prefix);
            }
            out.append(line);
            if (count < lines.length - 1) {
                out.append('\n');
            }
            count++;
        }

        return preserveFinalNewline(str, out.toString());
    }

    private static String preserveFinalNewline(final String orig, final String out) {
        return out + (orig.endsWith("\n") ? "\n" : "");
    }

    public static String asColumns(final List<String> values) {
        final int widest = values.stream()
                                 .mapToInt(String::length)
                                 .max()
                                 .orElse(0); // won't happen
        final int columns = widest > 20 ? 2 : 3;
        final int rows = values.size() / columns + (values.size() % columns > 0 ? 1 : 0);

        final String format = "%-" + widest + "s  ";

        final StringBuilder out = new StringBuilder();
        for (int i = 0; i < rows; i++) {
            for (int col = 0; col < columns; col++) {
                final int offset = (col * rows) + i;
                if (offset < values.size()) {
                    out.append(String.format(format, values.get(offset)));
                }
            }
            out.append('\n');
        }

        return out.toString();
    }

    public static String documentName(final URL path) {
        final String[] docPathParts = path.getPath().split("/");

        return docPathParts[docPathParts.length - 1];
    }

    public static String stripPeriod(final String in) {
        if (in.endsWith(".")) {

            return in.substring(0, in.length() - 1);
        }

        return in;
    }
}
