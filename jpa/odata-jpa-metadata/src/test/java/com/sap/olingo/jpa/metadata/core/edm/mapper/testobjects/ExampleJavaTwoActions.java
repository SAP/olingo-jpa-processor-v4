package com.sap.olingo.jpa.metadata.core.edm.mapper.testobjects;

import java.math.BigDecimal;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmAction;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmParameter;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extension.ODataAction;
import com.sap.olingo.jpa.processor.core.testmodel.Person;

public class ExampleJavaTwoActions implements ODataAction {

  @EdmAction(name = "Test", isBound = false)
  public void unbound(
      @EdmParameter(name = "Person") Person person,
      @EdmParameter(name = "A", precision = 34, scale = 10) BigDecimal a) {
    // Do nothing
  }

  @EdmAction(isBound = true)
  public void bound(
      @EdmParameter(name = "Person") Person person,
      @EdmParameter(name = "A", precision = 34, scale = 10) BigDecimal a) {
    // Do nothing
  }
}
