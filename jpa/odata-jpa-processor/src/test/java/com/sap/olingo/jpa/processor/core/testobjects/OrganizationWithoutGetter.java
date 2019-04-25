package com.sap.olingo.jpa.processor.core.testobjects;

/**
 * Needed for testing.<br>
 * Should not have a getter for the id
 * @author Oliver Grande
 *
 */
public class OrganizationWithoutGetter {
  @SuppressWarnings("unused")
  private String id;

  public OrganizationWithoutGetter(String id) {
    super();
    this.id = id;
  }
}
