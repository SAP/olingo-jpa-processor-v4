package org.apache.olingo.jpa.metadata.core.edm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Metadata of a function. <p>
 * By default bound functions are treated as User Defined Functions, whereas unbound functions are teared as Stored
 * Procedures.
 * a User Defined Function or
 * @author Oliver Grande
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface EdmFunction {

  @interface ReturnType {

    /**
     * Define if the return type for the function. It can be a collection (entity set) or
     * an single entity (entity).
     * 
     * @return <code>true</code> if a collection is returned,
     * otherwise <code>false</code> if a single entity is returned.
     */
    boolean isCollection() default false;

    // facets
    boolean isNullable() default true;

    int maxLength() default -1;

    int precision() default -1;

    int scale() default -1;
    // TODO SRID (4- bis 5-stelliger Schlüsselnummern)
    // SRID srid();

    /**
     * Define the return type for the function import.<p>
     * 
     * @return Class of java parameter (row) type. This can be either a simple type like <code> Integer.class</code> or
     * the POJO defining an Entity. If the type is not set and the
     * function is defined at an JPA Entity POJO, the corresponding Entity Type is used. In addition, in case of an
     * unbound function, no out-bound parameter is set.
     */
    Class<?> type() default Object.class;
  }

  /**
   * Defines the name of the function in the service document
   * @return
   */
  String name();

  EdmFunctionParameter[] parameter() default {};

  /**
   * Defines the name of a Stored Procedure respectively User Defined FUnction on the database
   * @return
   */
  String functionName() default "";

  boolean isBound() default false;

  /**
   * Define the return type of this function import
   * 
   * @return return type of this function import
   */
  ReturnType returnType();
}
