package com.sap.olingo.jpa.metadata.core.edm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmFunction.ReturnType;

/**
 * Metadata of an action, see <a href =
 * "http://docs.oasis-open.org/odata/odata/v4.0/errata03/os/complete/part3-csdl/odata-v4.0-errata03-os-part3-csdl-complete.html#_Toc453752579">
 * edm:Action</a>.<p>
 * @author Oliver Grande
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
public @interface EdmAction {

  /**
   * Defines the name of the action. If not given, the Java method name is taken.
   * @return
   */
  String name() default "";

  /**
   * Define additional facet information for the return type of an action
   *
   * @return return type of this action
   */
  ReturnType returnType() default @ReturnType();

  /**
   * Indicates that the action is bound. Default is <b>false</b>.<p>
   * Unbound actions are invoked through an action import. Bound actions are invoked by appending a segment containing
   * the qualified action name to a segment of the appropriate binding parameter type within the resource path.
   * For details see:
   * <a href =
   * "http://docs.oasis-open.org/odata/odata/v4.0/errata03/os/complete/part3-csdl/odata-v4.0-errata03-os-part3-csdl-complete.html#_Toc453752581"
   * />OData Version 4.0 Part 3 - 12.1.2 Attribute IsBound</a> <p>
   * @return
   */
  boolean isBound() default false;

  /**
   * Bound actions that return an entity or a collection of entities MAY specify a value for the EntitySetPath
   * attribute if determination of the entity set for the return type is contingent on the binding parameter.
   * <p>
   * See:
   * <a href =
   * "http://docs.oasis-open.org/odata/odata/v4.0/errata03/os/complete/part3-csdl/odata-v4.0-errata03-os-part3-csdl-complete.html#_Toc453752582"
   * />OData Version 4.0 Part 3 - 12.1.3 Attribute EntitySetPath</a> <p>
   * @return
   */
  String entitySetPath() default "";
}
