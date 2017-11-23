# 1.12 Enumeration Types

Since version 3 OData allows the specification of enumeration types. Together with Java enumerations it allows on the one hand to provide a client more details about the allowed values of a property and on the other hand to increase type safety in the code.

OData distinguish between two kinds of enumerations, the flags and the non flags. We want have a look at both and see how Java enumerations are used to create OData enumerations.

## Simple Enumerations

As an example we assume that the Companies shall be classified using an ABC-Classification. For this a enumeration with the corresponding value shall be introduced:

```Java
package tutorial.model;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmEnumeration;

@EdmEnumeration()
public enum ABCClassifiaction {
  A, B, C;
}
```
This enumeration is tagged with a `@EdmEnumeration` so it will be converted into an OData Enumeration, if it is found. As described in [Tutorial 1.8 Functions](1-8-Functions.md) it is neccesary to provide the package name to look for the enumerations. Therefore it is required to change our Servlet as follows:
```Java
...
handler.getJPAODataContext().setTypePackage("tutorial.operations", "tutorial.model");
...
```
With that we can already have a look at the metadata document, http://localhost:8080/Tutorial/Tutorial.svc/$metadata, with the following mapping:
![Mapping of ABCClassifiaction](Metadata/MappingSimpleEnum.png)

As you can see all not given values are filled with default value, which are _false_ for _isFlag_, and _Edm.Int32_, so _Integer_, as _UnderlingType_. The numbering of the members determined via the _ordinal()_ method of the enumeration.

Next we want to extend the company class with a property for the _ABC Class_. So we need to add a corresponding attribute to the Company. Even so OData handles enumeration always as numeric values, the _ABC Class_ shall be stored as a String, which is signaled via JPA annotation` @Enumerated(value = EnumType.STRING)`:

```Java
package tutorial.model;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

@Entity(name = "Company")
@DiscriminatorValue(value = "2")
public class Company extends BusinessPartner {

...

@Enumerated(value = EnumType.STRING)
@Column(name = "\"ABCClass\"")
private ABCClassifiaction abcClass;
```
If we have a look at the metadata again, we will find the following:
![Mapping of ABCClassifiaction](Metadata/MappingSimpleEnumCompany.png)
## Flags-Enabled Enumeration

Here we take over the example given in [Schema Definition Language Documentation](http://docs.oasis-open.org/odata/odata/v4.0/errata03/os/complete/part3-csdl/odata-v4.0-errata03-os-part3-csdl-complete.html#_Toc453752565). Our use-case is that we want to be able to get insights into the access rights of a Person. The rights should be  _Read_, _Write_, _Create_ or _Delete_. A person may have multiple right at the same time. Do achieve this we assign a short values to each constant:

```Java
package tutorial.model;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmEnumeration;

@EdmEnumeration(isFlags = true, converter = AccessRightsConverter.class)
public enum AccessRights {
  Read((short) 1), Write((short) 2), Create((short) 4), Delete((short) 8);

  private short value;

  private AccessRights(short value) {
    this.setValue(value);
  }

  public short getValue() {
    return value;
  }

  private void setValue(short value) {
    this.value = value;
  }
}
```
The EdmEnumeration annotation has been enriched, so the JPA processor knows that this enumeration is a flag and that the value shall not be determined via `ordinal()` but via a converter, which we can later use, when we add a column to the Person. A simple converter can look as follows:
```Java
package tutorial.model;

import java.util.Arrays;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter(autoApply = false)
public class AccessRightsConverter implements AttributeConverter<AccessRights, Short> {

  @Override
  public Short convertToDatabaseColumn(AccessRights attribute) {
    return attribute != null ? attribute.getValue() : null;
  }

  @Override
  public AccessRights convertToEntityAttribute(Short dbData) {
    if (dbData != null) {
      for (AccessRights e : Arrays.asList(AccessRights.values())) {
        if (e.getValue() == dbData)
          return e;
      }
    }
    return null;
  }

}
```
Again we can have a look at the metadata document:
![Mapping of AccessRights](Metadata/MappingFlagsEnum.png)

Now, as the last step, let's create the access rights column:

```Java
package tutorial.model;

import java.sql.Date;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity(name = "Person")
@DiscriminatorValue(value = "1")
public class Person extends BusinessPartner {
...
@Convert(converter = AccessRightsConverter.class)
@Column(name = "\"AccessRights\"")
private AccessRights accessRights;
...
```
