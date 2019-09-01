package com.github.oowekyala.rset.xml;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author Clément Fournier
 */
public final class SerComposition {


    /**
     * Builds a new serializer that can serialize arbitrary collections
     * with element type {@code <T>}.
     *
     * If the [seqName] is "seq", and the item serializer's item name is "item",
     * then this will produce output like this:
     *
     * <pre>{@code
     * <seq>
     *     <item>...</item>
     *     <item>...</item>
     * </seq>
     * }</pre>
     *
     * @param <C>               Collection type to serialize
     * @param seqName           Name of the sequence element
     * @param itemSer           Serializer for the items
     * @param emptyCollSupplier Supplier for a collection of the correct
     *                          type, to which the deserialized elements
     *                          are added.
     *
     * @return A new serializer
     */
    public static <T, C extends Collection<T>> XmlSerializer<C> toSeq(final String seqName, final XmlSerializer<T> itemSer, Supplier<C> emptyCollSupplier) {

        class MyDecorator implements XmlSerializer<C> {

            @Override
            public String eltName() {
                return seqName;
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


                getElementChildren(element).forEach(child -> {
                    if (!itemSer.eltName().equals(child.getLocalName())) {
                        err.warn(child, "Expecting a " + itemSer.eltName() + " element, ignoring node");
                    } else {
                        result.add(itemSer.fromXml(child, err));
                    }
                });
                return result;
            }
        }

        return new MyDecorator();
    }

    /**
     * Builds a new serializer that can serialize maps with an arbitrary key type.
     *
     * If the elt is "map", and the key and value serializers' item name are "key"
     * and "value", then this will produce output like this:
     *
     * <pre>{@code
     * <map>
     *     <entry>
     *         <key>...</key>
     *         <value>...</value>
     *     </entry>
     * </map>
     * }</pre>
     *
     * @param <M>              Map type to serialize
     * @param <V>              Type of values
     * @param eltName          Name of the produced element
     * @param emptyMapSupplier Supplier for a collection of the correct
     *                         type, to which the deserialized elements
     *                         are added.
     *
     * @return A new serializer
     */
    public static <V, M extends Map<String, V>> XmlSerializer<M> forStringMap(final String eltName,
                                                                              Supplier<M> emptyMapSupplier,
                                                                              XmlSerializer<V> valueSerializer) {

        class MyDecorator implements XmlSerializer<M> {

            @Override
            public String eltName() {
                return eltName;
            }

            @Override
            public void toXml(Element container, M c, Function<String, Element> eltFactory) {
                c.forEach((k, v) -> {
                    // TODO escape
                    Element key = eltFactory.apply(k);
                    Element val = eltFactory.apply(valueSerializer.eltName());

                    valueSerializer.toXml(val, v, eltFactory);
                    key.appendChild(val);
                    container.appendChild(key);
                });
            }

            @Override
            public M fromXml(Element element, ErrorReporter err) {
                M result = emptyMapSupplier.get();

                NodeList children = element.getChildNodes();
                for (int i = 0; i < children.getLength(); i++) {
                    Node item = children.item(i);
                    if (item.getNodeType() == Element.ELEMENT_NODE) {
                        Element entry = (Element) item;

                        V v = expectElement(err, entry, 0, valueSerializer);

                        if (v != null) {
                            result.put(entry.getLocalName(), v);
                        }
                    }
                }

                return result;
            }
        }

        return new MyDecorator();
    }

    /**
     * Builds a new serializer that can serialize maps with an arbitrary key type.
     *
     * If the elt is "map", and the key and value serializers' item name are "key"
     * and "value", then this will produce output like this:
     *
     * <pre>{@code
     * <map>
     *     <entry>
     *         <key>...</key>
     *         <value>...</value>
     *     </entry>
     * </map>
     * }</pre>
     *
     * @param <M>              Map type to serialize
     * @param <K>              Type of keys
     * @param <V>              Type of values
     * @param eltName          Name of the produced element
     * @param emptyMapSupplier Supplier for a collection of the correct
     *                         type, to which the deserialized elements
     *                         are added.
     *
     * @return A new serializer
     */
    public static <K, V, M extends Map<K, V>> XmlSerializer<M> forMap(final String eltName, Supplier<M> emptyMapSupplier, final XmlSerializer<K> keySerializer, XmlSerializer<V> valueSerializer) {

        class MyDecorator implements XmlSerializer<M> {

            @Override
            public String eltName() {
                return eltName;
            }

            @Override
            public void toXml(Element container, M c, Function<String, Element> eltFactory) {
                c.forEach((k, v) -> {
                    Element entry = eltFactory.apply("entry");
                    Element key = eltFactory.apply(keySerializer.eltName());
                    Element val = eltFactory.apply(valueSerializer.eltName());

                    keySerializer.toXml(key, k, eltFactory);
                    valueSerializer.toXml(val, v, eltFactory);
                    entry.appendChild(key);
                    entry.appendChild(val);
                    container.appendChild(entry);
                });
            }

            // TODO null key

            @Override
            public M fromXml(Element element, ErrorReporter err) {
                M result = emptyMapSupplier.get();

                NodeList children = element.getChildNodes();
                for (int i = 0; i < children.getLength(); i++) {
                    Node item = children.item(i);
                    if (item.getNodeType() == Element.ELEMENT_NODE) {
                        Element entry = (Element) item;

                        K key = expectElement(err, entry, 0, keySerializer);
                        V value = expectElement(err, entry, 1, valueSerializer);

                        if (key != null && value != null) {
                            result.put(key, value);
                        }
                    }
                }

                return result;
            }

        }

        return new MyDecorator();
    }

    /**
     * Returns a new serializer that can handle another type {@code <S>},
     * provided {@code <T>} can be mapped to and from {@code <S>}.
     */
    public static <T, S> XmlSerializer<S> map(final XmlSerializer<T> base, Function<T, S> toS, Function<S, T> fromS) {

        return new XmlSerializer<S>() {

            @Override
            public String eltName() {
                return base.eltName();
            }

            @Override
            public void toXml(Element container, S s, Function<String, Element> eltFactory) {
                base.toXml(container, fromS.apply(s), eltFactory);
            }

            @Override
            public S fromXml(Element s, ErrorReporter err) {
                return toS.apply(base.fromXml(s, err));
            }
        };
    }

    /**
     * Builds a new serializer that can serialize arrays of component type
     * {@code <T>}.
     *
     * @param itemSer    Serializer for the items
     * @param emptyArray Empty array supplier
     *
     * @return A new serializer
     */
    public static <T> XmlSerializer<T[]> toArray(String eltName, XmlSerializer<T> itemSer, T[] emptyArray) {
        return SerComposition.<T, List<T>>toSeq(eltName, itemSer, ArrayList::new).map(l -> l.toArray(emptyArray), Arrays::asList);
    }

    private static Element getChild(Element parent, int idx) {
        return getElementChildren(parent).skip(idx).findFirst().orElse(null);

    }

    private static Stream<Element> getElementChildren(Element parent) {
        return toList(parent.getChildNodes()).stream()
                                             .filter(it -> it.getNodeType() == Node.ELEMENT_NODE)
                                             .map(Element.class::cast);
    }

    private static List<Node> toList(NodeList lst) {
        ArrayList<Node> nodes = new ArrayList<>();
        for (int i = 0; i < lst.getLength(); i++) {
            nodes.add(lst.item(i));
        }
        return nodes;
    }

    private static <T> T expectElement(ErrorReporter err, Element parent, int idx, XmlSerializer<T> s2) {
        Element value = getChild(parent, idx);
        if (value == null) {
            err.warn(parent, "Expecting child #" + idx + " to be a " + s2.eltName());
        } else if (!s2.eltName().equals(value.getLocalName())) {
            err.warn(value, "Expecting a " + s2.eltName() + " element");
        } else {
            return s2.fromXml(value, err);
        }

        return null;
    }
}
