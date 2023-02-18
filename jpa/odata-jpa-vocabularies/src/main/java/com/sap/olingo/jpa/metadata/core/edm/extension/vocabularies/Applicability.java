/**
 *
 */
package com.sap.olingo.jpa.metadata.core.edm.extension.vocabularies;

/**
 * @author Oliver Grande
 * @since 1.1.1
 * 04.01.2023
 */
public enum Applicability {
  /**
   * Action
   */
  ACTION("Action"),
  /**
   * Action Import
   */
  ACTION_IMPORT("ActionImport"),
  /**
   * Annotation
   */
  ANNOTATION("Annotation"),
  /**
   * Application of a client-side function in an annotation
   */
  APPLY("Apply"),
  /**
   * Type Cast annotation expression
   */
  CAST("Cast"),
  /**
   * Entity Set or collection-valued Property or Navigation Property
   */
  COLLECTION("Collection"),
  /**
   * Complex Type
   */
  COMPLEX_TYPE("ComplexType"),
  /**
   * Entity Container
   */
  ENTITY_CONTAINER("EntityContainer"),
  /**
   * Entity Set
   */
  ENTITY_SET("EntitySet"),
  /**
   * Entity Type
   */
  ENTITY_TYPE("EntityType"),
  /**
   * Enumeration Type
   */
  ENUM_TYPE("EnumType"),
  /**
   * Function
   */
  FUNCTION("Function"),
  /**
   * Function Import
   */
  FUNCTION_IMPORT("FunctionImport"),
  /**
   * Conditional annotation expression
   */
  IF("If"),
  /**
   * Reference to an Included Schema
   */
  INCLUDE("Include"),
  /**
   * Type Check annotation expression
   */
  IS_OF("IsOf"),
  /**
   * Labeled Element expression
   */
  LABELD_ELEMENT("LabeledElement"),
  /**
   * Enumeration Member
   */
  MEMBER("Member"),
  /**
   * Navigation Property
   */
  NAVIGATION_PROPERTY("NavigationProperty"),
  /**
   * Null annotation expression
   */
  NULL("Null"),
  /**
   * On-Delete Action of a navigation property
   */
  ON_DELETE("OnDelete"),
  /**
   * Action of Function Parameter
   */
  PARAMETER("Parameter"),
  /**
   * Property of a structured type
   */
  PROPERTY("Property"),
  /**
   * Property value of a Record annotation expression
   */
  PROPERTY_VALUE("PropertyValue"),
  /**
   * Record annotation expression
   */
  RECORD("Record"),
  /**
   * Reference to another CSDL document
   */
  REFERENCE("Reference"),
  /**
   * Referential Constraint of a navigation property
   */
  REFERENTIAL_CONSTRAINT("ReferentialConstraint"),
  /**
   * Return Type of an Action or Function
   */
  RETURN_TYPE("ReturnType"),
  /**
   * Schema
   */
  SCHEMA("Schema"),
  /**
   * Singleton
   */
  SINGLETON("Singleton"),
  /**
   * Term
   */
  TERM("Term"),
  /**
   * Type Definition
   */
  TYPE_DEFINITION("TypeDefinition"),
  /**
   * UrlRef annotation expression
   */
  URL_REF("UrlRef");

  private String odataApplicability;

  /**
   * @param odataApplicability
   */
  Applicability(final String odataApplicability) {
    this.odataApplicability = odataApplicability;
  }

  public String getOdataApplicability() {
    return odataApplicability;
  }

}
