package com.sap.olingo.jpa.metadata.core.edm.mapper.testaction;

import java.math.BigDecimal;

import jakarta.persistence.EntityManager;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmAction;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmParameter;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extension.ODataAction;
import com.sap.olingo.jpa.processor.core.testmodel.Person;

public class Actions implements ODataAction {
  public Actions(final EntityManager em) {
    super();
  }

  @EdmAction(name = "BoundNoImport", isBound = true)
  public void boundNoImport(
      @EdmParameter(name = "Person") final Person person,
      @EdmParameter(name = "A", precision = 34, scale = 10) final BigDecimal a) {
    // Do nothing
  }

  @EdmAction(name = "WithImport", isBound = false)
  public void withImport(
      @EdmParameter(name = "Person") final Person person,
      @EdmParameter(name = "A", precision = 34, scale = 10) final BigDecimal a) {
    // Do nothing
  }
}
