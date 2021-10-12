package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Member;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.Attribute.PersistentAttributeType;
import javax.persistence.metamodel.EmbeddableType;
import javax.persistence.metamodel.ManagedType;

import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.sap.olingo.jpa.metadata.api.JPAEdmMetadataPostProcessor;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmProtectedBy;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmProtections;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extention.IntermediateEntityTypeAccess;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extention.IntermediateNavigationPropertyAccess;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extention.IntermediatePropertyAccess;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extention.IntermediateReferenceList;
import com.sap.olingo.jpa.metadata.core.edm.mapper.util.MemberDouble;
import com.sap.olingo.jpa.processor.core.testmodel.AdministrativeDivision;
import com.sap.olingo.jpa.processor.core.testmodel.BusinessPartner;
import com.sap.olingo.jpa.processor.core.testmodel.BusinessPartnerProtected;
import com.sap.olingo.jpa.processor.core.testmodel.Comment;
import com.sap.olingo.jpa.processor.core.testmodel.DummyToBeIgnored;
import com.sap.olingo.jpa.processor.core.testmodel.Organization;
import com.sap.olingo.jpa.processor.core.testmodel.Person;
import com.sap.olingo.jpa.processor.core.testmodel.PersonImage;

public class TestIntermediateSimpleProperty extends TestMappingRoot {
  private TestHelper helper;
  private JPAEdmMetadataPostProcessor processor;

  @BeforeEach
  public void setup() throws ODataJPAModelException {
    helper = new TestHelper(emf.getMetamodel(), PUNIT_NAME);
    processor = mock(JPAEdmMetadataPostProcessor.class);
  }

  @Test
  public void checkPropertyCanBeCreated() throws ODataJPAModelException {
    final EmbeddableType<?> et = helper.getEmbeddedableType("CommunicationData");
    final Attribute<?, ?> jpaAttribute = helper.getAttribute(et, "landlinePhoneNumber");
    assertNotNull(new IntermediateSimpleProperty(new JPADefaultEdmNameBuilder(PUNIT_NAME), jpaAttribute,
        helper.schema));
  }

  @Test
  public void checkGetPropertyName() throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = helper.getAttribute(helper.getEntityType(BusinessPartner.class), "type");
    final IntermediateSimpleProperty property = new IntermediateSimpleProperty(new JPADefaultEdmNameBuilder(PUNIT_NAME),
        jpaAttribute,
        helper.schema);
    assertEquals("Type", property.getEdmItem().getName(), "Wrong name");
  }

  @Test
  public void checkGetPropertyDBFieldName() throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = helper.getAttribute(helper.getEntityType(BusinessPartner.class), "type");
    final IntermediateSimpleProperty property = new IntermediateSimpleProperty(new JPADefaultEdmNameBuilder(PUNIT_NAME),
        jpaAttribute,
        helper.schema);
    assertEquals("\"Type\"", property.getDBFieldName(), "Wrong name");
  }

  @Test
  public void checkGetPropertySimpleType() throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = helper.getAttribute(helper.getEntityType(BusinessPartner.class), "type");
    final IntermediateSimpleProperty property = new IntermediateSimpleProperty(new JPADefaultEdmNameBuilder(PUNIT_NAME),
        jpaAttribute, helper.schema);
    assertEquals(EdmPrimitiveTypeKind.String.getFullQualifiedName().getFullQualifiedNameAsString(),
        property.getEdmItem().getType(), "Wrong type");
  }

  @Test
  public void checkGetPropertyComplexType() throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = helper.getAttribute(helper.getEntityType(BusinessPartner.class),
        "communicationData");
    final IntermediateSimpleProperty property = new IntermediateSimpleProperty(new JPADefaultEdmNameBuilder(PUNIT_NAME),
        jpaAttribute,
        helper.schema);
    assertEquals(PUNIT_NAME + ".CommunicationData", property.getEdmItem().getType(), "Wrong type");
  }

  @Test
  public void checkGetPropertyEnumTypeWithoutConverter() throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = helper.getAttribute(helper.getEntityType(Organization.class), "aBCClass");
    final IntermediateSimpleProperty property = new IntermediateSimpleProperty(new JPADefaultEdmNameBuilder(PUNIT_NAME),
        jpaAttribute,
        helper.schema);
    assertEquals("com.sap.olingo.jpa.ABCClassifiaction", property.getEdmItem().getType(), "Wrong type");
  }

  @Test
  public void checkGetPropertyEnumTypeWithoutConverterMustNotHaveMapper() throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = helper.getAttribute(helper.getEntityType(Organization.class), "aBCClass");
    final IntermediateSimpleProperty property = new IntermediateSimpleProperty(new JPADefaultEdmNameBuilder(PUNIT_NAME),
        jpaAttribute,
        helper.schema);
    assertNull(property.getEdmItem().getMapping());
  }

  @Test
  public void checkGetPropertyEnumTypeWithConverter() throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = helper.getAttribute(helper.getEntityType(Person.class), "accessRights");
    final IntermediateSimpleProperty property = new IntermediateSimpleProperty(new JPADefaultEdmNameBuilder(PUNIT_NAME),
        jpaAttribute,
        helper.schema);
    assertEquals("com.sap.olingo.jpa.AccessRights", property.getEdmItem().getType(), "Wrong type");
  }

  @Test
  public void checkGetPropertyIgnoreFalse() throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = helper.getAttribute(helper.getEntityType(BusinessPartner.class), "type");
    final IntermediatePropertyAccess property = new IntermediateSimpleProperty(new JPADefaultEdmNameBuilder(PUNIT_NAME),
        jpaAttribute,
        helper.schema);
    assertFalse(property.ignore());
  }

  @Test
  public void checkGetPropertyIgnoreTrue() throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = helper.getAttribute(helper.getEntityType(BusinessPartner.class),
        "customString1");
    final IntermediatePropertyAccess property = new IntermediateSimpleProperty(new JPADefaultEdmNameBuilder(PUNIT_NAME),
        jpaAttribute,
        helper.schema);
    assertTrue(property.ignore());
  }

  @Test
  public void checkGetPropertyFacetsNullableTrue() throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = helper.getAttribute(helper.getEntityType(BusinessPartner.class),
        "customString1");
    final IntermediateSimpleProperty property = new IntermediateSimpleProperty(new JPADefaultEdmNameBuilder(PUNIT_NAME),
        jpaAttribute,
        helper.schema);
    assertTrue(property.getEdmItem().isNullable());
  }

  @Test
  public void checkGetPropertyFacetsNullableTrueComplex() throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = helper.getAttribute(helper.getEmbeddedableType("PostalAddressData"), "pOBox");
    final IntermediateSimpleProperty property = new IntermediateSimpleProperty(new JPADefaultEdmNameBuilder(PUNIT_NAME),
        jpaAttribute, helper.schema);
    assertTrue(property.getEdmItem().isNullable());
  }

  @Test
  public void checkGetPropertyFacetsNullableFalse() throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = helper.getAttribute(helper.getEntityType(BusinessPartner.class), "eTag");
    final IntermediateSimpleProperty property = new IntermediateSimpleProperty(new JPADefaultEdmNameBuilder(PUNIT_NAME),
        jpaAttribute,
        helper.schema);
    assertFalse(property.getEdmItem().isNullable());
  }

  @Test
  public void checkGetPropertyIsETagTrue() throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = helper.getAttribute(helper.getEntityType(BusinessPartner.class), "eTag");
    final IntermediateSimpleProperty property = new IntermediateSimpleProperty(new JPADefaultEdmNameBuilder(PUNIT_NAME),
        jpaAttribute,
        helper.schema);
    assertTrue(property.isEtag());
  }

  @Test
  public void checkGetPropertyIsETagFalse() throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = helper.getAttribute(helper.getEntityType(BusinessPartner.class), "type");
    final IntermediateSimpleProperty property = new IntermediateSimpleProperty(new JPADefaultEdmNameBuilder(PUNIT_NAME),
        jpaAttribute,
        helper.schema);
    assertFalse(property.isEtag());
  }

  @Test
  public void checkGetPropertyMaxLength() throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = helper.getAttribute(helper.getEntityType(BusinessPartner.class), "type");
    final IntermediateSimpleProperty property = new IntermediateSimpleProperty(new JPADefaultEdmNameBuilder(PUNIT_NAME),
        jpaAttribute,
        helper.schema);
    assertEquals(new Integer(1), property.getEdmItem().getMaxLength());
  }

  @Test
  public void checkGetPropertyMaxLengthNullForClob() throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = helper.getAttribute(helper.getComplexType("DummyEmbeddedToIgnore"), "command");
    final IntermediateSimpleProperty property = new IntermediateSimpleProperty(new JPADefaultEdmNameBuilder(PUNIT_NAME),
        jpaAttribute,
        helper.schema);
    assertNull(property.getEdmItem().getMaxLength());
  }

  @Test
  public void checkGetPropertyPrecisionDecimal() throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = helper.getAttribute(helper.getEntityType(BusinessPartner.class), "customNum1");
    final IntermediateSimpleProperty property = new IntermediateSimpleProperty(new JPADefaultEdmNameBuilder(PUNIT_NAME),
        jpaAttribute,
        helper.schema);
    assertEquals(new Integer(16), property.getEdmItem().getPrecision());
  }

  @Test
  public void checkGetPropertyScaleDecimal() throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = helper.getAttribute(helper.getEntityType(BusinessPartner.class), "customNum1");
    final IntermediateSimpleProperty property = new IntermediateSimpleProperty(new JPADefaultEdmNameBuilder(PUNIT_NAME),
        jpaAttribute,
        helper.schema);
    assertEquals(new Integer(5), property.getEdmItem().getScale());
  }

  @Test
  public void checkGetPropertyPrecisionTime() throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = helper.getAttribute(helper.getEntityType(BusinessPartner.class),
        "creationDateTime");
    final IntermediateSimpleProperty property = new IntermediateSimpleProperty(new JPADefaultEdmNameBuilder(PUNIT_NAME),
        jpaAttribute,
        helper.schema);
    assertEquals(new Integer(3), property.getEdmItem().getPrecision());
  }

  @Test
  public void checkGetPropertyMapper() throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = helper.getAttribute(helper.getEntityType(BusinessPartner.class),
        "creationDateTime");
    final IntermediateSimpleProperty property = new IntermediateSimpleProperty(new JPADefaultEdmNameBuilder(PUNIT_NAME),
        jpaAttribute,
        helper.schema);
    assertNotNull(property.getEdmItem().getMapping());
    assertEquals(Timestamp.class, property.getEdmItem().getMapping().getMappedJavaClass());
  }

  @Test
  public void checkGetPropertyMapperWithConverter() throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = helper.getAttribute(helper.getEntityType(Person.class), "birthDay");
    final IntermediateSimpleProperty property = new IntermediateSimpleProperty(new JPADefaultEdmNameBuilder(PUNIT_NAME),
        jpaAttribute,
        helper.schema);
    assertNotNull(property.getEdmItem().getMapping());
    assertEquals(Date.class, property.getEdmItem().getMapping().getMappedJavaClass());
  }

  @Test
  public void checkGetNoPropertyMapperForClob() throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = helper.getAttribute(helper.getEntityType(Comment.class), "text");
    final IntermediateSimpleProperty property = new IntermediateSimpleProperty(new JPADefaultEdmNameBuilder(PUNIT_NAME),
        jpaAttribute,
        helper.schema);
    assertNull(property.getEdmItem().getMapping());
  }

  @Test
  public void checkPostProcessorCalled() throws ODataJPAModelException {
    IntermediateSimpleProperty.setPostProcessor(processor);
    final Attribute<?, ?> jpaAttribute = helper.getAttribute(helper.getEntityType(BusinessPartner.class),
        "creationDateTime");
    final IntermediateSimpleProperty property = new IntermediateSimpleProperty(new JPADefaultEdmNameBuilder(PUNIT_NAME),
        jpaAttribute,
        helper.schema);

    property.getEdmItem();
    verify(processor, atLeastOnce()).processProperty(property, BUPA_CANONICAL_NAME);
  }

  @Test
  public void checkPostProcessorNameChanged() throws ODataJPAModelException {
    final PostProcessorSetName pPDouble = new PostProcessorSetName();
    IntermediateSimpleProperty.setPostProcessor(pPDouble);

    final Attribute<?, ?> jpaAttribute = helper.getAttribute(helper.getEntityType(BusinessPartner.class),
        "customString1");
    final IntermediateSimpleProperty property = new IntermediateSimpleProperty(new JPADefaultEdmNameBuilder(PUNIT_NAME),
        jpaAttribute,
        helper.schema);

    assertEquals("ContactPersonName", property.getEdmItem().getName(), "Wrong name");
  }

  @Test
  public void checkPostProcessorExternalNameChanged() throws ODataJPAModelException {
    final PostProcessorSetName pPDouble = new PostProcessorSetName();
    IntermediateModelElement.setPostProcessor(pPDouble);

    final Attribute<?, ?> jpaAttribute = helper.getAttribute(helper.getEntityType(BusinessPartner.class),
        "customString1");
    final IntermediatePropertyAccess property = new IntermediateSimpleProperty(new JPADefaultEdmNameBuilder(PUNIT_NAME),
        jpaAttribute,
        helper.schema);

    assertEquals("ContactPersonName", property.getExternalName(), "Wrong name");
  }

  @Test
  public void checkConverterGetConverterReturned() throws ODataJPAModelException {
    final PostProcessorSetName pPDouble = new PostProcessorSetName();
    IntermediateModelElement.setPostProcessor(pPDouble);

    final Attribute<?, ?> jpaAttribute = helper.getAttribute(helper.getEntityType(BusinessPartner.class),
        "creationDateTime");
    final IntermediateSimpleProperty property = new IntermediateSimpleProperty(new JPADefaultEdmNameBuilder(PUNIT_NAME),
        jpaAttribute, helper.schema);

    assertNotNull(property.getConverter());
  }

  @Test
  public void checkConverterGetConverterNotReturned() throws ODataJPAModelException {
    final PostProcessorSetName pPDouble = new PostProcessorSetName();
    IntermediateModelElement.setPostProcessor(pPDouble);

    final Attribute<?, ?> jpaAttribute = helper.getAttribute(helper.getEntityType(Person.class), "customString1");
    final IntermediateSimpleProperty property = new IntermediateSimpleProperty(new JPADefaultEdmNameBuilder(PUNIT_NAME),
        jpaAttribute,
        helper.schema);

    assertNull(property.getConverter());
  }

  @Test
  public void checkConverterGetConverterNotReturnedDiffernt() throws ODataJPAModelException {
    final PostProcessorSetName pPDouble = new PostProcessorSetName();
    IntermediateModelElement.setPostProcessor(pPDouble);

    final Attribute<?, ?> jpaAttribute = helper.getAttribute(helper.getEntityType(DummyToBeIgnored.class), "uuid");
    final IntermediateSimpleProperty property = new IntermediateSimpleProperty(new JPADefaultEdmNameBuilder(PUNIT_NAME),
        jpaAttribute,
        helper.schema);

    assertNull(property.getConverter());
  }

  @Test
  public void checkGetPropertyDefaultValue() throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = helper.getAttribute(helper.getEmbeddedableType("PostalAddressData"),
        "regionCodePublisher");
    final IntermediateSimpleProperty property = new IntermediateSimpleProperty(new JPADefaultEdmNameBuilder(PUNIT_NAME),
        jpaAttribute,
        helper.schema);
    assertEquals("ISO", property.getEdmItem().getDefaultValue());
  }

  @Test
  public void checkGetPropertyIsStream() throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = helper.getAttribute(helper.getEntityType(PersonImage.class),
        "image");
    final IntermediateSimpleProperty property = new IntermediateSimpleProperty(new JPADefaultEdmNameBuilder(PUNIT_NAME),
        jpaAttribute,
        helper.schema);
    assertTrue(property.isStream());
  }

  @Test
  public void checkGetTypeBoxedForPrimitive() throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = helper.getAttribute(helper.getEntityType(AdministrativeDivision.class),
        "population");
    final IntermediateSimpleProperty property = new IntermediateSimpleProperty(new JPADefaultEdmNameBuilder(PUNIT_NAME),
        jpaAttribute,
        helper.schema);
    assertEquals(Long.class, property.getType());
  }

  @Test
  public void checkGetTypeBoxed() throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = helper.getAttribute(helper.getEntityType(AdministrativeDivision.class),
        "area");
    final IntermediateSimpleProperty property = new IntermediateSimpleProperty(new JPADefaultEdmNameBuilder(PUNIT_NAME),
        jpaAttribute,
        helper.schema);
    assertEquals(Integer.class, property.getType());
  }

  @Test
  public void checkTimestampWithoutPrecisionReturns0() throws ODataJPAModelException {
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

    final Column column = mock(Column.class);
    final AnnotatedElement annotations = mock(AnnotatedElement.class, withSettings().extraInterfaces(Member.class));
    when(annotations.getAnnotation(Column.class)).thenReturn(column);
    when(jpaAttribute.getJavaMember()).thenReturn((Member) annotations);
    when(column.name()).thenReturn("Test");

    final IntermediateSimpleProperty property = new IntermediateSimpleProperty(new JPADefaultEdmNameBuilder(PUNIT_NAME),
        jpaAttribute, helper.schema);

    assertEquals(0, property.getEdmItem().getPrecision());
  }

  @Test
  public void checkGetPropertyHasProtectionFalse() throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = helper.getAttribute(helper.getEntityType(BusinessPartnerProtected.class),
        "eTag");
    final IntermediatePropertyAccess property = new IntermediateSimpleProperty(new JPADefaultEdmNameBuilder(PUNIT_NAME),
        jpaAttribute, helper.schema);
    assertFalse(property.hasProtection());
  }

  @Test
  public void checkGetPropertyHasProtectionTrue() throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = helper.getAttribute(helper.getEntityType(BusinessPartnerProtected.class),
        "username");
    final IntermediatePropertyAccess property = new IntermediateSimpleProperty(new JPADefaultEdmNameBuilder(PUNIT_NAME),
        jpaAttribute, helper.schema);
    assertTrue(property.hasProtection());
  }

  @Test
  public void checkGetPropertyProtectionSupportsWildCardTrue() throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = helper.getAttribute(helper.getEntityType(BusinessPartnerProtected.class),
        "username");
    final IntermediateProperty property = new IntermediateSimpleProperty(new JPADefaultEdmNameBuilder(PUNIT_NAME),
        jpaAttribute, helper.schema);
    assertTrue(property.protectionWithWildcard("UserId", String.class));
  }

  //
  @Test
  public void checkGetPropertyProtectionSupportsWildCardFalse() throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = helper.getAttribute(helper.getComplexType(
        "InhouseAddressWithThreeProtections"),
        "building");
    final IntermediateProperty property = new IntermediateSimpleProperty(new JPADefaultEdmNameBuilder(PUNIT_NAME),
        jpaAttribute, helper.schema);
    assertFalse(property.protectionWithWildcard("BuildingNumber", String.class));
  }

  @Test
  public void checkGetPropertyProtectionSupportsWildCardFalseNonString() throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = helper.getAttribute(helper.getComplexType(
        "InhouseAddressWithThreeProtections"),
        "roomNumber");
    final IntermediateProperty property = new IntermediateSimpleProperty(new JPADefaultEdmNameBuilder(PUNIT_NAME),
        jpaAttribute, helper.schema);
    assertFalse(property.protectionWithWildcard("RoomNumber", Integer.class));
  }

  @Test
  public void checkGetPropertyProtectedAttributeClaimName() throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = helper.getAttribute(helper.getEntityType(BusinessPartnerProtected.class),
        "username");
    final IntermediateProperty property = new IntermediateSimpleProperty(new JPADefaultEdmNameBuilder(PUNIT_NAME),
        jpaAttribute, helper.schema);
    assertEquals("UserId", property.getProtectionClaimNames().toArray(new String[] {})[0]);
    assertNotNull(property.getProtectionPath("UserId"));
    final List<String> actPath = property.getProtectionPath("UserId");
    assertEquals(1, actPath.size());
    assertEquals("Username", actPath.get(0));
  }

  @Test
  public void checkGetPropertyNotProtectedAttributeClaimName() throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = helper.getAttribute(helper.getEntityType(BusinessPartnerProtected.class),
        "eTag");
    final IntermediateProperty property = new IntermediateSimpleProperty(new JPADefaultEdmNameBuilder(PUNIT_NAME),
        jpaAttribute, helper.schema);
    assertTrue(property.getProtectionClaimNames().isEmpty());
    assertTrue(property.getProtectionPath("Username").isEmpty());
  }

  @Test
  public void checkGetComplexPropertyProtectedAttributeClaimName() throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = helper.getAttribute(helper.getEntityType(BusinessPartnerProtected.class),
        "administrativeInformation");

    final EdmProtectedBy annotation = Mockito.mock(EdmProtectedBy.class);
    when(annotation.name()).thenReturn("UserId");
    when(annotation.path()).thenReturn("created/by");

    final MemberDouble memberSpy = new MemberDouble(jpaAttribute.getJavaMember());
    memberSpy.addAnnotation(EdmProtectedBy.class, annotation);
    final Attribute<?, ?> attributeSpy = Mockito.spy(jpaAttribute);
    when(attributeSpy.getJavaMember()).thenReturn(memberSpy);

    final IntermediateProperty property = new IntermediateSimpleProperty(new JPADefaultEdmNameBuilder(PUNIT_NAME),
        attributeSpy, helper.schema);
    assertEquals("UserId", property.getProtectionClaimNames().toArray(new String[] {})[0]);
    assertNotNull(property.getProtectionPath("UserId"));
    final List<String> actPath = property.getProtectionPath("UserId");
    assertEquals(1, actPath.size());
    assertEquals("AdministrativeInformation/Created/By", actPath.get(0));
  }

  @Test
  public void checkGetComplexPropertyTwoProtectedAttributeClaimName() throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = helper.getAttribute(helper.getEntityType(BusinessPartnerProtected.class),
        "administrativeInformation");

    final EdmProtections protections = Mockito.mock(EdmProtections.class);
    final EdmProtectedBy protectedBy1 = Mockito.mock(EdmProtectedBy.class);
    when(protectedBy1.name()).thenReturn("UserId");
    when(protectedBy1.path()).thenReturn("created/by");

    final EdmProtectedBy protectedBy2 = Mockito.mock(EdmProtectedBy.class);
    when(protectedBy2.name()).thenReturn("UserId");
    when(protectedBy2.path()).thenReturn("updated/by");

    when(protections.value()).thenReturn(new EdmProtectedBy[] { protectedBy1, protectedBy2 });

    final MemberDouble memberSpy = new MemberDouble(jpaAttribute.getJavaMember());
    memberSpy.addAnnotation(EdmProtections.class, protections);
    final Attribute<?, ?> attributeSpy = Mockito.spy(jpaAttribute);
    when(attributeSpy.getJavaMember()).thenReturn(memberSpy);

    final IntermediateProperty property = new IntermediateSimpleProperty(new JPADefaultEdmNameBuilder(PUNIT_NAME),
        attributeSpy, helper.schema);
    assertEquals("UserId", property.getProtectionClaimNames().toArray(new String[] {})[0]);
    assertNotNull(property.getProtectionPath("UserId"));

    final List<String> actPath = property.getProtectionPath("UserId");
    assertEquals(2, actPath.size());
    assertEquals("AdministrativeInformation/Created/By", actPath.get(0));
    assertEquals("AdministrativeInformation/Updated/By", actPath.get(1));
  }

  @Test
  public void checkGetComplexPropertyTwoProtectedAttributeTwoClaimName() throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = helper.getAttribute(helper.getEntityType(BusinessPartnerProtected.class),
        "administrativeInformation");

    final EdmProtections protections = Mockito.mock(EdmProtections.class);
    final EdmProtectedBy protectedBy1 = Mockito.mock(EdmProtectedBy.class);
    when(protectedBy1.name()).thenReturn("UserId");
    when(protectedBy1.path()).thenReturn("created/by");

    final EdmProtectedBy protectedBy2 = Mockito.mock(EdmProtectedBy.class);
    when(protectedBy2.name()).thenReturn("Date");
    when(protectedBy2.path()).thenReturn("created/at");

    when(protections.value()).thenReturn(new EdmProtectedBy[] { protectedBy1, protectedBy2 });

    final MemberDouble memberSpy = new MemberDouble(jpaAttribute.getJavaMember());
    memberSpy.addAnnotation(EdmProtections.class, protections);
    final Attribute<?, ?> attributeSpy = Mockito.spy(jpaAttribute);
    when(attributeSpy.getJavaMember()).thenReturn(memberSpy);

    final IntermediateProperty property = new IntermediateSimpleProperty(new JPADefaultEdmNameBuilder(PUNIT_NAME),
        attributeSpy, helper.schema);

    assertTrue(property.getProtectionClaimNames().contains("UserId"));
    List<String> actPath = property.getProtectionPath("UserId");
    assertEquals(1, actPath.size());
    assertEquals("AdministrativeInformation/Created/By", actPath.get(0));

    assertTrue(property.getProtectionClaimNames().contains("Date"));
    actPath = property.getProtectionPath("Date");
    assertEquals(1, actPath.size());
    assertEquals("AdministrativeInformation/Created/At", actPath.get(0));
  }

  @Disabled
  @Test
  public void checkGetSRID() {
    // Test for spatial data missing
  }

  private class PostProcessorSetName extends JPAEdmMetadataPostProcessor {

    @Override
    public void processProperty(final IntermediatePropertyAccess property, final String jpaManagedTypeClassName) {
      if (jpaManagedTypeClassName.equals(
          "com.sap.olingo.jpa.processor.core.testmodel.BusinessPartner")) {
        if (property.getInternalName().equals("customString1")) {
          property.setExternalName("ContactPersonName");
        }
      }
    }

    @Override
    public void processNavigationProperty(final IntermediateNavigationPropertyAccess property,
        final String jpaManagedTypeClassName) {}

    @Override
    public void provideReferences(final IntermediateReferenceList references) throws ODataJPAModelException {}

    @Override
    public void processEntityType(final IntermediateEntityTypeAccess entity) {}
  }
}
