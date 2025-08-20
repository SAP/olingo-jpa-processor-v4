package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import static com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException.MessageKeys.PROPERTY_PRECISION_NOT_IN_RANGE;
import static com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException.MessageKeys.TRANSIENT_CALCULATOR_TOO_MANY_CONSTRUCTORS;
import static com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException.MessageKeys.TRANSIENT_CALCULATOR_WRONG_PARAMETER;
import static com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException.MessageKeys.TRANSIENT_KEY_NOT_SUPPORTED;
import static com.sap.olingo.jpa.processor.core.util.Assertions.assertException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Member;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.persistence.Column;
import jakarta.persistence.metamodel.Attribute;
import jakarta.persistence.metamodel.Attribute.PersistentAttributeType;
import jakarta.persistence.metamodel.EmbeddableType;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.ManagedType;
import jakarta.persistence.metamodel.SingularAttribute;

import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.commons.api.edm.geo.Geospatial.Dimension;
import org.apache.olingo.commons.api.edm.provider.CsdlAnnotation;
import org.apache.olingo.commons.api.edm.provider.CsdlTypeDefinition;
import org.apache.olingo.commons.api.edm.provider.annotation.CsdlConstantExpression;
import org.apache.olingo.commons.api.edm.provider.annotation.CsdlConstantExpression.ConstantExpressionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.sap.olingo.jpa.metadata.api.JPAEdmMetadataPostProcessor;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmGeospatial;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmProtectedBy;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmProtections;
import com.sap.olingo.jpa.metadata.core.edm.extension.vocabularies.Applicability;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEdmNameBuilder;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extension.IntermediateEntityTypeAccess;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extension.IntermediateNavigationPropertyAccess;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extension.IntermediatePropertyAccess;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extension.IntermediateReferenceList;
import com.sap.olingo.jpa.metadata.core.edm.mapper.util.AnnotationTestHelper;
import com.sap.olingo.jpa.metadata.core.edm.mapper.util.MemberDouble;
import com.sap.olingo.jpa.metadata.odata.v4.core.terms.GeneralProperty;
import com.sap.olingo.jpa.metadata.odata.v4.core.terms.Terms;
import com.sap.olingo.jpa.metadata.odata.v4.general.Aliases;
import com.sap.olingo.jpa.metadata.odata.v4.provider.JavaBasedCoreAnnotationsProvider;
import com.sap.olingo.jpa.processor.core.errormodel.TeamWithTransientCalculatorConstructorError;
import com.sap.olingo.jpa.processor.core.errormodel.TeamWithTransientCalculatorError;
import com.sap.olingo.jpa.processor.core.errormodel.TeamWithTransientKey;
import com.sap.olingo.jpa.processor.core.testmodel.AdministrativeDivision;
import com.sap.olingo.jpa.processor.core.testmodel.AdministrativeDivisionDescription;
import com.sap.olingo.jpa.processor.core.testmodel.AnnotationsParent;
import com.sap.olingo.jpa.processor.core.testmodel.BusinessPartner;
import com.sap.olingo.jpa.processor.core.testmodel.BusinessPartnerProtected;
import com.sap.olingo.jpa.processor.core.testmodel.Collection;
import com.sap.olingo.jpa.processor.core.testmodel.Comment;
import com.sap.olingo.jpa.processor.core.testmodel.CommunicationData;
import com.sap.olingo.jpa.processor.core.testmodel.ComplexBaseType;
import com.sap.olingo.jpa.processor.core.testmodel.DummyToBeIgnored;
import com.sap.olingo.jpa.processor.core.testmodel.Organization;
import com.sap.olingo.jpa.processor.core.testmodel.Person;
import com.sap.olingo.jpa.processor.core.testmodel.PersonImage;
import com.sap.olingo.jpa.processor.core.testmodel.PostalAddressData;
import com.sap.olingo.jpa.processor.core.testmodel.RestrictedEntityComplex;
import com.sap.olingo.jpa.processor.core.testmodel.RestrictedEntityUnrestrictedSource;
import com.sap.olingo.jpa.processor.core.testmodel.User;

class IntermediateSimplePropertyTest extends TestMappingRoot {
  private TestHelper helper;
  private TestHelper errorHelper;
  private JPAEdmMetadataPostProcessor processor;

  @BeforeEach
  void setup() throws ODataJPAModelException {
    helper = new TestHelper(emf.getMetamodel(), PUNIT_NAME);
    errorHelper = new TestHelper(errorEmf.getMetamodel(), ERROR_PUNIT);
    processor = mock(JPAEdmMetadataPostProcessor.class);
  }

  @Test
  void checkPropertyCanBeCreated() throws ODataJPAModelException {
    final EmbeddableType<?> et = helper.getEmbeddableType(CommunicationData.class);
    final Attribute<?, ?> jpaAttribute = helper.getAttribute(et, "landlinePhoneNumber");
    assertNotNull(new IntermediateSimpleProperty(new JPADefaultEdmNameBuilder(PUNIT_NAME), jpaAttribute,
        helper.schema));
  }

  @Test
  void checkGetPropertyName() throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = helper.getAttribute(helper.getEntityType(BusinessPartner.class), "type");
    final var property = new IntermediateSimpleProperty(new JPADefaultEdmNameBuilder(PUNIT_NAME),
        jpaAttribute, helper.schema);
    assertEquals("Type", property.getEdmItem().getName(), "Wrong name");
  }

  @Test
  void checkGetPropertyDBFieldNameFromColumn() throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = helper.getAttribute(helper.getEntityType(BusinessPartner.class), "type");
    final var property = new IntermediateSimpleProperty(new JPADefaultEdmNameBuilder(PUNIT_NAME),
        jpaAttribute, helper.schema);
    assertEquals("\"Type\"", property.getDBFieldName(), "Wrong name");
  }

  @Test
  void checkGetPropertyDBFieldNameNotProvidedDifferentNameBuilder() throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = helper.getAttribute(helper.getComplexType(ComplexBaseType.class),
        "oneAttribute");
    final var property = new IntermediateSimpleProperty(new LocalJPAEdmNameBuilder(), jpaAttribute, helper.schema);
    assertEquals("ONE_ATTRIBUTE", property.getDBFieldName(), "Wrong name");
  }

  @Test
  void checkGetPropertyDBFieldNameNotProvided() throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = helper.getAttribute(helper.getComplexType(ComplexBaseType.class),
        "oneAttribute");
    final var property = new IntermediateSimpleProperty(new JPADefaultEdmNameBuilder(PUNIT_NAME),
        jpaAttribute, helper.schema);
    assertEquals("\"oneAttribute\"", property.getDBFieldName(), "Wrong name");
  }

  @Test
  void checkGetPropertySimpleType() throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = helper.getAttribute(helper.getEntityType(BusinessPartner.class), "type");
    final var property = new IntermediateSimpleProperty(new JPADefaultEdmNameBuilder(PUNIT_NAME),
        jpaAttribute, helper.schema);
    assertEquals(EdmPrimitiveTypeKind.String.getFullQualifiedName().getFullQualifiedNameAsString(),
        property.getEdmItem().getType(), "Wrong type");
  }

  @Test
  void checkGetPropertyComplexType() throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = helper.getAttribute(helper.getEntityType(BusinessPartner.class),
        "communicationData");
    final var property = new IntermediateSimpleProperty(new JPADefaultEdmNameBuilder(PUNIT_NAME),
        jpaAttribute, helper.schema);
    assertEquals(PUNIT_NAME + ".CommunicationData", property.getEdmItem().getType(), "Wrong type");
  }

  @Test
  void checkGetPropertyEnumTypeWithoutConverter() throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = helper.getAttribute(helper.getEntityType(Organization.class), "aBCClass");
    final var property = new IntermediateSimpleProperty(new JPADefaultEdmNameBuilder(PUNIT_NAME),
        jpaAttribute, helper.schema);
    assertEquals("com.sap.olingo.jpa.ABCClassification", property.getEdmItem().getType(), "Wrong type");
  }

  @Test
  void checkGetPropertyEnumTypeWithoutConverterMustNotHaveMapper() throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = helper.getAttribute(helper.getEntityType(Organization.class), "aBCClass");
    final var property = new IntermediateSimpleProperty(new JPADefaultEdmNameBuilder(PUNIT_NAME),
        jpaAttribute, helper.schema);
    assertNull(property.getEdmItem().getMapping());
  }

  @Test
  void checkGetPropertyEnumTypeWithConverter() throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = helper.getAttribute(helper.getEntityType(Person.class), "accessRights");
    final var property = new IntermediateSimpleProperty(new JPADefaultEdmNameBuilder(PUNIT_NAME),
        jpaAttribute, helper.schema);
    assertEquals("com.sap.olingo.jpa.AccessRights", property.getEdmItem().getType(), "Wrong type");
  }

  @Test
  void checkGetPropertyEnumTypeWithEnumerated() throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = helper.getAttribute(helper.getEntityType(User.class), "userType");
    final var property = new IntermediateSimpleProperty(new JPADefaultEdmNameBuilder(PUNIT_NAME),
        jpaAttribute, helper.schema);
    assertEquals(String.class, property.getDbType());
    assertNotNull(property.getEdmItem());
  }

  @Test
  void checkGetPropertyIgnoreFalse() throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = helper.getAttribute(helper.getEntityType(BusinessPartner.class), "type");
    final IntermediatePropertyAccess property = new IntermediateSimpleProperty(new JPADefaultEdmNameBuilder(PUNIT_NAME),
        jpaAttribute, helper.schema);
    assertFalse(property.ignore());
  }

  @Test
  void checkGetPropertyIgnoreTrue() throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = helper.getAttribute(helper.getEntityType(BusinessPartner.class),
        "customString1");
    final IntermediatePropertyAccess property = new IntermediateSimpleProperty(new JPADefaultEdmNameBuilder(PUNIT_NAME),
        jpaAttribute, helper.schema);
    assertTrue(property.ignore());
  }

  @Test
  void checkGetPropertyFacetsNullableTrue() throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = helper.getAttribute(helper.getEntityType(BusinessPartner.class),
        "customString1");
    final var property = new IntermediateSimpleProperty(new JPADefaultEdmNameBuilder(PUNIT_NAME),
        jpaAttribute, helper.schema);
    assertTrue(property.getEdmItem().isNullable());
  }

  @Test
  void checkGetPropertyFacetsNullableTrueComplex() throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = helper.getAttribute(
        helper.getEmbeddableType(PostalAddressData.class), "pOBox");
    final var property = new IntermediateSimpleProperty(new JPADefaultEdmNameBuilder(PUNIT_NAME),
        jpaAttribute, helper.schema);
    assertTrue(property.getEdmItem().isNullable());
  }

  @Test
  void checkGetPropertyFacetsNullableFalse() throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = helper.getAttribute(helper.getEntityType(BusinessPartner.class), "eTag");
    final var property = new IntermediateSimpleProperty(new JPADefaultEdmNameBuilder(PUNIT_NAME),
        jpaAttribute, helper.schema);
    assertFalse(property.getEdmItem().isNullable());
  }

  @Test
  void checkGetPropertyIsEtagTrue() throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = helper.getAttribute(helper.getEntityType(BusinessPartner.class), "eTag");
    final var property = new IntermediateSimpleProperty(new JPADefaultEdmNameBuilder(PUNIT_NAME),
        jpaAttribute, helper.schema);
    assertTrue(property.isEtag());
  }

  @Test
  void checkGetPropertyIsEtagFalse() throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = helper.getAttribute(helper.getEntityType(BusinessPartner.class), "type");
    final var property = new IntermediateSimpleProperty(new JPADefaultEdmNameBuilder(PUNIT_NAME),
        jpaAttribute, helper.schema);

    assertFalse(property.isEtag());
  }

  @Test
  void checkGetPropertyMaxLength() throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = helper.getAttribute(helper.getEntityType(BusinessPartner.class), "type");
    final var property = new IntermediateSimpleProperty(new JPADefaultEdmNameBuilder(PUNIT_NAME),
        jpaAttribute, helper.schema);

    assertEquals(Integer.valueOf(1), property.getEdmItem().getMaxLength());
  }

  @Test
  void checkGetPropertyMaxLengthNullForClob() throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = helper.getAttribute(helper.getComplexType("DummyEmbeddedToIgnore"), "command");
    final var property = new IntermediateSimpleProperty(new JPADefaultEdmNameBuilder(PUNIT_NAME),
        jpaAttribute, helper.schema);

    assertNull(property.getEdmItem().getMaxLength());
  }

  @Test
  void checkGetPropertyPrecisionDecimal() throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = helper.getAttribute(helper.getEntityType(BusinessPartner.class), "customNum1");
    final var property = new IntermediateSimpleProperty(new JPADefaultEdmNameBuilder(PUNIT_NAME),
        jpaAttribute, helper.schema);

    assertEquals(Integer.valueOf(16), property.getEdmItem().getPrecision());
  }

  @Test
  void checkGetPropertyScaleDecimal() throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = helper.getAttribute(helper.getEntityType(BusinessPartner.class), "customNum1");
    final var property = new IntermediateSimpleProperty(new JPADefaultEdmNameBuilder(PUNIT_NAME),
        jpaAttribute, helper.schema);
    assertEquals(Integer.valueOf(5), property.getEdmItem().getScale());
  }

  @Test
  void checkGetPropertyPrecisionTime() throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = helper.getAttribute(helper.getEntityType(BusinessPartner.class),
        "creationDateTime");
    final var property = new IntermediateSimpleProperty(new JPADefaultEdmNameBuilder(PUNIT_NAME),
        jpaAttribute, helper.schema);

    assertEquals(Integer.valueOf(2), property.getEdmItem().getPrecision());
  }

  @Test
  void checkGetPropertyMapper() throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = helper.getAttribute(helper.getEntityType(BusinessPartner.class),
        "creationDateTime");
    final var property = new IntermediateSimpleProperty(new JPADefaultEdmNameBuilder(PUNIT_NAME),
        jpaAttribute, helper.schema);
    assertNotNull(property.getEdmItem().getMapping());
    assertEquals(Timestamp.class, property.getEdmItem().getMapping().getMappedJavaClass());
  }

  @Test
  void checkGetPropertyMapperWithConverter() throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = helper.getAttribute(helper.getEntityType(Collection.class), "localDateTime");
    final var property = new IntermediateSimpleProperty(new JPADefaultEdmNameBuilder(PUNIT_NAME),
        jpaAttribute, helper.schema);
    assertNotNull(property.getEdmItem().getMapping());
    assertEquals(java.util.Date.class, property.getEdmItem().getMapping().getMappedJavaClass());
  }

  @Test
  void checkGetNoPropertyMapperForClob() throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = helper.getAttribute(helper.getEntityType(Comment.class), "text");
    final var property = new IntermediateSimpleProperty(new JPADefaultEdmNameBuilder(PUNIT_NAME),
        jpaAttribute,
        helper.schema);
    assertNull(property.getEdmItem().getMapping());
  }

  @Test
  void checkPostProcessorCalled() throws ODataJPAModelException {
    IntermediateSimpleProperty.setPostProcessor(processor);
    final Attribute<?, ?> jpaAttribute = helper.getAttribute(helper.getEntityType(BusinessPartner.class),
        "creationDateTime");
    final var property = new IntermediateSimpleProperty(new JPADefaultEdmNameBuilder(PUNIT_NAME),
        jpaAttribute, helper.schema);

    property.getEdmItem();
    verify(processor, atLeastOnce()).processProperty(property, BUPA_CANONICAL_NAME);
  }

  @Test
  void checkPostProcessorAnnotationAdded() throws ODataJPAModelException {
    final var postProcessorDouble = new PostProcessorSetName();
    IntermediateSimpleProperty.setPostProcessor(postProcessorDouble);

    final Attribute<?, ?> jpaAttribute = helper.getAttribute(helper.getEntityType(BusinessPartner.class),
        "customString1");
    final var cut = new IntermediateSimpleProperty(new JPADefaultEdmNameBuilder(PUNIT_NAME),
        jpaAttribute, helper.schema);

    assertEquals(1L, cut.getEdmItem().getAnnotations().stream().filter(a -> a.getTerm().equals("Immutable")).count());
  }

  @Test
  void checkGetConverterReturnedConversionRequired() throws ODataJPAModelException {
    final var postProcessorDouble = new PostProcessorSetName();
    IntermediateModelElement.setPostProcessor(postProcessorDouble);

    final Attribute<?, ?> jpaAttribute = helper.getAttribute(helper.getEntityType(BusinessPartner.class),
        "creationDateTime");
    final var property = new IntermediateSimpleProperty(new JPADefaultEdmNameBuilder(PUNIT_NAME),
        jpaAttribute, helper.schema);

    assertNotNull(property.getConverter());
  }

  @Test
  void checkGetConverterNullNoConverterDefined() throws ODataJPAModelException {
    final var postProcessorDouble = new PostProcessorSetName();
    IntermediateModelElement.setPostProcessor(postProcessorDouble);

    final Attribute<?, ?> jpaAttribute = helper.getAttribute(helper.getEntityType(Person.class), "customString2");
    final var property = new IntermediateSimpleProperty(new JPADefaultEdmNameBuilder(PUNIT_NAME),
        jpaAttribute, helper.schema);

    assertNull(property.getRawConverter());
  }

  @Test
  void checkGetConverterNullDBTypeEqJavaType() throws ODataJPAModelException {
    final var postProcessorDouble = new PostProcessorSetName();
    IntermediateModelElement.setPostProcessor(postProcessorDouble);

    final Attribute<?, ?> jpaAttribute = helper.getAttribute(helper.getEntityType(Person.class), "customString1");
    final var property = new IntermediateSimpleProperty(new JPADefaultEdmNameBuilder(PUNIT_NAME),
        jpaAttribute, helper.schema);

    assertNull(property.getConverter());
  }

  @Test
  void checkGetConverterNullConversionNotRequired() throws ODataJPAModelException {
    final var postProcessorDouble = new PostProcessorSetName();
    IntermediateModelElement.setPostProcessor(postProcessorDouble);

    final Attribute<?, ?> jpaAttribute = helper.getAttribute(helper.getEntityType(DummyToBeIgnored.class), "uuid");
    final var property = new IntermediateSimpleProperty(new JPADefaultEdmNameBuilder(PUNIT_NAME),
        jpaAttribute, helper.schema);

    assertNull(property.getConverter());
  }

  @Test
  void checkGetRawConverterReturnedConversionRequired() throws ODataJPAModelException {
    final var postProcessorDouble = new PostProcessorSetName();
    IntermediateModelElement.setPostProcessor(postProcessorDouble);

    final Attribute<?, ?> jpaAttribute = helper.getAttribute(helper.getEntityType(BusinessPartner.class),
        "creationDateTime");
    final var property = new IntermediateSimpleProperty(new JPADefaultEdmNameBuilder(PUNIT_NAME),
        jpaAttribute, helper.schema);

    assertNotNull(property.getRawConverter());
  }

  @Test
  void checkGetRawConverterNullNoConverterDefined() throws ODataJPAModelException {
    final var postProcessorDouble = new PostProcessorSetName();
    IntermediateModelElement.setPostProcessor(postProcessorDouble);

    final Attribute<?, ?> jpaAttribute = helper.getAttribute(helper.getEntityType(Person.class), "customString2");
    final var property = new IntermediateSimpleProperty(new JPADefaultEdmNameBuilder(PUNIT_NAME),
        jpaAttribute, helper.schema);

    assertNull(property.getRawConverter());
  }

  @Test
  void checkGetRawConverterReturnsConversionNotRequired() throws ODataJPAModelException {
    final var postProcessorDouble = new PostProcessorSetName();
    IntermediateModelElement.setPostProcessor(postProcessorDouble);

    final Attribute<?, ?> jpaAttribute = helper.getAttribute(helper.getEntityType(DummyToBeIgnored.class), "uuid");
    final var property = new IntermediateSimpleProperty(new JPADefaultEdmNameBuilder(PUNIT_NAME),
        jpaAttribute, helper.schema);

    assertNotNull(property.getRawConverter());
  }

  @Test
  void checkGetPropertyDefaultValue() throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = helper.getAttribute(helper.getEmbeddableType(PostalAddressData.class),
        "regionCodePublisher");
    final var property = new IntermediateSimpleProperty(nameBuilder,
        jpaAttribute, helper.schema);
    assertEquals("ISO", property.getEdmItem().getDefaultValue());
  }

  @Test
  void checkGetPropertyDefaultValueIgnoredOnAbstractClass() throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = helper.getAttribute(helper.getEntityType(BusinessPartner.class),
        "iD");
    final var property = new IntermediateSimpleProperty(nameBuilder,
        jpaAttribute, helper.schema);
    assertNull(property.getEdmItem().getDefaultValue());
  }

  @Test
  void checkGetPropertyIsStream() throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = helper.getAttribute(helper.getEntityType(PersonImage.class),
        "image");
    final var property = new IntermediateSimpleProperty(nameBuilder,
        jpaAttribute, helper.schema);
    assertTrue(property.isStream());
  }

  @Test
  void checkGetPropertyIsTransientTrue() throws ODataJPAModelException, NoSuchFieldException, SecurityException {

    final Attribute<?, ?> jpaAttribute = new IntermediateStructuredType.TransientSingularAttribute<>(helper
        .getEntityType(Person.class), Person.class.getDeclaredField("fullName"));

    final var property = new IntermediateSimpleProperty(nameBuilder,
        jpaAttribute, helper.schema);
    assertTrue(property.isTransient());
  }

  @Test
  void checkGetPropertyIsTransientFalse() throws ODataJPAModelException, NoSuchFieldException,
      SecurityException {

    final Attribute<?, ?> jpaAttribute = new IntermediateStructuredType.TransientSingularAttribute<>(helper
        .getEntityType(Person.class), Person.class.getDeclaredField("lastName"));

    final var property = new IntermediateSimpleProperty(nameBuilder,
        jpaAttribute, helper.schema);
    assertFalse(property.isTransient());
  }

  @Test
  void checkGetPropertyIsTransientThrowExceptionOnBeingKey() throws NoSuchFieldException, SecurityException {

    final Attribute<?, ?> jpaAttribute = new IntermediateStructuredType.TransientSingularAttribute<>(errorHelper
        .getEntityType(TeamWithTransientKey.class), TeamWithTransientKey.class.getDeclaredField("name"));
    assertException(ODataJPAModelException.class,
        () -> new IntermediateSimpleProperty(errorNameBuilder, jpaAttribute, errorHelper.schema),
        TRANSIENT_KEY_NOT_SUPPORTED.getKey());
  }

  @Test
  void checkGetPropertyGetCalculatorPersistentIsNull() throws ODataJPAModelException, NoSuchFieldException,
      SecurityException {

    final Attribute<?, ?> jpaAttribute = new IntermediateStructuredType.TransientSingularAttribute<>(helper
        .getEntityType(Person.class), Person.class.getDeclaredField("lastName"));
    final var property = new IntermediateSimpleProperty(nameBuilder,
        jpaAttribute, helper.schema);
    assertNull(property.getCalculatorConstructor());
  }

  @Test
  void checkGetPropertyGetCalculatorTransientNotNull() throws ODataJPAModelException, NoSuchFieldException,
      SecurityException {

    final Attribute<?, ?> jpaAttribute = new IntermediateStructuredType.TransientSingularAttribute<>(helper
        .getEntityType(Person.class), Person.class.getDeclaredField("fullName"));

    final var property = new IntermediateSimpleProperty(nameBuilder,
        jpaAttribute, helper.schema);
    assertNotNull(property.getCalculatorConstructor());
  }

  @Test
  void checkTransientIsCollectionFalse() throws NoSuchFieldException, SecurityException {

    final SingularAttribute<?, ?> jpaAttribute = new IntermediateStructuredType.TransientSingularAttribute<>(helper
        .getEntityType(Person.class), Person.class.getDeclaredField("fullName"));
    assertFalse(jpaAttribute.isCollection());
  }

  @Test
  void checkTransientBindableTypeNull() throws NoSuchFieldException, SecurityException {

    final SingularAttribute<?, ?> jpaAttribute = new IntermediateStructuredType.TransientSingularAttribute<>(helper
        .getEntityType(Person.class), Person.class.getDeclaredField("fullName"));
    assertNull(jpaAttribute.getBindableType());
  }

  @Test
  void checkTransientBindableJavaTypeNull() throws NoSuchFieldException, SecurityException {

    final SingularAttribute<?, ?> jpaAttribute = new IntermediateStructuredType.TransientSingularAttribute<>(helper
        .getEntityType(Person.class), Person.class.getDeclaredField("fullName"));
    assertNull(jpaAttribute.getBindableJavaType());
  }

  @Test
  void checkTransientTypeNull() throws NoSuchFieldException, SecurityException {

    final SingularAttribute<?, ?> jpaAttribute = new IntermediateStructuredType.TransientSingularAttribute<>(helper
        .getEntityType(Person.class), Person.class.getDeclaredField("fullName"));
    assertNull(jpaAttribute.getType());
  }

  @Test
  void checkTransientIsVersionFalse() throws NoSuchFieldException, SecurityException {

    final SingularAttribute<?, ?> jpaAttribute = new IntermediateStructuredType.TransientSingularAttribute<>(helper
        .getEntityType(Person.class), Person.class.getDeclaredField("fullName"));
    assertFalse(jpaAttribute.isVersion());
  }

  @Test
  void checkTransientIsOptionalTrue() throws NoSuchFieldException, SecurityException {

    final SingularAttribute<?, ?> jpaAttribute = new IntermediateStructuredType.TransientSingularAttribute<>(helper
        .getEntityType(Person.class), Person.class.getDeclaredField("fullName"));
    assertTrue(jpaAttribute.isOptional());
  }

  @Test
  void checkGetPropertyGetCalculatorThrowExceptionOnMultipleConstructors() throws NoSuchFieldException,
      SecurityException {

    final Attribute<?, ?> jpaAttribute = new IntermediateStructuredType.TransientSingularAttribute<>(errorHelper
        .getEntityType(
            TeamWithTransientCalculatorError.class), TeamWithTransientCalculatorError.class.getDeclaredField(
                "completeName"));
    assertException(ODataJPAModelException.class,
        () -> new IntermediateSimpleProperty(errorNameBuilder, jpaAttribute, errorHelper.schema),
        TRANSIENT_CALCULATOR_TOO_MANY_CONSTRUCTORS.getKey());
  }

  @Test
  void checkGetPropertyGetCalculatorThrowExceptionOnWrongConstructors() throws NoSuchFieldException, SecurityException {

    final Attribute<?, ?> jpaAttribute = new IntermediateStructuredType.TransientSingularAttribute<>(errorHelper
        .getEntityType(
            TeamWithTransientCalculatorConstructorError.class), TeamWithTransientCalculatorConstructorError.class
                .getDeclaredField("completeName"));
    assertException(ODataJPAModelException.class,
        () -> new IntermediateSimpleProperty(errorNameBuilder, jpaAttribute, errorHelper.schema),
        TRANSIENT_CALCULATOR_WRONG_PARAMETER.getKey());
  }

  @Test
  void checkGetPropertyGetCalculatorNullOnPersistentProperty() throws ODataJPAModelException,
      NoSuchFieldException,
      SecurityException {

    final Attribute<?, ?> jpaAttribute = new IntermediateStructuredType.TransientSingularAttribute<>(helper
        .getEntityType(Person.class), Person.class.getDeclaredField("lastName"));

    final var property = new IntermediateSimpleProperty(nameBuilder, jpaAttribute,
        helper.schema);
    assertNull(property.getCalculatorConstructor());
  }

  @Test
  void checkGetTypeBoxedForPrimitive() throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = helper.getAttribute(helper.getEntityType(AdministrativeDivision.class),
        "population");
    final var property = new IntermediateSimpleProperty(nameBuilder,
        jpaAttribute, helper.schema);

    assertEquals(Long.class, property.getType());
  }

  @Test
  void checkGetTypeBoxed() throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = helper.getAttribute(helper.getEntityType(AdministrativeDivision.class),
        "area");
    final var property = new IntermediateSimpleProperty(nameBuilder,
        jpaAttribute, helper.schema);

    assertEquals(Long.class, property.getType());
  }

  @Test
  void checkGetTypeConversionRequired() throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = helper.getAttribute(helper.getEntityType(BusinessPartner.class),
        "creationDateTime");
    final var property = new IntermediateSimpleProperty(new JPADefaultEdmNameBuilder(PUNIT_NAME),
        jpaAttribute, helper.schema);

    assertTrue(property.conversionRequired);
    assertEquals(Timestamp.class, property.getType());
    assertEquals(Timestamp.class, property.getDbType());
    assertEquals(LocalDateTime.class, property.getJavaType());
  }

  @Test
  void checkGetTypeConversionNotRequired() throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = helper.getAttribute(helper.getEntityType(Person.class),
        "birthDay");
    final var property = new IntermediateSimpleProperty(nameBuilder,
        jpaAttribute, helper.schema);

    assertFalse(property.conversionRequired);
    assertEquals(LocalDate.class, property.getType());
    assertEquals(Date.class, property.getDbType());
    assertEquals(LocalDate.class, property.getJavaType());
  }

  @Test
  void checkTimestampWithoutPrecisionReturns0() throws ODataJPAModelException {
    // If Precision missing EdmDateTimeOffset.internalValueToString throws an exception => pre-check
    final Attribute<?, ?> jpaAttribute = mock(Attribute.class);
    final ManagedType<?> jpaManagedType = mock(ManagedType.class);
    when(jpaAttribute.getName()).thenReturn("start");
    when(jpaAttribute.getPersistentAttributeType()).thenReturn(PersistentAttributeType.BASIC);
    when(jpaAttribute.getDeclaringType()).thenAnswer(new Answer<ManagedType<?>>() {
      @Override
      public ManagedType<?> answer(final InvocationOnMock invocation) throws Throwable {
        return jpaManagedType;
      }
    });
    when(jpaAttribute.getJavaType()).thenAnswer(new Answer<Class<?>>() {
      @Override
      public Class<?> answer(final InvocationOnMock invocation) throws Throwable {
        return Timestamp.class;
      }
    });
    when(jpaManagedType.getJavaType()).thenAnswer(new Answer<Class<?>>() {
      @Override
      public Class<?> answer(final InvocationOnMock invocation) throws Throwable {
        return DummyToBeIgnored.class;
      }
    });

    final var column = mock(Column.class);
    final var annotations = mock(AnnotatedElement.class, withSettings().extraInterfaces(Member.class));
    when(annotations.getAnnotation(Column.class)).thenReturn(column);
    when(jpaAttribute.getJavaMember()).thenReturn((Member) annotations);
    when(column.name()).thenReturn("Test");

    final var property = new IntermediateSimpleProperty(nameBuilder,
        jpaAttribute, helper.schema);

    assertEquals(0, property.getEdmItem().getPrecision());
  }

  @Test
  void checkGetPropertyHasProtectionFalse() throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = helper.getAttribute(helper.getEntityType(BusinessPartnerProtected.class),
        "eTag");
    final IntermediatePropertyAccess property = new IntermediateSimpleProperty(nameBuilder,
        jpaAttribute, helper.schema);
    assertFalse(property.hasProtection());
  }

  @Test
  void checkGetPropertyHasProtectionTrue() throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = helper.getAttribute(helper.getEntityType(BusinessPartnerProtected.class),
        "userName");
    final IntermediatePropertyAccess property = new IntermediateSimpleProperty(nameBuilder,
        jpaAttribute, helper.schema);
    assertTrue(property.hasProtection());
  }

  @Test
  void checkGetPropertyProtectionSupportsWildCardTrue() throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = helper.getAttribute(helper.getEntityType(BusinessPartnerProtected.class),
        "userName");
    final IntermediateProperty property = new IntermediateSimpleProperty(nameBuilder,
        jpaAttribute, helper.schema);
    assertTrue(property.protectionWithWildcard("UserId", String.class));
  }

  @Test
  void checkGetPropertyProtectionSupportsWildCardFalse() throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = helper.getAttribute(helper.getComplexType(
        "InhouseAddressWithThreeProtections"),
        "building");
    final IntermediateProperty property = new IntermediateSimpleProperty(nameBuilder,
        jpaAttribute, helper.schema);
    assertFalse(property.protectionWithWildcard("BuildingNumber", String.class));
  }

  @Test
  void checkGetPropertyProtectionSupportsWildCardFalseNonString() throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = helper.getAttribute(helper.getComplexType(
        "InhouseAddressWithThreeProtections"),
        "roomNumber");
    final IntermediateProperty property = new IntermediateSimpleProperty(nameBuilder,
        jpaAttribute, helper.schema);
    assertFalse(property.protectionWithWildcard("RoomNumber", Integer.class));
  }

  @Test
  void checkGetPropertyProtectedAttributeClaimName() throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = helper.getAttribute(helper.getEntityType(BusinessPartnerProtected.class),
        "userName");
    final IntermediateProperty property = new IntermediateSimpleProperty(nameBuilder,
        jpaAttribute, helper.schema);
    assertEquals("UserId", property.getProtectionClaimNames().toArray(new String[] {})[0]);
    assertNotNull(property.getProtectionPath("UserId"));
    final var actPath = property.getProtectionPath("UserId");
    assertEquals(1, actPath.size());
    assertEquals("UserName", actPath.get(0));
  }

  @Test
  void checkGetPropertyNotProtectedAttributeClaimName() throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = helper.getAttribute(helper.getEntityType(BusinessPartnerProtected.class),
        "eTag");
    final IntermediateProperty property = new IntermediateSimpleProperty(nameBuilder,
        jpaAttribute, helper.schema);
    assertTrue(property.getProtectionClaimNames().isEmpty());
    assertTrue(property.getProtectionPath("Username").isEmpty());
  }

  @Test
  void checkGetComplexPropertyProtectedAttributeClaimName() throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = helper.getAttribute(helper.getEntityType(BusinessPartnerProtected.class),
        "administrativeInformation");

    final var annotation = Mockito.mock(EdmProtectedBy.class);
    when(annotation.name()).thenReturn("UserId");
    when(annotation.path()).thenReturn("created/by");

    final var memberSpy = new MemberDouble(jpaAttribute.getJavaMember());
    memberSpy.addAnnotation(EdmProtectedBy.class, annotation);
    final Attribute<?, ?> attributeSpy = Mockito.spy(jpaAttribute);
    when(attributeSpy.getJavaMember()).thenReturn(memberSpy);

    final IntermediateProperty property = new IntermediateSimpleProperty(nameBuilder,
        attributeSpy, helper.schema);
    assertEquals("UserId", property.getProtectionClaimNames().toArray(new String[] {})[0]);
    assertNotNull(property.getProtectionPath("UserId"));
    final var actPath = property.getProtectionPath("UserId");
    assertEquals(1, actPath.size());
    assertEquals("AdministrativeInformation/Created/By", actPath.get(0));
  }

  @Test
  void checkGetComplexPropertyTwoProtectedAttributeClaimName() throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = helper.getAttribute(helper.getEntityType(BusinessPartnerProtected.class),
        "administrativeInformation");

    final var protections = Mockito.mock(EdmProtections.class);
    final var protectedBy1 = Mockito.mock(EdmProtectedBy.class);
    when(protectedBy1.name()).thenReturn("UserId");
    when(protectedBy1.path()).thenReturn("created/by");

    final var protectedBy2 = Mockito.mock(EdmProtectedBy.class);
    when(protectedBy2.name()).thenReturn("UserId");
    when(protectedBy2.path()).thenReturn("updated/by");

    when(protections.value()).thenReturn(new EdmProtectedBy[] { protectedBy1, protectedBy2 });

    final var memberSpy = new MemberDouble(jpaAttribute.getJavaMember());
    memberSpy.addAnnotation(EdmProtections.class, protections);
    final Attribute<?, ?> attributeSpy = Mockito.spy(jpaAttribute);
    when(attributeSpy.getJavaMember()).thenReturn(memberSpy);

    final IntermediateProperty property = new IntermediateSimpleProperty(nameBuilder,
        attributeSpy, helper.schema);
    assertEquals("UserId", property.getProtectionClaimNames().toArray(new String[] {})[0]);
    assertNotNull(property.getProtectionPath("UserId"));

    final var actPath = property.getProtectionPath("UserId");
    assertEquals(2, actPath.size());
    assertEquals("AdministrativeInformation/Created/By", actPath.get(0));
    assertEquals("AdministrativeInformation/Updated/By", actPath.get(1));
  }

  @Test
  void checkGetComplexPropertyTwoProtectedAttributeTwoClaimName() throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = helper.getAttribute(helper.getEntityType(BusinessPartnerProtected.class),
        "administrativeInformation");

    final var protections = Mockito.mock(EdmProtections.class);
    final var protectedBy1 = Mockito.mock(EdmProtectedBy.class);
    when(protectedBy1.name()).thenReturn("UserId");
    when(protectedBy1.path()).thenReturn("created/by");

    final var protectedBy2 = Mockito.mock(EdmProtectedBy.class);
    when(protectedBy2.name()).thenReturn("Date");
    when(protectedBy2.path()).thenReturn("created/at");

    when(protections.value()).thenReturn(new EdmProtectedBy[] { protectedBy1, protectedBy2 });

    final var memberSpy = new MemberDouble(jpaAttribute.getJavaMember());
    memberSpy.addAnnotation(EdmProtections.class, protections);
    final Attribute<?, ?> attributeSpy = Mockito.spy(jpaAttribute);
    when(attributeSpy.getJavaMember()).thenReturn(memberSpy);

    final IntermediateProperty property = new IntermediateSimpleProperty(nameBuilder,
        attributeSpy, helper.schema);

    assertTrue(property.getProtectionClaimNames().contains("UserId"));
    var actPath = property.getProtectionPath("UserId");
    assertEquals(1, actPath.size());
    assertEquals("AdministrativeInformation/Created/By", actPath.get(0));

    assertTrue(property.getProtectionClaimNames().contains("Date"));
    actPath = property.getProtectionPath("Date");
    assertEquals(1, actPath.size());
    assertEquals("AdministrativeInformation/Created/At", actPath.get(0));
  }

  @Test
  void checkGetSRIDDefaultValueGeography() throws ODataJPAModelException {
    final var jpaColumn = createDummyColumn();
    final Attribute<?, ?> jpaAttribute = createDummyAttribute(jpaColumn);
    final var geospatial = mock(EdmGeospatial.class);
    addTypeBigDecimal(jpaColumn, jpaAttribute);
    when(((AnnotatedElement) jpaAttribute.getJavaMember()).getAnnotation(EdmGeospatial.class)).thenReturn(geospatial);
    when(geospatial.srid()).thenReturn("");
    when(geospatial.dimension()).thenReturn(Dimension.GEOGRAPHY);

    final var cut = new IntermediateSimpleProperty(nameBuilder, jpaAttribute, helper.schema);
    final var act = cut.getSRID();
    assertEquals("4326", act.toString());
  }

  @Test
  void checkGetSRIDDefaultValueGeometry() throws ODataJPAModelException {
    final var jpaColumn = createDummyColumn();
    final Attribute<?, ?> jpaAttribute = createDummyAttribute(jpaColumn);
    final var geospatial = mock(EdmGeospatial.class);
    addTypeBigDecimal(jpaColumn, jpaAttribute);
    when(((AnnotatedElement) jpaAttribute.getJavaMember()).getAnnotation(EdmGeospatial.class)).thenReturn(geospatial);
    when(geospatial.srid()).thenReturn("");
    when(geospatial.dimension()).thenReturn(Dimension.GEOMETRY);

    final var cut = new IntermediateSimpleProperty(nameBuilder, jpaAttribute, helper.schema);
    final var act = cut.getSRID();
    assertEquals("0", act.toString());
  }

  @Test
  void checkGetSRIDExplicitValue() throws ODataJPAModelException {
    final var jpaColumn = createDummyColumn();
    final Attribute<?, ?> jpaAttribute = createDummyAttribute(jpaColumn);
    final var geospatial = mock(EdmGeospatial.class);
    addTypeBigDecimal(jpaColumn, jpaAttribute);
    when(((AnnotatedElement) jpaAttribute.getJavaMember()).getAnnotation(EdmGeospatial.class)).thenReturn(geospatial);
    when(geospatial.srid()).thenReturn("31466");
    when(geospatial.dimension()).thenReturn(Dimension.GEOGRAPHY);

    final var cut = new IntermediateSimpleProperty(nameBuilder, jpaAttribute, helper.schema);
    final var act = cut.getSRID();
    assertEquals(Dimension.GEOGRAPHY, act.getDimension());
    assertEquals("31466", act.toString());
  }

  @Test
  void checkGetPropertyThrowsExceptionOnDateTimePrecisionGt12() throws ODataJPAModelException {
    final var jpaColumn = createDummyColumn();
    final Attribute<?, ?> jpaAttribute = createDummyAttribute(jpaColumn);

    when(jpaColumn.precision()).thenReturn(13);
    when(jpaAttribute.getJavaType()).thenAnswer(new Answer<Class<?>>() {
      @Override
      public Class<?> answer(final InvocationOnMock invocation) throws Throwable {
        return OffsetDateTime.class;
      }
    });
    final var property = new IntermediateSimpleProperty(nameBuilder,
        jpaAttribute, helper.schema);
    final var act = assertThrows(ODataJPAModelException.class, property::lazyBuildEdmItem);
    assertEquals(PROPERTY_PRECISION_NOT_IN_RANGE.getKey(), act.getId());
    assertTrue(act.getMessage().contains("13"));
  }

  @Test
  void checkGetPropertyThrowsExceptionOnDateTimePrecisionLt0() throws ODataJPAModelException {
    final var jpaColumn = createDummyColumn();
    final Attribute<?, ?> jpaAttribute = createDummyAttribute(jpaColumn);

    when(jpaColumn.precision()).thenReturn(-1);
    when(jpaAttribute.getJavaType()).thenAnswer(new Answer<Class<?>>() {
      @Override
      public Class<?> answer(final InvocationOnMock invocation) throws Throwable {
        return OffsetDateTime.class;
      }
    });
    final var property = new IntermediateSimpleProperty(nameBuilder,
        jpaAttribute, helper.schema);
    final var act = assertThrows(ODataJPAModelException.class, property::lazyBuildEdmItem);
    assertEquals(PROPERTY_PRECISION_NOT_IN_RANGE.getKey(), act.getId());
    assertTrue(act.getMessage().contains("-1"));
  }

  @Test
  void checkGetPropertyPrecisionNullOnDecimalPrecision0() throws ODataJPAModelException {
    final var jpaColumn = createDummyColumn();
    final Attribute<?, ?> jpaAttribute = createDummyAttribute(jpaColumn);
    addTypeBigDecimal(jpaColumn, jpaAttribute);
    final var property = new IntermediateSimpleProperty(nameBuilder,
        jpaAttribute, helper.schema);
    final var act = property.getEdmItem();
    assertNull(act.getPrecision());
  }

  @Test
  void checkGetEdmType() throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = helper.getAttribute(helper.getEntityType(BusinessPartnerProtected.class),
        "eTag");
    final IntermediateProperty property = new IntermediateSimpleProperty(nameBuilder, jpaAttribute, helper.schema);
    assertEquals(EdmPrimitiveTypeKind.Int64, property.getEdmType());
  }

  @Test
  void checkIsSearchableTrue() throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = helper.getAttribute(helper.getEntityType(
        AdministrativeDivisionDescription.class), "name");
    final IntermediateProperty property = new IntermediateSimpleProperty(nameBuilder, jpaAttribute, helper.schema);
    assertTrue(property.isSearchable());
  }

  @Test
  void checkIsSearchableFalse() throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = helper.getAttribute(helper.getEntityType(
        AdministrativeDivision.class), "codePublisher");
    final IntermediateProperty property = new IntermediateSimpleProperty(nameBuilder, jpaAttribute, helper.schema);
    assertFalse(property.isSearchable());
  }

  @Test
  void checkGetAnnotationReturnsExistingAnnotation() throws ODataJPAModelException {
    createAnnotation();
    final Attribute<?, ?> jpaAttribute = helper.getAttribute(helper.getEntityType(
        AnnotationsParent.class), "area");
    final IntermediateProperty property = new IntermediateSimpleProperty(nameBuilder, jpaAttribute, helper.schema);
    final var act = property.getAnnotation("Core", "ComputedDefaultValue");
    assertNotNull(act);
  }

  @Test
  void checkGetAnnotationReturnsNullAliasUnknown() throws ODataJPAModelException {
    createAnnotation();
    final Attribute<?, ?> jpaAttribute = helper.getAttribute(helper.getEntityType(
        AnnotationsParent.class), "area");
    final IntermediateProperty property = new IntermediateSimpleProperty(nameBuilder, jpaAttribute, helper.schema);
    assertNull(property.getAnnotation("Capability", "ComputedDefaultValue"));
  }

  @Test
  void checkGetAnnotationValueSimpleProperty() throws ODataJPAModelException {
    createAnnotation();
    final Attribute<?, ?> jpaAttribute = helper.getAttribute(helper.getEntityType(
        AnnotationsParent.class), "area");
    final IntermediateProperty property = new IntermediateSimpleProperty(nameBuilder, jpaAttribute, helper.schema);
    final var act = property.getAnnotationValue(Aliases.CORE, Terms.COMPUTED_DEFAULT_VALUE, GeneralProperty.VALUE,
        Boolean.class);
    assertNotNull(act);
    assertTrue(act);
  }

  @Test
  void checkGetAnnotationValueReturnsNullAliasUnknown() throws ODataJPAModelException {
    createAnnotation();
    final Attribute<?, ?> jpaAttribute = helper.getAttribute(helper.getEntityType(
        AnnotationsParent.class), "area");
    final IntermediateProperty property = new IntermediateSimpleProperty(nameBuilder, jpaAttribute, helper.schema);
    assertNull(property.getAnnotationValue(Aliases.CAPABILITIES.alias(), Terms.COMPUTED_DEFAULT_VALUE.term(),
        GeneralProperty.VALUE.property()));
  }

  @Test
  void checkJavaAnnotationsOneAnnotation() throws ODataJPAModelException {
    createAnnotation();
    final Attribute<?, ?> jpaAttribute = helper.getAttribute(helper.getEntityType(
        AnnotationsParent.class), "area");
    final IntermediateProperty property = new IntermediateSimpleProperty(nameBuilder, jpaAttribute, helper.schema);
    final var act = property.javaAnnotations(Column.class.getPackage().getName());
    assertEquals(1, act.size());
    assertNotNull(act.get("Column"));
  }

  @Test
  void checkJavaAnnotationsNoAnnotations() throws ODataJPAModelException {
    createAnnotation();
    final Attribute<?, ?> jpaAttribute = helper.getAttribute(helper.getEntityType(
        AnnotationsParent.class), "area");
    final IntermediateProperty property = new IntermediateSimpleProperty(nameBuilder, jpaAttribute, helper.schema);
    final var act = property.javaAnnotations(Test.class.getPackage().getName());
    assertTrue(act.isEmpty());
  }

  @Test
  void checkAsUserGroupRestrictedUser() throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = helper.getAttribute(helper.getEntityType(
        RestrictedEntityUnrestrictedSource.class), "relation");
    final var property = new IntermediateSimpleProperty(new JPADefaultEdmNameBuilder(PUNIT_NAME),
        jpaAttribute, helper.schema);

    final IntermediateSimpleProperty act = property.asUserGroupRestricted(List.of("Company"));
    assertNotEquals(property, act);
    assertTrue(((IntermediateStructuredType<RestrictedEntityComplex>) act.getStructuredType()).isRestricted());
  }

  private void createAnnotation() {
    final var reference = helper.annotationInfo.getReferences();
    final var annotationProvider = new JavaBasedCoreAnnotationsProvider();

    final var typeDefinition = mock(CsdlTypeDefinition.class);
    when(typeDefinition.getName()).thenReturn("Tag");
    when(typeDefinition.getUnderlyingType()).thenReturn("Edm.Boolean");
    final var terms = AnnotationTestHelper.addTermToCoreReferences(reference, "ComputedDefaultValue", "Tag",
        typeDefinition);

    when(reference.convertAlias("Core")).thenReturn("Org.OData.Core.V1");
    when(reference.getTerms("Core", Applicability.PROPERTY))
        .thenReturn(Arrays.asList(terms));

    helper.annotationInfo.getAnnotationProvider().add(annotationProvider);
  }

  private Attribute<?, ?> createDummyAttribute(final Column jpaColumn) {
    final Attribute<?, ?> jpaAttribute = mock(Attribute.class);
    final ManagedType<?> managedType = mock(ManagedType.class);
    final var javaMember = mock(Member.class, withSettings().extraInterfaces(AnnotatedElement.class));
    when(managedType.getJavaType()).thenAnswer(new Answer<Class<?>>() {
      @Override
      public Class<?> answer(final InvocationOnMock invocation) throws Throwable {
        return Person.class;
      }
    });
    when(jpaAttribute.getName()).thenReturn("dummy");
    when(jpaAttribute.getDeclaringType()).thenAnswer(new Answer<ManagedType<?>>() {
      @Override
      public ManagedType<?> answer(final InvocationOnMock invocation) throws Throwable {
        return managedType;
      }
    });
    when(jpaAttribute.getJavaMember()).thenReturn(javaMember);
    when(((AnnotatedElement) jpaAttribute.getJavaMember()).getAnnotation(Column.class)).thenReturn(jpaColumn);
    when(jpaAttribute.getPersistentAttributeType()).thenReturn(PersistentAttributeType.BASIC);
    return jpaAttribute;
  }

  private Column createDummyColumn() {
    final var jpaColumn = mock(Column.class);
    when(jpaColumn.name()).thenReturn("DUMMY");
    when(jpaColumn.nullable()).thenReturn(true);
    return jpaColumn;
  }

  private static class PostProcessorSetName implements JPAEdmMetadataPostProcessor {

    @Override
    public void processProperty(final IntermediatePropertyAccess property, final String jpaManagedTypeClassName) {
      if (jpaManagedTypeClassName.equals(
          "com.sap.olingo.jpa.processor.core.testmodel.BusinessPartner") && property.getInternalName().equals(
              "customString1")) {
        final var annotation = new CsdlAnnotation();
        annotation.setTerm("Immutable");
        annotation.setExpression(new CsdlConstantExpression(ConstantExpressionType.Bool, "true"));
        property.addAnnotations(Collections.singletonList(annotation));
      }
    }

    @Override
    public void processNavigationProperty(final IntermediateNavigationPropertyAccess property,
        final String jpaManagedTypeClassName) { /* Not needed */ }

    @Override
    public void provideReferences(final IntermediateReferenceList references) throws ODataJPAModelException {
      /* Not needed */ }

    @Override
    public void processEntityType(final IntermediateEntityTypeAccess entity) {/* Not needed */ }
  }

  private static class LocalJPAEdmNameBuilder implements JPAEdmNameBuilder {

    private final JPAEdmNameBuilder builder = new JPADefaultEdmNameBuilder(PUNIT_NAME);

    @Override
    public String buildComplexTypeName(final EmbeddableType<?> jpaEmbeddedType) {
      return builder.buildComplexTypeName(jpaEmbeddedType);
    }

    @Override
    public String buildContainerName() {
      return builder.buildContainerName();
    }

    @Override
    public String buildEntitySetName(final String entityTypeName) {
      return builder.buildEntitySetName(entityTypeName);
    }

    @Override
    public String buildEntityTypeName(final EntityType<?> jpaEntityType) {
      return builder.buildEntityTypeName(jpaEntityType);
    }

    @Override
    public String buildEnumerationTypeName(final Class<? extends Enum<?>> javaEnum) {
      return builder.buildEnumerationTypeName(javaEnum);
    }

    @Override
    public String buildNaviPropertyName(final Attribute<?, ?> jpaAttribute) {
      return builder.buildNaviPropertyName(jpaAttribute);
    }

    @Override
    public String buildOperationName(final String internalOperationName) {
      return builder.buildOperationName(internalOperationName);
    }

    @Override
    public String buildPropertyName(final String jpaAttributeName) {
      return builder.buildPropertyName(jpaAttributeName);
    }

    @Override
    public String getNamespace() {
      return builder.getNamespace();
    }

    @Override
    public String buildColumnName(final String jpaFieldName) {

      int start = 0;
      final List<String> splits = new ArrayList<>();
      final var chars = jpaFieldName.toCharArray();
      for (int i = 0; i < chars.length; i++) {
        if (Character.isUpperCase(chars[i])) {
          splits.add(String.copyValueOf(chars, start, i - start));
          start = i;
        }
      }
      if (start < chars.length)
        splits.add(String.copyValueOf(chars, start, chars.length - start));

      return splits.stream()
          .map(String::toUpperCase)
          .collect(Collectors.joining("_"));

    }

  }

  private void addTypeBigDecimal(final Column jpaColumn, final Attribute<?, ?> jpaAttribute) {
    when(jpaColumn.precision()).thenReturn(-1);
    when(jpaAttribute.getJavaType()).thenAnswer(new Answer<Class<?>>() {
      @Override
      public Class<?> answer(final InvocationOnMock invocation) throws Throwable {
        return BigDecimal.class;
      }
    });
  }

}
