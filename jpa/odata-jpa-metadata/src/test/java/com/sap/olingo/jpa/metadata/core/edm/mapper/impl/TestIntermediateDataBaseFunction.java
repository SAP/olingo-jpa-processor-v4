package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.commons.api.edm.provider.CsdlParameter;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmFunction;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmFunctionParameter;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.testmodel.AssertList;
import com.sap.olingo.jpa.processor.core.testmodel.BusinessPartner;
import com.sap.olingo.jpa.processor.core.testmodel.ChangeInformation;
import com.sap.olingo.jpa.processor.core.testmodel.DateConverter;

public class TestIntermediateDataBaseFunction extends TestMappingRoot {
  private TestHelper helper;

  @Before
  public void setup() throws ODataJPAModelException {
    helper = new TestHelper(emf.getMetamodel(), PUNIT_NAME);
  }

  @Test
  public void checkByEntityAnnotationCreate() throws ODataJPAModelException {
    new IntermediateDataBaseFunction(new JPAEdmNameBuilder(PUNIT_NAME), helper.getStoredProcedure(helper.getEntityType(
        "BusinessPartner"), "CountRoles"), BusinessPartner.class, helper.schema);
  }

  @Test
  public void checkByEntityAnnotationGetName() throws ODataJPAModelException {
    IntermediateFunction func = new IntermediateDataBaseFunction(new JPAEdmNameBuilder(PUNIT_NAME), helper
        .getStoredProcedure(
            helper.getEntityType("BusinessPartner"), "CountRoles"), BusinessPartner.class, helper.schema);
    assertEquals("CountRoles", func.getEdmItem().getName());
  }

  @Test
  public void checkByEntityAnnotationGetStoredProcedureName() throws ODataJPAModelException {
    IntermediateFunction func = new IntermediateDataBaseFunction(new JPAEdmNameBuilder(PUNIT_NAME), helper
        .getStoredProcedure(
            helper.getEntityType("BusinessPartner"), "CountRoles"), BusinessPartner.class, helper.schema);
    assertEquals("COUNT_ROLES", func.getUserDefinedFunction());
  }

  @Test
  public void checkByEntityAnnotationInputParameter1() throws ODataJPAModelException {
    IntermediateFunction func = new IntermediateDataBaseFunction(new JPAEdmNameBuilder(PUNIT_NAME), helper
        .getStoredProcedure(
            helper.getEntityType("BusinessPartner"), "CountRoles"), BusinessPartner.class, helper.schema);

    List<CsdlParameter> expInput = new ArrayList<CsdlParameter>();
    CsdlParameter param = new CsdlParameter();
    param.setName("Amount");
    param.setType(EdmPrimitiveTypeKind.Int32.getFullQualifiedName());
    param.setNullable(false);
    expInput.add(param);
    AssertList.assertEquals(expInput, func.getEdmItem().getParameters());
  }

  @Test
  public void checkByEntityAnnotationInputParameter2() throws ODataJPAModelException {
    IntermediateFunction func = new IntermediateDataBaseFunction(new JPAEdmNameBuilder(PUNIT_NAME), helper
        .getStoredProcedure(
            helper.getEntityType("BusinessPartner"), "IsPrime"), BusinessPartner.class, helper.schema);

    List<CsdlParameter> expInput = new ArrayList<CsdlParameter>();
    CsdlParameter param = new CsdlParameter();
    param.setName("Number");
    param.setType(EdmPrimitiveTypeKind.Decimal.getFullQualifiedName());
    param.setNullable(false);
    param.setPrecision(32);
    param.setScale(0);
    expInput.add(param);
    AssertList.assertEquals(expInput, func.getEdmItem().getParameters());
  }

  @Test
  public void checkByEntityAnnotationResultParameterSimple() throws ODataJPAModelException {
    IntermediateFunction func = new IntermediateDataBaseFunction(new JPAEdmNameBuilder(PUNIT_NAME), helper
        .getStoredProcedure(
            helper.getEntityType("BusinessPartner"), "IsPrime"), BusinessPartner.class, helper.schema);

    assertEquals(EdmPrimitiveTypeKind.Boolean.getFullQualifiedName().getFullQualifiedNameAsString(), func.getEdmItem()
        .getReturnType().getType());
  }

  @Test
  public void checkByEntityAnnotationResultParameterIsEmpty() throws ODataJPAModelException {
    IntermediateFunction func = new IntermediateDataBaseFunction(new JPAEdmNameBuilder(PUNIT_NAME), helper
        .getStoredProcedure(
            helper.getEntityType("BusinessPartner"), "CountRoles"), BusinessPartner.class, helper.schema);

    assertEquals(PUNIT_NAME + ".BusinessPartner", func.getEdmItem().getReturnType().getType());
  }

  @Test
  public void checkByEntityAnnotationResultParameterIsEntity() throws ODataJPAModelException {
    IntermediateFunction func = new IntermediateFunctionFactory().create(new JPAEdmNameBuilder(
        PUNIT_NAME), helper.getEntityType("Organization"), helper.schema).get("AllCustomersByABC");
    assertEquals(PUNIT_NAME + ".Organization", func.getEdmItem().getReturnType().getType());
  }

  @Test
  public void checkByEntityAnnotationResultParameterIsCollection() throws ODataJPAModelException {
    IntermediateFunction func = new IntermediateFunctionFactory().create(new JPAEdmNameBuilder(
        PUNIT_NAME), helper.getEntityType("Organization"), helper.schema).get("AllCustomersByABC");
    assertTrue(func.getEdmItem().getReturnType().isCollection());

    func = new IntermediateFunctionFactory().create(new JPAEdmNameBuilder(PUNIT_NAME), helper
        .getEntityType("BusinessPartner"), helper.schema).get("IsPrime");
    assertFalse(func.getEdmItem().getReturnType().isCollection());
  }

  @Test
  public void checkByEntityAnnotationResultParameterIsNullable() throws ODataJPAModelException {
    IntermediateFunction func = new IntermediateFunctionFactory().create(new JPAEdmNameBuilder(
        PUNIT_NAME), helper.getEntityType("Organization"), helper.schema).get("AllCustomersByABC");
    assertTrue(func.getEdmItem().getReturnType().isNullable());

    func = new IntermediateFunctionFactory().create(new JPAEdmNameBuilder(PUNIT_NAME), helper
        .getEntityType("BusinessPartner"), helper.schema).get("IsPrime");
    assertFalse(func.getEdmItem().getReturnType().isNullable());
  }

  @Test
  public void checkReturnTypeEmbedded() throws ODataJPAModelException {
    EdmFunction func = mock(EdmFunction.class);
    EdmFunction.ReturnType retType = mock(EdmFunction.ReturnType.class);
    // EdmFunctionParameter[] params = new EdmFunctionParameter[0];
    when(func.returnType()).thenReturn(retType);
    when(func.parameter()).thenReturn(new EdmFunctionParameter[0]);
    when(retType.type()).thenAnswer(new Answer<Class<?>>() {

      @Override
      public Class<?> answer(InvocationOnMock invocation) throws Throwable {
        return ChangeInformation.class;
      }
    });

    IntermediateFunction act = new IntermediateDataBaseFunction(new JPAEdmNameBuilder(PUNIT_NAME), func,
        BusinessPartner.class,
        helper.schema);
    assertEquals("org.apache.olingo.jpa.ChangeInformation", act.getEdmItem().getReturnType().getTypeFQN()
        .getFullQualifiedNameAsString());
  }

  @Test
  public void checkReturnTypeUnknown() throws ODataJPAModelException {
    EdmFunction func = mock(EdmFunction.class);
    EdmFunction.ReturnType retType = mock(EdmFunction.ReturnType.class);
    // EdmFunctionParameter[] params = new EdmFunctionParameter[0];
    when(func.returnType()).thenReturn(retType);
    when(func.parameter()).thenReturn(new EdmFunctionParameter[0]);
    when(retType.type()).thenAnswer(new Answer<Class<?>>() {
      @Override
      public Class<?> answer(InvocationOnMock invocation) throws Throwable {
        return DateConverter.class;
      }
    });
    IntermediateFunction act;
    try {
      act = new IntermediateDataBaseFunction(new JPAEdmNameBuilder(PUNIT_NAME), func, BusinessPartner.class,
          helper.schema);
      act.getEdmItem();
    } catch (ODataJPAModelException e) {
      assertEquals(ODataJPAModelException.MessageKeys.FUNC_RETURN_TYPE_UNKNOWN.getKey(), e.getId());
      return;
    }
    fail();
  }

}
