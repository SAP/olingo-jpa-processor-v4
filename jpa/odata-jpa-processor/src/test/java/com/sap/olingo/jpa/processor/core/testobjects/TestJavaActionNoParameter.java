package com.sap.olingo.jpa.processor.core.testobjects;

import java.math.BigDecimal;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmAction;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmFunction.ReturnType;
import com.sap.olingo.jpa.processor.core.testmodel.AdministrativeDivision;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmParameter;

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

  @EdmAction(name = "", returnType = @ReturnType(isNullable = false, precision = 20, scale = 5))
  public BigDecimal unboundReturnPrimitivetNoParameter() {
    return new BigDecimal(7);
  }

  @EdmAction()
  public void unboundVoidOneParameter(@EdmParameter(name = "A") Short a) {
    param1 = a;
  }

  @EdmAction()
  public void unboundVoidTwoParameter(@EdmParameter(name = "A") Short a, @EdmParameter(name = "B") Integer b) {
    param1 = a;
    param2 = b;
  }

  @EdmAction(isBound = true)
  public void boundOnlyBinding(@EdmParameter(name = "Root") AdministrativeDivision root) {
    bindingParam = root;
  }

  @EdmAction(isBound = true)
  public void boundBindingPlus(@EdmParameter(name = "Root") AdministrativeDivision root, @EdmParameter(
      name = "A") Short a, @EdmParameter(name = "B") Integer b) {
    bindingParam = root;
    param1 = a;
    param2 = b;
  }

  @EdmAction()
  public void unboundVoidOneEnumerationParameter(@EdmParameter(name = "AccessRights") FileAccess a) {
    enumeration = a;
  }
}
