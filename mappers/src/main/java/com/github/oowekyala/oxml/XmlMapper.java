package com.github.oowekyala.oxml;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import org.w3c.dom.Element;

/**
 * Composable XML serializer/deserializer for a value of type {@code <T>}.
 *
 * @param <T> Type of value handled
 */
public interface XmlMapper<T> {

    /**
     * Name of the element that this serializer should serialize to.
     * Serializer composers will create elements with that name.
     */
    String eltName(T value);


    Set<String> getPossibleNames();

    /**
     * Produce an XML element that represents the value [t]. The parameter
     * [eltFactory] can be used to produce a new element to add children.
     * The returned element must be understood by {@link #fromXml(Element, ErrorReporter)}.
     */
    void toXml(Element container, T t, Function<String, Element> eltFactory);


    /**
     * Parses the given XML element into a value of type {@code <T>}. This
     * method must be kept in sync with {@link #toXml(Element, Object, Function)}.
     */
    T fromXml(Element s, ErrorReporter err);


    /**
     * Simple serialization from and to a string, using the element's text content.
     *
     * @param <T> Type of values
     */
    static <T> XmlMapper<T> textOnly(String eltName, Function<String, T> fromString, Function<T, String> toString) {

        class MyDecorator implements XmlMapper<T> {

            @Override
            public String eltName(T value) {
                return eltName;
            }

            @Override
            public Set<String> getPossibleNames() {
                return Collections.singleton(eltName);
            }

            @Override
            public void toXml(Element container, T t, Function<String, Element> eltFactory) {
                container.setTextContent(toString.apply(t));
            }

            @Override
            public T fromXml(Element element, ErrorReporter err) {
                try {
                    return fromString.apply(element.getTextContent());
                } catch (Exception e) {
                    throw err.error(element, e);
                }
            }
        }

        return new MyDecorator();
    }

    /**
     * Returns a new serializer that can handle another type {@code <S>},
     * provided {@code <T>} can be mapped to and from {@code <S>}.
     */
    default <S> XmlMapper<S> map(Function<T, S> toS, Function<S, T> fromS) {
        return XmlComposition.map(this, toS, fromS);
    }


    /**
     * Builds a new serializer that can serialize arrays of component type
     * {@code <T>}.
     *
     * @see XmlComposition#toArray(String, XmlMapper, Object[])
     */
    default XmlMapper<T[]> toArray(T[] emptyArray) {
        return XmlComposition.toArray("array", this, emptyArray);
    }


    /**
     * Returns a new serializer, identical to the given [base] serializer,
     * except its serializer name is the given one.
     *
     * @see XmlComposition#rename(String, XmlMapper)
     */
    default XmlMapper<T> withName(String name) {
        return XmlComposition.rename(name, this);
    }


    /**
     * Builds a new serializer that can serialize arbitrary collections
     * with element type {@code <T>}. The list element name is "seq".
     *
     * @see XmlComposition#toSeq(String, XmlMapper, Supplier)
     */
    default <C extends Collection<T>> XmlMapper<C> toSeq(Supplier<C> emptyCollSupplier) {
        return XmlComposition.toSeq("seq", this, emptyCollSupplier);
    }


    /**
     * Builds a new serializer that can serialize lists of {@code <T>}.
     * The list element name is "list".
     *
     * @see XmlComposition#toSeq(String, XmlMapper, Supplier)
     */
    default XmlMapper<List<T>> toList() {
        return XmlComposition.toSeq("list", this, ArrayList::new);
    }


    interface MapAction<T, S> {

        S map(T value, Element s, ErrorReporter err);

    }


}
