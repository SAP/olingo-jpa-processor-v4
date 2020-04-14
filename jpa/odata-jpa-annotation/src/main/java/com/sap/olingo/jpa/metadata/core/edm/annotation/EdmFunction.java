package com.sap.olingo.jpa.metadata.core.edm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Metadata of a function, see <a href =
 * "http://docs.oasis-open.org/odata/odata/v4.0/errata02/os/complete/part3-csdl/odata-v4.0-errata02-os-part3-csdl-complete.html#_Toc406398010">
 * edm:Function.</a><p>
 * @author Oliver Grande
 *
 */
@Repeatable(EdmFunctions.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD })
public @interface EdmFunction {
  /**
   * Define if the return type for the function. It can be a collection (entity set) or
   * an single entity (entity).
   * 
   */
  @interface ReturnType {

    /**
     * @return <code>true</code> if a collection is returned,
     * otherwise <code>false</code> if a single entity is returned.
     */
    boolean isCollection() default false;

    // facets
    boolean isNullable() default true;

    int maxLength() default -1;

    int precision() default -1;

    int scale() default -1;

    EdmGeospatial srid() default @EdmGeospatial();

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
   * Defines the name of the function in the service document. This is a required attribute for database based functions
   * and can be omitted for java based functions.
   * @return
   */
  String name() default "";

  EdmParameter[] parameter() default {};

  /**
   * Defines the name of a User Defined Function on the database
   * @return
   */
  String functionName() default "";

  /**
   * Indicates that the Function is bound. <p>
   * If isBound is false a function is treated as <i>unbound</i>, so it can be accessed either via a Function Import or
   * be used in <i>filter</i> or <i>orderby</i> expression. Otherwise the function is treated as bound.
   * For details see:
   * <a href =
   * "http://docs.oasis-open.org/odata/odata/v4.0/errata02/os/complete/part3-csdl/odata-v4.0-errata02-os-part3-csdl-complete.html#_Toc406398013"
   * />OData Version 4.0 Part 3 - 12.2.2 Attribute IsBound</a> <p>
   * <b>If the function is java based isBound is ignored and always set to false</b>
   * @return
   */
  boolean isBound() default true;

  /**
   * Indicates that a Function Import shall be generated into the Container. For details see:
   * <a href =
   * "http://docs.oasis-open.org/odata/odata/v4.0/errata02/os/complete/part3-csdl/odata-v4.0-errata02-os-part3-csdl-complete.html#_Toc406398042"
   * />OData Version 4.0 Part 3 - 13.6 Element edm:FunctionImport</a> <p>
   * hasFunctionImport is handled as follows:<p>
   * <ol>
   * <li>For <b>bound</b> functions hasFunctionImport is always treated as <b>false</b></li>
   * <li>For <b>unbound</b> functions in case hasFunctionImport is <b>true</b> a function import is generated, which
   * allows to be call a function from the container
   * </ol>
   * <b>If the function is java based hasFunctionImport is ignored and always set to true</b>
   * @return
   */
  boolean hasFunctionImport() default false;

  /**
   * Define the return type of this function
   * 
   * @return return type of this function
   */
  ReturnType returnType();
}
