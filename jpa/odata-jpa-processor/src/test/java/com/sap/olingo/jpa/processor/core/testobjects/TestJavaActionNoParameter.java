package com.sap.olingo.jpa.processor.core.testobjects;

import java.math.BigDecimal;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmAction;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmFunction.ReturnType;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmParameter;
import com.sap.olingo.jpa.processor.core.testmodel.AdministrativeDivision;
import com.sap.olingo.jpa.processor.core.testmodel.BusinessPartner;

public class TestJavaActionNoParameter {
  public static int constructorCalls = 0;
  public static Short param1 = null;
  public static Integer param2 = null;
  public static FileAccess enumeration = null;
  public static AdministrativeDivision bindingParam = null;

  public TestJavaActionNoParameter() {
    super();
    constructorCalls++;
  }

  public static void resetCalls() {
    constructorCalls = 0;
  }

  @EdmAction(returnType = @ReturnType(isNullable = false, precision = 20, scale = 5))
  public BigDecimal unboundReturnPrimitiveNoParameter() {
    return new BigDecimal(7);
  }

  @EdmAction()
  public void unboundVoidOneParameter(@EdmParameter(name = "A") final Short a) {
    param1 = a;
  }

  @EdmAction()
  public void unboundVoidTwoParameter(@EdmParameter(name = "A") final Short a, @EdmParameter(
      name = "B") final Integer b) {
    param1 = a;
    param2 = b;
  }

  @EdmAction(isBound = true)
  public void boundOnlyBinding(@EdmParameter(name = "Root") final AdministrativeDivision root) {
    bindingParam = root;
  }

  @EdmAction(isBound = true)
  public void boundBindingPlus(@EdmParameter(name = "Root") final AdministrativeDivision root, @EdmParameter(
      name = "A") final Short a, @EdmParameter(name = "B") final Integer b) {
    bindingParam = root;
    param1 = a;
    param2 = b;
  }

  @EdmAction()
  public void unboundVoidOneEnumerationParameter(@EdmParameter(name = "AccessRights") final FileAccess a) {
    enumeration = a;
  }

  @EdmAction(isBound = true)
  public void boundBindingSuperType(@EdmParameter(name = "Root") final BusinessPartner root) {
    // Not needed
  }
}
