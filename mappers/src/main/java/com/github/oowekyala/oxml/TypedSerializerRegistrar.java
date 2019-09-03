
package com.github.oowekyala.oxml;

import static com.github.oowekyala.oxml.XmlMapper.textOnly;

import java.io.File;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.WeakHashMap;
import java.util.function.Function;
import java.util.function.Supplier;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;

/**
 * A collection of serializers. Once you register a serializer for a type T,
 * list/set types of any depth with that type T as element type can be serialized
 * without having to register anything else. You can override their serialization
 * routine explicitly if you want though.
 *
 * <p>Arrays are not supported out of the box. They'd require some boilerplate
 * I don't want to write without a use case.
 *
 * <p>Serializers for common types are registered implicitly.
 *
 * <p>Instead of creating serializer for new types from scratch, you can
 * instead map existing serializers to your new types. Eg if some value of
 * type {@code Foo} has a string id, then you can do:
 * <pre>
 *     {@code
 *      registrar.registerMapped(Foo.class, String.class, Foo::fromId, Foo::getId);
 *     }
 * </pre>
 *
 * @author Clément Fournier
 */
public class TypedSerializerRegistrar {


    private static final TypedSerializerRegistrar INSTANCE = new TypedSerializerRegistrar();

    // using a map of types obviously doesn't handle subtyping
    private final Map<Type, XmlMapper<?>> converters = new WeakHashMap<>();

    public TypedSerializerRegistrar() {
        registerStandard();
    }

    /**
     * Registers a new serializer for type [toRegister], which is based
     * on an already registered serializer for [existing]. The new serializer
     * is obtained using {@link XmlMapper#map(Function, Function)}.
     */
    public final <T, U> void registerMapped(Class<T> toRegister, Class<U> existing,
                                            Function<T, U> toBase, Function<U, T> fromBase) {
        if (converters.get(existing) == null) {
            throw new IllegalStateException("No existing converter for " + existing);
        }
        register(getSerializer(existing).map(fromBase, toBase), toRegister);
    }

    private void registerStandard() {

        register(textOnly("str", Function.identity(), Function.identity()), String.class);

        register(textOnly("int", Integer::valueOf, i -> Integer.toString(i)), Integer.class, Integer.TYPE);
        register(textOnly("double", Double::valueOf, d -> Double.toString(d)), Double.class, Double.TYPE);
        register(textOnly("bool", Boolean::valueOf, b -> Boolean.toString(b)), Boolean.class, Boolean.TYPE);
        register(textOnly("long", Long::valueOf, b -> Long.toString(b)), Long.class, Long.TYPE);
        register(textOnly("float", Float::valueOf, b -> Float.toString(b)), Float.class, Float.TYPE);
        register(textOnly("short", Short::valueOf, b -> Short.toString(b)), Short.class, Short.TYPE);
        register(textOnly("byte", Byte::valueOf, b -> Byte.toString(b)), Byte.class, Byte.TYPE);
        register(textOnly("char", s -> s.charAt(0), b -> Character.toString(b)), Character.class, Character.TYPE);

        registerMapped(Date.class, Long.TYPE, Date::getTime, Date::new);
        registerMapped(java.sql.Time.class, Long.TYPE, java.sql.Time::getTime, java.sql.Time::new);
        registerMapped(java.sql.Date.class, Long.TYPE, java.sql.Date::getTime, java.sql.Date::new);

        registerMapped(File.class, String.class, File::getPath, File::new);
        registerMapped(Path.class, String.class, Path::toString, Paths::get);
        registerMapped(Class.class, String.class, Class::getName, className -> {
            try {
                return Class.forName(className);
            } catch (ClassNotFoundException e) {
                return null;
            }
        });

    }

    /**
     * Registers a serializer suitable for one or more types. It can then
     * be accessed on this registrar instance with {@link #getSerializer(Typed)}.
     */
    @SafeVarargs
    public final <T> void register(XmlMapper<T> serializer, Typed<T> firstType, Typed<T>... type) {
        converters.put(firstType.getType(), serializer);
        for (Typed<T> tClass : type) {
            converters.put(tClass.getType(), serializer);
        }
    }

    @SafeVarargs
    public final <T> void register(XmlMapper<T> serializer, Class<T> firstType, Class<T>... type) {
        converters.put(firstType, serializer);
        for (Class<T> it : type) {
            converters.put(it, serializer);
        }
    }

    /**
     * Returns a new serializer that uses all the serializers registered
     * on this registrar instance to serialize any object. It throws IllegalStateExceptions
     * if serializers are missing for a particular type.
     */
    public XmlMapper<TypedObject<?>> compositeSerializer() {
        class Composite implements XmlMapper<TypedObject<?>> {

            @Override
            public String eltName(TypedObject<?> value) {
                return "object";
            }

            @Override
            public Set<String> getPossibleNames() {
                return Collections.singleton("object");
            }

            @Override
            public void toXml(Element container, TypedObject<?> typedObject, Function<String, Element> eltFactory) {
                @SuppressWarnings("unchecked")
                XmlMapper<Object> serializer = (XmlMapper<Object>) getSerializer(typedObject.getType());
                if (serializer == null) {
                    throw new IllegalStateException("No serializer registered for type " + typedObject.getType());
                }

                try {
                    serializer.toXml(container, typedObject.getObject(), eltFactory);
                    container.setAttribute("type", typedObject.getType().getTypeName());
                } catch (Exception e) {
                    String message = "Unable to serialize " + typedObject;
                    new IllegalStateException(message, e).printStackTrace();
                }
            }

            @Override
            public TypedObject<?> fromXml(Element s, ErrorReporter err) {
                Attr attr = s.getAttributeNode("type");
                if (attr == null) {
                    throw err.error(s, "Expected a 'type' attribute");
                }
                Type type;
                try {
                    type = parseType(attr.getValue());
                } catch (Exception e) {
                    throw err.error(attr, e);
                }

                XmlMapper<?> serializer = getSerializer(type);
                if (serializer == null) {
                    throw new IllegalStateException("No serializer registered for type " + type);
                }

                // TODO it could be that a different serializer serialized the object...

                return new TypedObject<>(serializer.fromXml(s, err), type);
            }

        }

        return new Composite();
    }

    /**
     * Gets a registered serializer for an a type. If the class is an
     * array type and some component type has a registered serializer,
     * a new array serializer will be derived and returned transparently.
     *
     * <p>To get serializers for collection types, use rather {@link #getSerializer(Typed)}.
     *
     * @return A serializer, or null if none can be derived
     */
    public final <T> XmlMapper<T> getSerializer(Class<T> type) {
        @SuppressWarnings("unchecked")
        XmlMapper<T> t = (XmlMapper<T>) converters.get(type);
        return t;
    }

    /**
     * Typesafe version of {@link #getSerializer(Type)}.
     *
     * @param typed Type witness
     */
    public final <T> XmlMapper<T> getSerializer(Typed<T> typed) {
        @SuppressWarnings("unchecked")
        XmlMapper<T> serializer = (XmlMapper<T>) getSerializer(typed.getType());
        return serializer;
    }


    /**
     * Get a serializer for some generic type. If the type is generic and
     * has at most one type argument, a best effort strategy will try to
     * find a suitable serializer.
     *
     * @param genericType The type for which to get a serializer.
     *
     * @return A serializer, or null if none can be found
     */
    @SuppressWarnings("unchecked")
    public final XmlMapper<?> getSerializer(Type genericType) {
        if (converters.containsKey(genericType)) {
            return converters.get(genericType);
        }

        if (genericType instanceof Class) {
            return getSerializer((Class) genericType);
        } else if (genericType instanceof WildcardType) {
            return getSerializer(((WildcardType) genericType).getUpperBounds()[0]);
        } else if (genericType instanceof TypeVariable<?>) {
            return getSerializer(((TypeVariable) genericType).getBounds()[0]);
        } else if (genericType instanceof ParameterizedType) {
            Class rawType = (Class) ((ParameterizedType) genericType).getRawType();

            Type[] actualTypeArguments = ((ParameterizedType) genericType).getActualTypeArguments();

            if (actualTypeArguments.length == 1) {
                XmlMapper<?> targSerializer = get1targSerializer(rawType, actualTypeArguments[0]);
                if (targSerializer != null) {
                    return targSerializer;
                }
            } else if (actualTypeArguments.length == 2) {
                XmlMapper<?> targSerializer = get2targSerializer(rawType, actualTypeArguments[0], actualTypeArguments[1]);
                if (targSerializer != null) {
                    return targSerializer;
                }
            }

            return getSerializer(rawType);
        }

        return null;
    }

    // FIXME the deserializer doesn't handle maps bc of 2 type args

    private XmlMapper<?> get2targSerializer(Class rawType, Type targ1, Type targ2) {
        XmlMapper kSerializer = getSerializer(targ1);
        XmlMapper vSerializer = getSerializer(targ2);
        if (kSerializer != null && vSerializer != null) {
            if (SortedMap.class.isAssignableFrom(rawType)) {
                return XmlComposition.forMap("map", TreeMap::new, kSerializer, vSerializer);
            } else if (Map.class.isAssignableFrom(rawType)) {
                return XmlComposition.forMap("map", HashMap::new, kSerializer, vSerializer);
            }
        }

        return null;
    }

    private XmlMapper<?> get1targSerializer(Class rawType, Type targ1) {

        Supplier<Collection<Object>> emptyCollSupplier = null;
        if (rawType != null && Collection.class.isAssignableFrom(rawType)) {
            if (List.class.isAssignableFrom(rawType)) {
                emptyCollSupplier = ArrayList::new;
            } else if (Set.class.isAssignableFrom(rawType)) {
                emptyCollSupplier = HashSet::new;
            }
        }

        if (emptyCollSupplier != null) {
            XmlMapper componentSerializer = getSerializer(targ1);
            if (componentSerializer != null) {
                return componentSerializer.<Collection<Object>>toSeq(emptyCollSupplier);
            }
        }

        return null;
    }


    public static TypedSerializerRegistrar getInstance() {
        return INSTANCE;
    }

    /**
     * Parses a string into a type. Returns null if it doesn't succeed.
     * FIXME Only supports parameterized types with at most one type argument.
     * Doesn't support wildcard types.
     *
     * TODO make a real parser someday
     */
    private static Type parseType(String t) {
        throw new UnsupportedOperationException("TODO");
    }
}
