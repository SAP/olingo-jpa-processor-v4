package com.sap.olingo.jpa.metadata.core.edm.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * The annotation can be used to assign attributes or properties to field or visibility groups. On case such a group is
 * provided during a GET request all properties that are assigned to that group and all properties that are assigned to
 * no group, or in other words that are not annotated, get selected. In case properties that belong to another group are
 * requested, a null value is returned.<p>
 * 
 * <b>Note:</b> Association or navigation properties can not be annotated.
 * 
 * @author Oliver Grande
 *
 */
@Retention(RUNTIME)
@Target(FIELD)
public @interface EdmVisibleFor {
  /**
   * List of field groups an attribute or property belongs to.
   * @return
   */
  String[] value();

}
