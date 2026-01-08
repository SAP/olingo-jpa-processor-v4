package com.sap.olingo.jpa.metadata.core.edm.mapper.testaction;

import jakarta.persistence.EntityManager;

import com.sap.olingo.jpa.metadata.api.JPAHttpHeaderMap;
import com.sap.olingo.jpa.metadata.api.JPARequestParameterMap;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmAction;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmParameter;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extension.ODataAction;
import com.sap.olingo.jpa.processor.core.testmodel.AdministrativeDivision;
import com.sap.olingo.jpa.processor.core.testmodel.BusinessPartner;
import com.sap.olingo.jpa.processor.core.testmodel.Person;

public class ActionWithOverload implements ODataAction {

  public ActionWithOverload(final EntityManager em, final JPAHttpHeaderMap header,
      final JPARequestParameterMap parameter) {
    super();
  }

  @EdmAction(name = "DoSometingFunny", isBound = true)
  public void baseAction(@EdmParameter(name = "Binding") final BusinessPartner partner) {
    // Not needed
  }

  @EdmAction(name = "DoSometingFunny", isBound = true)
  public void overloadedAction(@EdmParameter(name = "Binding") final Person partner) {
    // Not needed
  }

  @EdmAction(name = "DoSometingFunny", isBound = true)
  public void overloadedAction(@EdmParameter(name = "Binding") final AdministrativeDivision partner) {
    // Not needed
  }
}
