package com.github.oowekyala.rset.xml;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import org.w3c.dom.Element;

/**
 * Composable XML serializer for a value of type {@code <T>}.
 *
 * @param <T> Type of value handled
 */
public interface XmlSerializer<T> {

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
     * Returns a new serializer that can handle another type {@code <S>},
     * provided {@code <T>} can be mapped to and from {@code <S>}.
     */
    default <S> XmlSerializer<S> map(Function<T, S> toS, Function<S, T> fromS) {
        return SerComposition.map(this, toS, fromS);
    }


    /**
     * Builds a new serializer that can serialize arrays of component type
     * {@code <T>}.
     *
     * @see SerComposition#toArray(String, XmlSerializer, Object[])
     */
    default XmlSerializer<T[]> toArray(T[] emptyArray) {
        return SerComposition.toArray("array", this, emptyArray);
    }


    /**
     * Returns a new serializer, identical to the given [base] serializer,
     * except its serializer name is the given one.
     *
     * @see SerComposition#rename(String, XmlSerializer)
     */
    default XmlSerializer<T> withName(String name) {
        return SerComposition.rename(name, this);
    }


    /**
     * Builds a new serializer that can serialize arbitrary collections
     * with element type {@code <T>}. The list element name is "seq".
     *
     * @see SerComposition#toSeq(String, XmlSerializer, Supplier)
     */
    default <C extends Collection<T>> XmlSerializer<C> toSeq(Supplier<C> emptyCollSupplier) {
        return SerComposition.toSeq("seq", this, emptyCollSupplier);
    }


    /**
     * Simple serialization from and to a string, using the element's text content.
     *
     * @param <T> Type of values
     */
    static <T> XmlSerializer<T> textValue(String eltName, Function<String, T> fromString, Function<T, String> toString) {

        class MyDecorator implements XmlSerializer<T> {

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


}
