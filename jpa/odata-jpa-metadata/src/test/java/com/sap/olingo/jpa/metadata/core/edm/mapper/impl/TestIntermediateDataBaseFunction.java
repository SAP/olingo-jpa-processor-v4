package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlParameter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmFunction;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmParameter;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.testmodel.AdministrativeDivision;
import com.sap.olingo.jpa.processor.core.testmodel.AdministrativeInformation;
import com.sap.olingo.jpa.processor.core.testmodel.BusinessPartner;
import com.sap.olingo.jpa.processor.core.testmodel.BusinessPartnerRole;
import com.sap.olingo.jpa.processor.core.testmodel.ChangeInformation;
import com.sap.olingo.jpa.processor.core.testmodel.DateConverter;
import com.sap.olingo.jpa.processor.core.testmodel.Organization;
import com.sap.olingo.jpa.processor.core.testmodel.Person;
import com.sap.olingo.jpa.processor.core.util.Assertions;

class TestIntermediateDataBaseFunction extends TestMappingRoot {
  private TestHelper helper;

  @BeforeEach
  void setup() throws ODataJPAModelException {
    helper = new TestHelper(emf.getMetamodel(), PUNIT_NAME);
  }

  @Test
  void checkByEntityAnnotationCreate() throws ODataJPAModelException {
    assertNotNull(new IntermediateDataBaseFunction(new JPADefaultEdmNameBuilder(PUNIT_NAME), helper.getStoredProcedure(
        helper.getEntityType(BusinessPartner.class), "CountRoles"), BusinessPartner.class, helper.schema));
  }

  @Test
  void checkByEntityAnnotationGetName() throws ODataJPAModelException {
    final IntermediateFunction func = new IntermediateDataBaseFunction(new JPADefaultEdmNameBuilder(PUNIT_NAME), helper
        .getStoredProcedure(
            helper.getEntityType(BusinessPartner.class), "CountRoles"), BusinessPartner.class, helper.schema);
    assertEquals("CountRoles", func.getEdmItem().getName());
  }

  @Test
  void checkByEntityAnnotationGetFunctionName() throws ODataJPAModelException {
    final IntermediateFunction func = new IntermediateDataBaseFunction(new JPADefaultEdmNameBuilder(PUNIT_NAME), helper
        .getStoredProcedure(
            helper.getEntityType(BusinessPartner.class), "CountRoles"), BusinessPartner.class, helper.schema);
    assertEquals("COUNT_ROLES", func.getUserDefinedFunction());
  }

  @Test
  void checkByEntityAnnotationInputParameterBound() throws ODataJPAModelException {
    final IntermediateFunction func = new IntermediateDataBaseFunction(new JPADefaultEdmNameBuilder(PUNIT_NAME), helper
        .getStoredProcedure(
            helper.getEntityType(BusinessPartner.class), "CountRoles"), BusinessPartner.class, helper.schema);

    final List<CsdlParameter> expInput = new ArrayList<>();
    final CsdlParameter param = new CsdlParameter();
    param.setName("Key");
    param.setType(new FullQualifiedName("com.sap.olingo.jpa.BusinessPartner"));
    param.setNullable(false);
    expInput.add(param);
    Assertions.assertListEquals(expInput, func.getEdmItem().getParameters(), CsdlParameter.class);
  }

  @Test
  void checkByEntityAnnotationInputParameterBoundCompoundKey() throws ODataJPAModelException {
    final IntermediateFunction func = new IntermediateDataBaseFunction(new JPADefaultEdmNameBuilder(PUNIT_NAME), helper
        .getStoredProcedure(helper.getEntityType(AdministrativeDivision.class), "SiblingsBound"),
        AdministrativeDivision.class, helper.schema);

    final List<CsdlParameter> expInput = new ArrayList<>();
    final CsdlParameter param = new CsdlParameter();
    param.setName("Key");
    param.setType(new FullQualifiedName("com.sap.olingo.jpa.AdministrativeDivision"));
    param.setNullable(false);
    expInput.add(param);
    Assertions.assertListEquals(expInput, func.getEdmItem().getParameters(), CsdlParameter.class);
  }

  @Test
  void checkByEntityAnnotationInputParameter2() throws ODataJPAModelException {
    final IntermediateFunction func = new IntermediateDataBaseFunction(new JPADefaultEdmNameBuilder(PUNIT_NAME), helper
        .getStoredProcedure(
            helper.getEntityType(BusinessPartner.class), "IsPrime"), BusinessPartner.class, helper.schema);

    final List<CsdlParameter> expInput = new ArrayList<>();
    final CsdlParameter param = new CsdlParameter();
    param.setName("Number");
    param.setType(EdmPrimitiveTypeKind.Decimal.getFullQualifiedName());
    param.setNullable(false);
    param.setPrecision(32);
    param.setScale(0);
    expInput.add(param);
    Assertions.assertListEquals(expInput, func.getEdmItem().getParameters(), CsdlParameter.class);
  }

  @Test
  void checkByEntityAnnotationInputParameterIsEnumeration() throws ODataJPAModelException {

    final IntermediateFunction func = new IntermediateDataBaseFunction(new JPADefaultEdmNameBuilder(PUNIT_NAME), helper
        .getStoredProcedure(helper.getEntityType(Person.class), "CheckRights"), BusinessPartner.class, helper.schema);

    assertNotNull(func.getEdmItem().getParameters());
    assertEquals(2, func.getEdmItem().getParameters().size());
    assertEquals(PUNIT_NAME + ".AccessRights", func.getEdmItem().getParameters().get(0).getTypeFQN()
        .getFullQualifiedNameAsString());
    assertEquals("Edm.Int32", func.getEdmItem().getParameters().get(1).getTypeFQN()
        .getFullQualifiedNameAsString());
  }

  @Test
  void checkByEntityAnnotationResultParameterIsEmpty() throws ODataJPAModelException {
    final IntermediateFunction func = new IntermediateDataBaseFunction(new JPADefaultEdmNameBuilder(PUNIT_NAME), helper
        .getStoredProcedure(
            helper.getEntityType(BusinessPartner.class), "CountRoles"), BusinessPartner.class, helper.schema);

    assertEquals(PUNIT_NAME + ".BusinessPartner", func.getEdmItem().getReturnType().getType());
  }

  @Test
  void checkByEntityAnnotationIsBound() throws ODataJPAModelException {
    final IntermediateFunction func = new IntermediateDataBaseFunction(new JPADefaultEdmNameBuilder(PUNIT_NAME), helper
        .getStoredProcedure(
            helper.getEntityType(BusinessPartner.class), "CountRoles"), BusinessPartner.class, helper.schema);

    assertTrue(func.getEdmItem().isBound());
    assertTrue(func.isBound());
    assertEquals(PUNIT_NAME + ".BusinessPartner", func.getEdmItem().getParameters().get(0).getTypeFQN()
        .getFullQualifiedNameAsString());
  }

  @Test
  void checkByEntityAnnotationResultParameterSimple() throws ODataJPAModelException {
    final IntermediateFunction func = new IntermediateDataBaseFunction(new JPADefaultEdmNameBuilder(PUNIT_NAME), helper
        .getStoredProcedure(
            helper.getEntityType(BusinessPartner.class), "IsPrime"), BusinessPartner.class, helper.schema);

    assertEquals(EdmPrimitiveTypeKind.Boolean.getFullQualifiedName().getFullQualifiedNameAsString(), func.getEdmItem()
        .getReturnType().getType());
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  @Test
  void checkByEntityAnnotationResultParameterIsEntity() throws ODataJPAModelException {
    final IntermediateFunction func = (IntermediateFunction) new IntermediateFunctionFactory().create(
        new JPADefaultEdmNameBuilder(
            PUNIT_NAME), helper.getEntityType(Organization.class), helper.schema).get("AllCustomersByABC");
    assertEquals(PUNIT_NAME + ".Organization", func.getEdmItem().getReturnType().getType());
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  @Test
  void checkByEntityAnnotationResultParameterIsCollectionFalse() throws ODataJPAModelException {
    IntermediateFunction func = (IntermediateFunction) new IntermediateFunctionFactory().create(
        new JPADefaultEdmNameBuilder(
            PUNIT_NAME), helper.getEntityType(Organization.class), helper.schema).get("AllCustomersByABC");
    assertTrue(func.getEdmItem().getReturnType().isCollection());

    func = (IntermediateFunction) new IntermediateFunctionFactory().create(new JPADefaultEdmNameBuilder(PUNIT_NAME),
        helper.getEntityType(BusinessPartner.class), helper.schema).get("IsPrime");
    assertFalse(func.getEdmItem().getReturnType().isCollection());
  }

  @Test
  void checkByEntityAnnotationResultParameterNotGiven() throws ODataJPAModelException {
    final IntermediateFunction func = new IntermediateDataBaseFunction(new JPADefaultEdmNameBuilder(PUNIT_NAME), helper
        .getStoredProcedure(
            helper.getEntityType(BusinessPartner.class), "CountRoles"), BusinessPartner.class, helper.schema);

    assertTrue(func.getEdmItem().getReturnType().isCollection());
    assertEquals(PUNIT_NAME + ".BusinessPartner", func.getEdmItem().getReturnType().getType());
    assertEquals(BusinessPartner.class, func.getResultParameter().getType());
  }

  @Test
  @SuppressWarnings({ "rawtypes", "unchecked" })
  void checkByEntityAnnotationResultParameterIsNullable() throws ODataJPAModelException {
    IntermediateFunction func = (IntermediateFunction) new IntermediateFunctionFactory().create(
        new JPADefaultEdmNameBuilder(
            PUNIT_NAME), helper.getEntityType(Organization.class), helper.schema).get("AllCustomersByABC");
    assertTrue(func.getEdmItem().getReturnType().isNullable());

    func = (IntermediateFunction) new IntermediateFunctionFactory().create(new JPADefaultEdmNameBuilder(PUNIT_NAME),
        helper.getEntityType(BusinessPartner.class), helper.schema).get("IsPrime");
    assertFalse(func.getEdmItem().getReturnType().isNullable());
  }

  @Test
  void checkByEntityAnnotationResultParameterEnumerationType() throws ODataJPAModelException {

    final IntermediateFunction func = new IntermediateDataBaseFunction(new JPADefaultEdmNameBuilder(PUNIT_NAME), helper
        .getStoredProcedure(helper.getEntityType(Person.class), "ReturnRights"), BusinessPartner.class, helper.schema);

    assertNotNull(func.getEdmItem().getReturnType());
    assertEquals(PUNIT_NAME + ".AccessRights", func.getEdmItem().getReturnType().getTypeFQN()
        .getFullQualifiedNameAsString());
  }

  @Test
  void checkReturnTypeEmbedded() throws ODataJPAModelException {
    final EdmFunction func = mock(EdmFunction.class);
    final EdmFunction.ReturnType retType = mock(EdmFunction.ReturnType.class);
    // EdmFunctionParameter[] params = new EdmFunctionParameter[0];
    when(func.returnType()).thenReturn(retType);
    when(func.parameter()).thenReturn(new EdmParameter[0]);
    when(retType.type()).thenAnswer(new Answer<Class<?>>() {

      @Override
      public Class<?> answer(final InvocationOnMock invocation) throws Throwable {
        return ChangeInformation.class;
      }
    });

    final IntermediateFunction act = new IntermediateDataBaseFunction(new JPADefaultEdmNameBuilder(PUNIT_NAME), func,
        BusinessPartner.class, helper.schema);
    assertEquals("com.sap.olingo.jpa.ChangeInformation", act.getEdmItem().getReturnType().getTypeFQN()
        .getFullQualifiedNameAsString());
  }

  @Test
  void checkReturnsEntitySetPathForBound() throws ODataJPAModelException {
    final EdmFunction func = createBoundFunction();
    final IntermediateFunction act = new IntermediateDataBaseFunction(new JPADefaultEdmNameBuilder(PUNIT_NAME),
        func, Person.class, helper.schema);

    assertNotNull(act.getEdmItem());
    assertTrue(act.getEdmItem().isBound());
    assertEquals("Person/Roles", act.getEdmItem().getEntitySetPath());
  }

  @Test
  void checkThrowsExcpetionOnEntitySetGivenUnbound() throws ODataJPAModelException {
    final EdmFunction func = createBoundFunction();
    when(func.isBound()).thenReturn(false);

    final IntermediateFunction act = new IntermediateDataBaseFunction(new JPADefaultEdmNameBuilder(PUNIT_NAME),
        func, Person.class, helper.schema);
    assertThrows(ODataJPAModelException.class, () -> act.getEdmItem());
  }

  @Test
  void checkThrowsExcpetionOnEntitySetGivenNoEntityReturnType() throws ODataJPAModelException {
    final EdmFunction func = createBoundFunction();
    when(func.returnType().type()).thenAnswer(new Answer<Class<?>>() {
      @Override
      public Class<?> answer(final InvocationOnMock invocation) throws Throwable {
        return AdministrativeInformation.class;
      }
    });

    final IntermediateFunction act = new IntermediateDataBaseFunction(new JPADefaultEdmNameBuilder(PUNIT_NAME),
        func, Person.class, helper.schema);
    assertThrows(ODataJPAModelException.class, () -> act.getEdmItem());
  }

  @Test
  void checkReturnTypeUnknown() throws ODataJPAModelException {
    final EdmFunction func = mock(EdmFunction.class);
    final EdmFunction.ReturnType retType = mock(EdmFunction.ReturnType.class);
    // EdmFunctionParameter[] params = new EdmFunctionParameter[0];
    when(func.returnType()).thenReturn(retType);
    when(func.parameter()).thenReturn(new EdmParameter[0]);
    when(retType.type()).thenAnswer(new Answer<Class<?>>() {
      @Override
      public Class<?> answer(final InvocationOnMock invocation) throws Throwable {
        return DateConverter.class;
      }
    });
    IntermediateFunction act;
    try {
      act = new IntermediateDataBaseFunction(new JPADefaultEdmNameBuilder(PUNIT_NAME), func, BusinessPartner.class,
          helper.schema);
      act.getEdmItem();
    } catch (final ODataJPAModelException e) {
      assertEquals(ODataJPAModelException.MessageKeys.FUNC_RETURN_TYPE_UNKNOWN.getKey(), e.getId());
      return;
    }
    fail();
  }

  private EdmFunction createBoundFunction() {
    final EdmFunction func = mock(EdmFunction.class);
    final EdmFunction.ReturnType retType = mock(EdmFunction.ReturnType.class);
    when(func.isBound()).thenReturn(true);
    when(func.entitySetPath()).thenReturn("Person/Roles");
    when(func.returnType()).thenReturn(retType);
    when(func.parameter()).thenReturn(new EdmParameter[0]);
    when(retType.type()).thenAnswer(new Answer<Class<?>>() {
      @Override
      public Class<?> answer(final InvocationOnMock invocation) throws Throwable {
        return BusinessPartnerRole.class;
      }
    });
    return func;
  }
}
