package com.sap.olingo.jpa.metadata.core.edm.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * The annotation can be used to assign on the one hand attributes or properties and n the other hand entities to user
 * or visibility groups.
 * <ul>
 * <li>In case an entity is annotated, the entity and navigation targeting the entity are only visible and accessible if
 * one of the given groups are available.</li>
 * <li>In case an attribute is annotated and such a group is provided during a GET request all properties that are
 * assigned to that group and all properties that are assigned to no group, or in other words that are not annotated,
 * get selected. For properties that belong to another group are requested, a null value is returned.
 * <p>
 *
 * <b>Note:</b> Keys, mandatory fields as well as association or navigation properties can not be annotated</li>
 * </ul>
 * @author Oliver Grande
 *
 */
@Retention(RUNTIME)
@Target({ FIELD })
public @interface EdmVisibleFor {
  /**
   * List of user groups an attribute or property or entity belongs to.
   * @return
   */
  String[] value() default {};

}
