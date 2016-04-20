package org.apache.olingo.jpa.metadata.core.edm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apache.olingo.commons.api.edm.geo.Geospatial.Dimension;
import org.apache.olingo.commons.api.edm.geo.SRID;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
public @interface EdmGeospatial {
  /**
   * Olingo Geospatial dimension. Possible Values are GEOMETRY and GEOGRAPHY
   * @return
   */
  Dimension dimension();

  /**
   * Non negative integer value of the SRID. In case a negative value is provided the defaults as described in
   * {@link SRID} are taken.
   * 
   * @return
   */
  int srid() default -1;

}
