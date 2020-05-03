package com.sap.olingo.jpa.processor.core.api.example;

import org.apache.olingo.commons.api.http.HttpStatusCode;

import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAMessageKey;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessException;

public class JPAExampleModifyException extends ODataJPAProcessException { // NOSONAR

  private static final long serialVersionUID = 121932494074522961L;
  private static final String BUNDEL_NAME = "example-exceptions-i18n";

  public enum MessageKeys implements ODataJPAMessageKey {
    ENTITY_NOT_FOUND,
    ENTITY_ALREADY_EXISTS,
    MODIFY_NOT_ALLOWED,
    WILDCARD_RANGE_NOT_SUPPORTED;

    @Override
    public String getKey() {
      return name();
    }

  }

  public JPAExampleModifyException(final MessageKeys messageKey, final HttpStatusCode statusCode) {
    super(messageKey.getKey(), statusCode);
  }

  public JPAExampleModifyException(final Exception e, final HttpStatusCode statusCode) {
    super(e, statusCode);
  }

  @Override
  protected String getBundleName() {
    return BUNDEL_NAME;
  }

}
