package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlAction;
import org.apache.olingo.commons.api.edm.provider.CsdlParameter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;

class ODataActionKeyTest {
  private static final String TYPE_NAME = "Person";
  private static final String NAMESPACE = "Punit";
  private static final String ACTION_NAME = "Test";
  private IntermediateOperation operation;
  private CsdlAction csdl;
  private ODataActionKey cut;

  @BeforeEach
  void setup() throws ODataJPAModelException {
    csdl = mock(CsdlAction.class);
    operation = createBoundOperation(csdl, ACTION_NAME);
    cut = new ODataActionKey(operation);
  }

  private IntermediateOperation createBoundOperation(final CsdlAction csdlAction, final String actionName)
      throws ODataJPAModelException {
    final IntermediateOperation operation = mock(IntermediateOperation.class);
    final List<CsdlParameter> parameters = new ArrayList<>();
    final CsdlParameter parameter = mock(CsdlParameter.class);
    parameters.add(parameter);
    when(operation.getExternalName()).thenReturn(actionName);
    when(operation.getEdmItem()).thenReturn(csdlAction);
    when(operation.isBound()).thenReturn(true);
    when(csdlAction.getParameters()).thenReturn(parameters);
    when(parameter.getTypeFQN()).thenReturn(new FullQualifiedName(NAMESPACE, TYPE_NAME));
    return operation;
  }

  @Test
  void checkGetExternalName() {
    assertEquals(ACTION_NAME, cut.getExternalName());
  }

  @Test
  void checkGetBindingParameterType() {
    final FullQualifiedName act = cut.getBindingParameterType();
    assertEquals(NAMESPACE, act.getNamespace());
    assertEquals(TYPE_NAME, act.getName());
  }

  @Test
  void checkHasCode() {
    assertNotEquals(0, cut.hashCode());
  }

  @Test
  void checkEquals() throws ODataJPAModelException {
    final IntermediateOperation other = createBoundOperation(mock(CsdlAction.class), "Other");
    final IntermediateOperation other2 = createBoundOperation(mock(CsdlAction.class), "Other");

    when(((CsdlAction) other2.getEdmItem()).getParameters().get(0).getTypeFQN())
        .thenReturn(new FullQualifiedName("Dummy", TYPE_NAME));

    assertEquals(cut, cut);
    assertNotEquals(cut, "Test"); // NOSONAR
    assertNotEquals(new ODataActionKey(other), cut);
    assertNotEquals(new ODataActionKey(other2), cut);
  }

  @Test
  void checkUnboundAction() throws ODataJPAModelException {
    final IntermediateOperation operation = mock(IntermediateOperation.class);
    final List<CsdlParameter> parameters = new ArrayList<>();
    final CsdlParameter parameter = mock(CsdlParameter.class);
    parameters.add(parameter);
    when(operation.getExternalName()).thenReturn(ACTION_NAME);
    when(operation.getEdmItem()).thenReturn(csdl);
    when(operation.isBound()).thenReturn(false);

    cut = new ODataActionKey(operation);
    assertNull(cut.getBindingParameterType());
  }

  @Test
  void checkToString() {
    final String act = cut.toString();
    assertTrue(act.contains(ACTION_NAME));
    assertTrue(act.contains(TYPE_NAME));
  }
}
