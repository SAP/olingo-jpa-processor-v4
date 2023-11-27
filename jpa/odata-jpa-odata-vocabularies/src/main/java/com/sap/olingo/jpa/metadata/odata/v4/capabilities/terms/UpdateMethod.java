/**
 *
 */
package com.sap.olingo.jpa.metadata.odata.v4.capabilities.terms;

import org.apache.olingo.commons.api.http.HttpMethod;

/**
 * @author Oliver Grande
 * Created: 29.04.2021
 *
 */
public enum UpdateMethod {
  PATCH(HttpMethod.PATCH),
  PUT(HttpMethod.PUT),
  NOT_SPECIFIED(null);

  private final HttpMethod value;

  /**
   * @param verb
   */
  UpdateMethod(final HttpMethod verb) {
    this.value = verb;
  }

  public HttpMethod getValue() {
    return value;
  }
}
