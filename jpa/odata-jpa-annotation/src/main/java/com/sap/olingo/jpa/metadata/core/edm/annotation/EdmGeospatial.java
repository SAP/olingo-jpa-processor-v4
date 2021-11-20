package com.sap.olingo.jpa.metadata.core.edm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apache.olingo.commons.api.edm.geo.Geospatial.Dimension;
import org.apache.olingo.commons.api.edm.geo.SRID;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.PARAMETER, ElementType.ANNOTATION_TYPE, ElementType.FIELD })
public @interface EdmGeospatial {
  /**
   * Olingo Geospatial dimension. Possible Values are GEOMETRY and GEOGRAPHY.
   * @return
   */
  Dimension dimension() default Dimension.GEOGRAPHY;

  /**
   * Non negative integer value of the Spatial Reference System Identifier (SRID). Value range is described in
   * {@link SRID} are taken.<br>
   * If no value is set than the EdmGeospatial is ignored within function parameter.
   * @return
   */
  String srid() default "";

}
