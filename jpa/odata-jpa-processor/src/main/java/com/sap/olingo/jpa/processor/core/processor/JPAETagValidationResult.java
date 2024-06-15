package com.sap.olingo.jpa.processor.core.processor;

import org.apache.olingo.commons.api.http.HttpStatusCode;

enum JPAETagValidationResult {
  NOT_MODIFIED(HttpStatusCode.NOT_MODIFIED),
  SUCCESS(HttpStatusCode.OK),
  PRECONDITION_FAILED(HttpStatusCode.PRECONDITION_FAILED);

  private final HttpStatusCode statusCode;

  JPAETagValidationResult(final HttpStatusCode statusCode) {
    this.statusCode = statusCode;
  }

  HttpStatusCode getStatusCode() {
    return statusCode;
  }
}
