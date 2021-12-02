package com.sap.olingo.jpa.metadata.core.edm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks an association to an entity that contains a language/locale dependent description of a coded value. E.g the
 * Association to a country name for an attribute containing the country iso code.<p>
 * The association shall be annotated as OneToOne or OneToMany (as the key is not completely given), insertable = false
 * and updatable = false. It is mapped to a property with the name of the association<p>
 * One and only one of the fields of LanguageAttribute - LocaleAttribute has to be filled. To ensure that, in case
 * multiple descriptions are available, the right one the chosen the Description Attribute has to be named<p>
 * @author Oliver Grande
 *
 */
@Target({ ElementType.FIELD })
@Retention(value = RetentionPolicy.RUNTIME)
@Inherited
public @interface EdmDescriptionAssociation {
  String descriptionAttribute();

  String languageAttribute() default "";

  String localeAttribute() default "";

  @interface valueAssignment {
    String attribute() default "";

    String value() default "";
  }

  valueAssignment[] valueAssignments() default {};

}
