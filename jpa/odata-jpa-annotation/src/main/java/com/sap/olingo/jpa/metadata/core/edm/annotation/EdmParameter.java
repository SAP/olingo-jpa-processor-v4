package com.sap.olingo.jpa.metadata.core.edm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
public @interface EdmParameter {

  boolean isCollection() default false;

  // facets
  boolean isNullable() default true;

  int maxLength() default -1;

  String name();

  /**
   * Defines the name of the input parameter at a stored procedure or user defined function.
   * @return Parameter name
   */
  String parameterName() default "";

  int precision() default -1;

  int scale() default -1;

  EdmGeospatial srid() default @EdmGeospatial();

  /**
   * Define the parameter type in case of stored procedure or user defined function.<p>
   * 
   * @return Class of java parameter (row) type. This can be either a simple type like <code> Integer.class</code> or
   * the POJO defining an Entity.
   */
  Class<?> type() default Object.class;

}
