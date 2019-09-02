package com.github.oowekyala.oxml;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author Clément Fournier
 */
public final class XmlComposition {


    /**
     * Builds a new serializer from a bunch of other serializers. The
     * serializer is chosen by the [selector] predicate, the deserializer
     * is chosen by name.
     *
     * @return A new serializer
     */
    public static <T> XmlMapper<T> nameSelectedUnion(Set<XmlMapper<T>> syntaxes, BiPredicate<T, XmlMapper<T>> selector) {

        class MyDecorator implements XmlMapper<T> {

            private final Map<String, XmlMapper<T>> deser = new HashMap<>();


            {
                for (XmlMapper<T> ser : syntaxes) {
                    for (String it : ser.getPossibleNames()) {
                        if (deser.put(it, ser) != null) {
                            throw new IllegalStateException("Duplicate serializer name " + it);
                        }
                    }
                }

                if (deser.isEmpty()) {
                    throw new IllegalStateException("No selectable name!");
                }
            }


            @Override
            public String eltName(T value) {
                return selectSer(value).eltName(value);
            }

            private XmlMapper<T> selectSer(T value) {
                return syntaxes.stream().filter(it -> selector.test(value, it))
                               .findFirst()
                               .orElseThrow(() -> new IllegalStateException(
                                   "No serializer in " + syntaxes + " for value " + value));
            }

            @Override
            public Set<String> getPossibleNames() {
                return deser.keySet();
            }

            @Override
            public void toXml(Element container, T t, Function<String, Element> eltFactory) {
                selectSer(t).toXml(container, t, eltFactory);
            }

            @Override
            public T fromXml(Element s, ErrorReporter err) {
                XmlMapper<T> ser = deser.get(s.getLocalName());
                if (ser == null) {
                    throw err.error(s, "Unexpected element with name '" + s.getLocalName() + "', expecting "
                        + formatPossibilities(this));
                }
                return ser.fromXml(s, err);
            }
        }

        return new MyDecorator();
    }

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
    public static <T, C extends Collection<T>> XmlMapper<C> toSeq(final String seqName, final XmlMapper<T> itemSer, Supplier<C> emptyCollSupplier) {

        class MyDecorator implements XmlMapper<C> {

            @Override
            public String eltName(C value) {
                return seqName;
            }

            @Override
            public Set<String> getPossibleNames() {
                return Collections.singleton(seqName);
            }

            @Override
            public void toXml(Element container, C c, Function<String, Element> eltFactory) {
                for (T v : c) {
                    Element item = eltFactory.apply(itemSer.eltName(v));
                    itemSer.toXml(item, v, eltFactory);
                    container.appendChild(item);
                }
            }

            @Override
            public C fromXml(Element element, ErrorReporter err) {
                C result = emptyCollSupplier.get();

                getElementChildren(element).forEach(child -> {
                    T item = expectElement(err, child, itemSer);
                    if (item != null) {
                        result.add(item);
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
    public static <V, M extends Map<String, V>> XmlMapper<M> forStringMap(final String eltName,
                                                                          Supplier<M> emptyMapSupplier,
                                                                          XmlMapper<V> valueSerializer) {

        class MyDecorator implements XmlMapper<M> {

            @Override
            public String eltName(M value) {
                return eltName;
            }


            @Override
            public Set<String> getPossibleNames() {
                return Collections.singleton(eltName);
            }


            @Override
            public void toXml(Element container, M c, Function<String, Element> eltFactory) {
                c.forEach((k, v) -> {
                    // TODO escape
                    Element key = eltFactory.apply(k);
                    Element val = eltFactory.apply(valueSerializer.eltName(v));

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
    public static <K, V, M extends Map<K, V>> XmlMapper<M> forMap(final String eltName, Supplier<M> emptyMapSupplier, final XmlMapper<K> keySerializer, XmlMapper<V> valueSerializer) {

        class MyDecorator implements XmlMapper<M> {

            @Override
            public String eltName(M value) {
                return eltName;
            }

            @Override
            public Set<String> getPossibleNames() {
                return Collections.singleton(eltName);
            }

            @Override
            public void toXml(Element container, M c, Function<String, Element> eltFactory) {
                c.forEach((k, v) -> {
                    Element entry = eltFactory.apply("entry");
                    Element key = eltFactory.apply(keySerializer.eltName(k));
                    Element val = eltFactory.apply(valueSerializer.eltName(v));

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
    public static <T, S> XmlMapper<S> map(final XmlMapper<T> base, Function<T, S> toS, Function<S, T> fromS) {

        return new XmlMapper<S>() {

            @Override
            public String eltName(S value) {
                return base.eltName(fromS.apply(value));
            }

            @Override
            public Set<String> getPossibleNames() {
                return base.getPossibleNames();
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
    public static <T> XmlMapper<T[]> toArray(String eltName, XmlMapper<T> itemSer, T[] emptyArray) {
        return XmlComposition.<T, List<T>>toSeq(eltName, itemSer, ArrayList::new).map(l -> l.toArray(emptyArray), Arrays::asList);
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

    /**
     * Returns a new serializer, identical to the given [base] serializer,
     * except its serializer name is the given one.
     */
    public static <T> XmlMapper<T> rename(final String name, final XmlMapper<T> base) {

        return new XmlMapper<T>() {

            @Override
            public String eltName(T value) {
                return name;
            }

            @Override
            public Set<String> getPossibleNames() {
                return Collections.singleton(name);
            }

            @Override
            public void toXml(Element container, T t, Function<String, Element> eltFactory) {
                base.toXml(container, t, eltFactory);
            }

            @Override
            public T fromXml(Element s, ErrorReporter err) {
                return base.fromXml(s, err);
            }
        };
    }

    private static <T> T expectElement(ErrorReporter err, Element parent, int idx, XmlMapper<T> s2) {
        Element value = getChild(parent, idx);

        String possibilities = formatPossibilities(s2);

        if (possibilities == null && value != null) {
            return s2.fromXml(value, err);
        } else if (possibilities == null) {
            err.warn(parent, "Expecting a child element at index #" + idx);
        } else if (value == null) {
            err.warn(parent, "Expecting child #" + idx + " to be " + possibilities);
        } else if (!s2.getPossibleNames().contains(value.getTagName())) {
            err.warn(value, "Wrong name, expecting " + possibilities);
        } else {
            return s2.fromXml(value, err);
        }

        return null;
    }

    private static <T> T expectElement(ErrorReporter err, Element elt, XmlMapper<T> s2) {

        String possibilities = formatPossibilities(s2);

        if (!s2.getPossibleNames().contains(elt.getTagName())) {
            err.warn(elt, "Wrong name, expecting " + possibilities);
        } else {
            return s2.fromXml(elt, err);
        }

        return null;
    }

    // nullable
    private static String formatPossibilities(XmlMapper<?> ser) {
        Set<String> strings = ser.getPossibleNames();
        if (strings.isEmpty()) {
            return null;
        } else if (strings.size() == 1) {
            return Util.enquote(strings.iterator().next());
        } else {
            return "one of " + strings.stream().map(Util::enquote).collect(Collectors.joining(", "));
        }
    }

}
