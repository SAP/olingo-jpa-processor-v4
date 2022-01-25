package com.sap.olingo.jpa.metadata.core.edm.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.persistence.AttributeConverter;
import javax.persistence.Enumerated;

/**
 * Annotation to tag Java enumerations that shall be provided via an OData service. For details about OData Enumerations
 * see: <a
 * href="http://docs.oasis-open.org/odata/odata/v4.0/errata03/os/complete/part3-csdl/odata-v4.0-errata03-os-part3-csdl-complete.html#_Toc453752565">10
 * Enumeration Type</a>.
 * The following conversion rule have been established:
 * <ul>
 * <li>Name is taken from the enumeration name</li>
 * <li>UnderlyingType is derived from the converter, if no converter is provided Edm.Int32 is taken and a
 * field in an entity pojo needed to be annotated with <code>@Enumerated</code>.</li>
 * <li>Member</li>
 * <ul>
 * <li>Name is the name of a constant</li>
 * <li>Value is determined via the converter, if no converter is provided the value is determined via the method
 * ordinal</li>
 * </ul>
 * </ul>
 * @author Oliver Grande
 *
 */

@Retention(RUNTIME)
@Target(TYPE)
public @interface EdmEnumeration {

  /**
   * Converter to convert enumeration value into a number. If no converter is provided, the ordinal is taken.
   */
  Class<? extends AttributeConverter<? extends Enum<?>[], ? extends Number>> converter() default DummyConverter.class;

  boolean isFlags() default false;

  /**
   * Converter shall be optional, as java does not support <code>default null</code> a
   * dummy converter implementation is needed.
   */
  static class DummyConverter implements AttributeConverter<Enum<?>[], Integer> {

    @Override
    public Integer convertToDatabaseColumn(final Enum<?>[] attributes) {
      if (attributes == null || attributes.length == 0)
        return null;
      return attributes[0].ordinal();
    }

    @Enumerated
    @Override
    public Enum<?>[] convertToEntityAttribute(final Integer dbData) {
      return null; // NOSONAR
    }

  }
}
