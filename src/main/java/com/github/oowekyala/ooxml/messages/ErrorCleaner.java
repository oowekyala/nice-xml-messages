/*
 * MIT License
 *
 * Copyright (c) 2022 Clément Fournier
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.github.oowekyala.ooxml.messages;

import java.util.regex.Pattern;


class ErrorCleaner {
    
    private static final String[] SCHEMA_VALIDATION_MESSAGE_PREFIXES =  {
        "cvc-attribute\\.",
        "cvc-complex-type\\.",
        "cvc-datatype-valid\\.",
        "cvc-elt\\.",
        "cvc-id\\.",
        "cvc-\\w++-valid",
        "cvc-type\\.",
        "schema_reference",
        "src-annotation",
        "src-attribute\\.",
        "src-attribute_group\\.",
        "src-ct\\.",
        "src-element\\.",
        "src-import\\.",
        "src-include\\.",
        "src-redefine\\.",
        "src-redefine\\.",
        "src-resolve",
        "src-simple-type\\.",
        "src-single-facet-value",
        "src-union-memberTypes-or-simpleTypes",
        "ag?-props-correct\\.",
        "au-props-correct\\.",
        "cos-all-limited\\.",
        "cos-applicable-facets",
        "cos-ct-extends\\.",
        "cos-element-consistent",
        "cos-list-of-atomic",
        "cos-nonambig",
        "cos-particle-restrict\\.",
        "cos-st-restricts\\.",
        "cos-valid-default\\.",
        "c-props-correct\\.",
        "ct-props-correct\\.",
        "derivation-ok-restriction\\.",
        "enumeration-required-notation",
        "enumeration-valid-restriction",
        "e-props-correct\\.",
        "fractionDigits-valid-restriction",
        "fractionDigits-totalDigits",
        "length-minLength-maxLength\\.",
        "length-valid-restriction",
        "maxExclusive-valid-restriction\\.",
        "maxInclusive-maxExclusive",
        "maxInclusive-valid-restriction\\.",
        "maxLength-valid-restriction",
        "mg-props-correct\\.",
        "minExclusive-less-than-equal-to-\\.axExclusive",
        "minExclusive-less-than-maxInclusive",
        "minExclusive-valid-restriction.",
        "minInclusive-less-than-equal-to-maxInclusive",
        "minInclusive-less-than-maxExclusive",
        "minInclusive-minExclusive",
        "minInclusive-valid-restriction.",
        "minLength-less-than-equal-to-maxLength",
        "minLength-valid-restriction",
        "no-xmlns",
        "no-xsi",
        "p-props-correct\\.",
        "rcase-MapAndSum\\.",
        "rcase-NameAndTypeOK\\.",
        "rcase-NSCompat\\.",
        "rcase-NSRecurseCheckCardinality\\.",
        "rcase-NSSubset\\.",
        "rcase-Recurse\\.",
        "rcase-RecurseLax\\.",
        "rcase-RecurseUnordered\\.",
        "sch-props-correct\\.",
        "st-props-correct\\.",
        "totalDigits-valid-restriction",
        "whiteSpace-valid-restriction\\.",
        "s4s-att-invalid-value",
        "s4s-att-must-appear",
        "s4s-att-not-allowed",
        "s4s-elt-invalid",
        "s4s-elt-must-match\\.",
        "s4s-elt-must-match",
        "s4s-elt-invalid-content\\.",
        "s4s-elt-schema-ns",
        "s4s-elt-character",
        "c-fields-xpaths",
        "c-general-xpath",
        "c-general-xpath-ns",
        "c-selector-xpath",
        "EmptyTargetNamespace",
        "FacetValueFromBase",
        "FixedFacetValue",
        "InvalidRegex",
        "MaxOccurLimit",
        "PublicSystemOnNotation",
        "SchemaLocation",
        "TargetNamespace\\.",
        "TargetNamespace\\.",
        "UndeclaredEntity",
        "UndeclaredPrefix",
    };

    private static final Pattern SCHEMA_MESSAGE_PATTERN;

    static {
        SCHEMA_MESSAGE_PATTERN = Pattern.compile("^(" + String.join("|", SCHEMA_VALIDATION_MESSAGE_PREFIXES) + ")" + ".*");
    }


    static boolean isSchemaValidationMessage(String message) {
        return SCHEMA_MESSAGE_PATTERN.matcher(message).matches();
    }
    
}
