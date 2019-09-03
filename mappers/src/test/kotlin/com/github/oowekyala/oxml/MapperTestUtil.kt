package com.github.oowekyala.oxml

fun <T> Oxml.LocationedDoc.unmarshall(ser: XmlMapper<T>): T = OxmlMappers.getDefault().parse(this, ser)


inline fun <reified T> defaultSer(): XmlMapper<T> =
        TypedSerializerRegistrar.getInstance().getSerializer(T::class.java)
