package com.sap.olingo.jpa.processor.core.util;

import com.sap.olingo.jpa.metadata.api.JPAWrapperChecker;

public class TestWrapperChecker implements JPAWrapperChecker {

  @Override
  public boolean isWrapped() {
    // Fake to allow virtual properties
    return true;
  }

}
