package org.apache.olingo.jpa.metadata.core.edm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
public @interface EdmFunctionParameter {

  boolean isCollection() default false;

  // facets
  boolean isNullable() default true;

  int maxLength() default -1;

  String name();

  /**
   * Defines the name of of the input parameter at a stored procedure or user defined Function.
   * It is required in case the function shall be executed as a stored procedure
   * @return Parameter name
   */
  String parameterName() default "";

  int precision() default -1;

  int scale() default -1;
  // TODO SRID
  // SRID srid()

  /**
   * Define the parameter type.<p>
   * 
   * @return Class of java parameter (row) type. This can be either a simple type like <code> Integer.class</code> or
   * the POJO defining an Entity.
   */
  Class<?> type() default Object.class;

}
