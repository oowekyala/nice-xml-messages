package com.github.oowekyala.oxml

fun <T> Oxml.LocationedDoc.unmarshall(ser: XmlMapper<T>): T = OxmlMappers.getDefault().parse(this, ser)
