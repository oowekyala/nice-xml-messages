package com.github.oowekyala.rset.xml;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Composable XML serializer for a value of type {@code <T>}.
 *
 * @param <T> Type of value handled
 */
public interface XmlSerializer<T> {

    String eltName();


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

        return new XmlSerializer<S>() {

            @Override
            public String eltName() {
                return XmlSerializer.this.eltName();
            }

            @Override
            public void toXml(Element container, S s, Function<String, Element> eltFactory) {
                XmlSerializer.this.toXml(container, fromS.apply(s), eltFactory);
            }

            @Override
            public S fromXml(Element s, ErrorReporter err) {
                return toS.apply(XmlSerializer.this.fromXml(s, err));
            }
        };
    }


    /**
     * Builds a new serializer that can serialize arrays of component type
     * {@code <T>}.
     *
     * @param emptyArray Empty array supplier
     *
     * @return A new serializer
     */
    default XmlSerializer<T[]> toArray(T[] emptyArray) {
        return
            this.<List<T>>toSeq(ArrayList::new)
                .map(l -> l.toArray(emptyArray), Arrays::asList);
    }


    /**
     * Builds a new serializer that can serialize arbitrary collections
     * with element type {@code <T>}.
     *
     * @param emptyCollSupplier Supplier for a collection of the correct
     *                          type, to which the deserialized elements
     *                          are added.
     * @param <C>               Collection type to serialize
     *
     * @return A new serializer
     */
    default <C extends Collection<T>> XmlSerializer<C> toSeq(Supplier<C> emptyCollSupplier) {

        class MyDecorator implements XmlSerializer<C> {

            private final XmlSerializer<T> itemSer = XmlSerializer.this;

            @Override
            public String eltName() {
                return "seq";
            }

            @Override
            public void toXml(Element container, C c, Function<String, Element> eltFactory) {
                for (T v : c) {
                    Element item = eltFactory.apply(itemSer.eltName());
                    itemSer.toXml(item, v, eltFactory);
                    container.appendChild(item);
                }
            }

            @Override
            public C fromXml(Element element, ErrorReporter err) {
                C result = emptyCollSupplier.get();

                NodeList children = element.getChildNodes();
                for (int i = 0; i < children.getLength(); i++) {
                    Node item = children.item(i);
                    if (item.getNodeType() == Element.ELEMENT_NODE) {
                        Element child = (Element) item;
                        if (!itemSer.eltName().equals(child.getLocalName())) {
                            err.warn(child, "Expecting a " + itemSer.eltName() + " element");
                            continue;
                        }
                        result.add(itemSer.fromXml(child, err));
                    }
                }

                return result;
            }
        }

        return new MyDecorator();
    }


    /**
     * Builds a new serializer that can serialize maps with key type {@code <T>}.
     *
     * @param <M>              Map type to serialize
     * @param emptyMapSupplier Supplier for a collection of the correct
     *                         type, to which the deserialized elements
     *                         are added.
     *
     * @return A new serializer
     */
    static <T, V, M extends Map<T, V>> XmlSerializer<M> forMap(Supplier<M> emptyMapSupplier, final XmlSerializer<T> keySerializer, XmlSerializer<V> valueSerializer) {

        class MyDecorator implements XmlSerializer<M> {

            @Override
            public String eltName() {
                return "map";
            }

            @Override
            public void toXml(Element container, M c, Function<String, Element> eltFactory) {
                c.forEach((t, v) -> {
                    Element entry = eltFactory.apply("entry");
                    Element key = eltFactory.apply(keySerializer.eltName());
                    Element val = eltFactory.apply(valueSerializer.eltName());

                    keySerializer.toXml(key, t, eltFactory);
                    valueSerializer.toXml(val, v, eltFactory);
                    entry.appendChild(key);
                    entry.appendChild(val);
                    container.appendChild(entry);
                });
            }

            private Element getChild(Element parent, int idx) {
                NodeList children = parent.getChildNodes();
                for (int i = 0; i < children.getLength(); i++) {
                    Node item = children.item(i);
                    if (item.getNodeType() == Node.ELEMENT_NODE) {
                        if (idx == 0) {
                            return (Element) item;
                        }
                        idx--;
                    }
                }
                return null;
            }

            @Override
            public M fromXml(Element element, ErrorReporter err) {
                M result = emptyMapSupplier.get();

                NodeList children = element.getChildNodes();
                for (int i = 0; i < children.getLength(); i++) {
                    Node item = children.item(i);
                    if (item.getNodeType() == Element.ELEMENT_NODE) {
                        Element entry = (Element) item;
                        Element key = getChild(entry, 0);
                        Element value = getChild(entry, 1);


                        if (key == null) {
                            err.warn(entry, "Expecting first child to be a " + keySerializer.eltName());
                        } else if (!keySerializer.eltName().equals(key.getLocalName())) {
                            err.warn(key, "Expecting a " + keySerializer.eltName() + " element");
                        }

                        if (value == null) {
                            err.warn(entry, "Expecting second child to be a " + keySerializer.eltName());
                        } else if (!valueSerializer.eltName().equals(value.getLocalName())) {
                            err.warn(key, "Expecting a " + valueSerializer.eltName() + " element");
                        }

                        if (key != null && value != null) {
                            result.put(keySerializer.fromXml(key, err),
                                       valueSerializer.fromXml(value, err));
                        }
                    }
                }

                return result;
            }
        }

        return new MyDecorator();
    }


    /**
     * Simple serialization from and to a string, using the element's text content.
     */
    static <T> XmlSerializer<T> textValue(String eltName, Function<String, T> fromString, Function<T, String> toString) {

        class MyDecorator implements XmlSerializer<T> {

            @Override
            public String eltName() {
                return eltName;
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
