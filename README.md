# Nice XML messages

A mini library to output rich error messages when doing some
XML validation with the standard Java DOM API.


For example, if an XML parse exception occurs, instead of
```
javax.xml.transform.TransformerException: The entity "amb" was referenced, but not declared.
```
the exception would have the following message:
```
XML parsing error (in /some/file.xml)
    1| <?xml version="1.0" encoding="UTF-8" standalone="no"?>
    2| <list>
    3|     <list foo="&amb;"/>
                           ^ The entity "amb" was referenced, but not declared.
    4| </list>
```
or
```
Error at /some/file.xml:3:21 - The entity "amb" was referenced, but not declared.
```
if you prefer shorter messages.


The point is that it displays the file position.


This library would be most useful when doing some manual 
validation/parsing on the DOM afterwards, eg when reading a
configuration file.

It provides an interface which looks like this:

```java
public interface XmlErrorReporter {

  void warn(org.w3c.dom.Node location, String message, Object... formatArgs);

  // and other variants like error, fatalError, etc

}
```

After parsing your file appropriately, you can do your parsing
and report messages to the user easily:

```java
  void parseListElement(Element elt, XmlErrorReporter err) {
    for (Element item : DomUtils.elementsIn(elt)) {
        if (!item.getNodeName().equals("item")) {
            err.warn(item, "Unexpected element {0} will be ignored", item.getNodeName());
            continue;
        }

        // process item
    }
  }
```

The warning would include the position of the element.
