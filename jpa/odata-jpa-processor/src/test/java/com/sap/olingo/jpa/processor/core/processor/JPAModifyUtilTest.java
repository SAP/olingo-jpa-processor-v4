package com.sap.olingo.jpa.processor.core.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.olingo.commons.api.ex.ODataException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAInvocationTargetException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessorException;
import com.sap.olingo.jpa.processor.core.testmodel.AdministrativeDivisionKey;
import com.sap.olingo.jpa.processor.core.testmodel.BusinessPartner;
import com.sap.olingo.jpa.processor.core.testmodel.BusinessPartnerRole;
import com.sap.olingo.jpa.processor.core.testmodel.Country;
import com.sap.olingo.jpa.processor.core.testmodel.Organization;
import com.sap.olingo.jpa.processor.core.testmodel.Person;
import com.sap.olingo.jpa.processor.core.testmodel.PostalAddressData;
import com.sap.olingo.jpa.processor.core.testobjects.BusinessPartnerRoleWithoutSetter;
import com.sap.olingo.jpa.processor.core.testobjects.OrganizationWithoutGetter;
import com.sap.olingo.jpa.processor.core.util.TestBase;
import com.sap.olingo.jpa.processor.core.util.TestHelper;

class JPAModifyUtilTest extends TestBase {
  private JPAModifyUtil cut;
  private Map<String, Object> jpaAttributes;
  private BusinessPartner partner;
  private JPAEntityType org;

  @BeforeEach
  void setUp() throws ODataException {
    cut = new JPAModifyUtil();
    jpaAttributes = new HashMap<>();
    partner = new Organization();
    helper = new TestHelper(emf, PUNIT_NAME);
    org = helper.getJPAEntityType(Organization.class);
  }

  @Test
  void testBuildMethodNameSuffix() throws ODataJPAModelException {
    assertEquals("CreationDateTime", cut.buildMethodNameSuffix(org.getAttribute("creationDateTime").get()));
  }

  @Test
  void testSetterName() throws ODataJPAModelException {
    assertEquals("setCreationDateTime", cut.buildSetterName(org.getAttribute("creationDateTime").get()));
  }

  @Test
  void testSetAttributeOneAttribute() throws ODataJPAProcessException {
    jpaAttributes.put("iD", "Willi");
    cut.setAttributes(jpaAttributes, partner, org);
    assertEquals("Willi", partner.getID());
  }

  @Test
  void testSetAttributeMultipleAttribute() throws ODataJPAProcessException {
    jpaAttributes.put("iD", "Willi");
    jpaAttributes.put("type", "2");
    cut.setAttributes(jpaAttributes, partner, org);
    assertEquals("Willi", partner.getID());
    assertEquals("2", partner.getType());
  }

  @Test
  void testSetAttributeIfAttributeNull() throws ODataJPAProcessException {
    partner.setType("2");
    jpaAttributes.put("iD", "Willi");
    jpaAttributes.put("type", null);
    cut.setAttributes(jpaAttributes, partner, org);
    assertEquals("Willi", partner.getID());
    assertNull(partner.getType());
  }

  @Test
  void testDoNotSetAttributeIfNotInMap() throws ODataJPAProcessException {
    partner.setType("2");
    jpaAttributes.put("iD", "Willi");
    cut.setAttributes(jpaAttributes, partner, org);
    assertEquals("Willi", partner.getID());
    assertEquals("2", partner.getType());
  }

  @Test
  void testSetAttributesDeepOneAttribute() throws ODataJPAProcessException {
    jpaAttributes.put("iD", "Willi");
    cut.setAttributesDeep(jpaAttributes, partner, org);
    assertEquals("Willi", partner.getID());
  }

  @Test
  void testSetAttributesDeepMultipleAttribute() throws ODataJPAProcessException {
    jpaAttributes.put("iD", "Willi");
    jpaAttributes.put("country", "DEU");
    cut.setAttributesDeep(jpaAttributes, partner, org);
    assertEquals("Willi", partner.getID());
    assertEquals("DEU", partner.getCountry());
  }

  @Test
  void testSetAttributeDeepIfAttributeNull() throws ODataJPAProcessException,
      ODataJPAInvocationTargetException {
    partner.setType("2");
    jpaAttributes.put("iD", "Willi");
    jpaAttributes.put("type", null);
    cut.setAttributesDeep(jpaAttributes, partner, org);
    assertEquals("Willi", partner.getID());
    assertNull(partner.getType());
  }

  @Test
  void testDoNotSetAttributeDeepIfNotInMap() throws ODataJPAProcessException,
      ODataJPAInvocationTargetException {
    partner.setType("2");
    jpaAttributes.put("iD", "Willi");
    cut.setAttributesDeep(jpaAttributes, partner, org);
    assertEquals("Willi", partner.getID());
    assertEquals("2", partner.getType());
  }

  @Test
  void testSetAttributesDeepShallIgnoreRequestEntities() throws ODataJPAProcessException,
      ODataJPAInvocationTargetException {
    try {
      final JPARequestEntity roles = mock(JPARequestEntity.class);
      jpaAttributes.put("iD", "Willi");
      jpaAttributes.put("roles", roles);
      cut.setAttributesDeep(jpaAttributes, partner, org);
    } catch (final Exception e) {
      fail();
    }

  }

  @Test
  void testSetAttributesDeepOneLevelViaGetter() throws ODataJPAProcessException,
      ODataJPAInvocationTargetException, ODataJPAModelException {
    final Map<String, Object> embeddedAttributes = new HashMap<>();
    jpaAttributes.put("iD", "Willi");
    jpaAttributes.put("address", embeddedAttributes);
    embeddedAttributes.put("cityName", "Test Town");
    cut.setAttributesDeep(jpaAttributes, partner, org);
    assertEquals("Willi", partner.getID());
    assertNotNull(partner.getAddress());
    assertEquals("Test Town", partner.getAddress().getCityName());
  }

  @Test
  void testSetAttributesDeepOneLevelViaGetterWithWrongRequestData() throws Throwable {
    final Map<String, Object> embeddedAttributes = new HashMap<>();
    final Map<String, Object> innerEmbeddedAttributes = new HashMap<>();
    jpaAttributes.put("iD", "Willi");
    jpaAttributes.put("administrativeInformation", embeddedAttributes);
    embeddedAttributes.put("updated", innerEmbeddedAttributes);
    innerEmbeddedAttributes.put("by", null);
    try {
      cut.setAttributesDeep(jpaAttributes, partner, org);
    } catch (final ODataJPAInvocationTargetException e) {
      assertEquals("Organization/AdministrativeInformation/Updated/By", e.getPath());
      assertEquals(NullPointerException.class, e.getCause().getClass());
    }
  }

  @Test
  void testDoNotSetAttributesDeepOneLevelIfNotProvided() throws ODataJPAProcessException,
      ODataJPAInvocationTargetException {
    jpaAttributes.put("iD", "Willi");
    jpaAttributes.put("address", null);
    cut.setAttributesDeep(jpaAttributes, partner, org);

    assertEquals("Willi", partner.getID());
    assertNull(partner.getAddress());
  }

  @Test
  void testSetAttributesDeepOneLevelIfNull() throws ODataJPAProcessException,
      ODataJPAInvocationTargetException {
    final PostalAddressData address = new PostalAddressData();
    address.setCityName("Test City");

    partner.setAddress(address);
    jpaAttributes.put("iD", "Willi");
    cut.setAttributesDeep(jpaAttributes, partner, org);

    assertEquals("Willi", partner.getID());
    assertNotNull(partner.getAddress());
    assertEquals("Test City", partner.getAddress().getCityName());
  }

  @Test
  void testSetAttributesDeepOneLevelViaSetter() throws ODataJPAProcessException,
      ODataJPAInvocationTargetException, ODataJPAModelException {
    final Map<String, Object> embeddedAttributes = new HashMap<>();
    jpaAttributes.put("iD", "Willi");
    jpaAttributes.put("communicationData", embeddedAttributes);
    embeddedAttributes.put("email", "Test@Town");
    cut.setAttributesDeep(jpaAttributes, partner, org);

    assertEquals("Willi", partner.getID());
    assertNotNull(partner.getCommunicationData());
    assertEquals("Test@Town", partner.getCommunicationData().getEmail());
  }

  @Test
  void testSetAttributesDeepTwoLevel() throws ODataJPAProcessException, ODataJPAModelException {
    final Map<String, Object> embeddedAttributes = new HashMap<>();
    final Map<String, Object> innerEmbeddedAttributes = new HashMap<>();
    jpaAttributes.put("iD", "Willi");
    jpaAttributes.put("administrativeInformation", embeddedAttributes);
    embeddedAttributes.put("updated", innerEmbeddedAttributes);
    innerEmbeddedAttributes.put("by", "Hugo");
    cut.setAttributesDeep(jpaAttributes, partner, org);

    assertEquals("Willi", partner.getID());
    assertNotNull(partner.getAdministrativeInformation());
    assertNotNull(partner.getAdministrativeInformation().getUpdated());
    assertEquals("Hugo", partner.getAdministrativeInformation().getUpdated().getBy());
  }

  @Test
  void testCreatePrimaryKeyOneStringKeyField() throws ODataJPAProcessException, ODataJPAModelException {
    final JPAEntityType et = createSingleKeyEntityType();

    when(et.getKeyType()).thenAnswer(new Answer<Class<?>>() {
      @Override
      public Class<?> answer(final InvocationOnMock invocation) throws Throwable {
        return String.class;
      }
    });

    jpaAttributes.put("iD", "Willi");
    final String act = (String) cut.createPrimaryKey(et, jpaAttributes, org);
    assertEquals("Willi", act);
  }

  @Test
  void testCreatePrimaryKeyOneIntegerKeyField() throws ODataJPAProcessException, ODataJPAModelException {
    final JPAEntityType et = createSingleKeyEntityType();

    when(et.getKeyType()).thenAnswer(new Answer<Class<?>>() {
      @Override
      public Class<?> answer(final InvocationOnMock invocation) throws Throwable {
        return Integer.class;
      }
    });

    jpaAttributes.put("iD", Integer.valueOf(10));
    final Integer act = (Integer) cut.createPrimaryKey(et, jpaAttributes, org);
    assertEquals(Integer.valueOf(10), act);
  }

  @Test
  void testCreatePrimaryKeyOneBigIntegerKeyField() throws ODataJPAProcessException, ODataJPAModelException {
    final JPAEntityType et = createSingleKeyEntityType();

    when(et.getKeyType()).thenAnswer(new Answer<Class<?>>() {
      @Override
      public Class<?> answer(final InvocationOnMock invocation) throws Throwable {
        return BigInteger.class;
      }
    });

    jpaAttributes.put("iD", new BigInteger("10"));
    final BigInteger act = (BigInteger) cut.createPrimaryKey(et, jpaAttributes, org);
    assertEquals(new BigInteger("10"), act);
  }

  @Test
  void testCreatePrimaryKeyMultipleField() throws ODataJPAProcessException, ODataJPAModelException {
    final JPAEntityType et = mock(JPAEntityType.class);

    when(et.getKeyType()).thenAnswer(new Answer<Class<?>>() {
      @Override
      public Class<?> answer(final InvocationOnMock invocation) throws Throwable {
        return AdministrativeDivisionKey.class;
      }
    });

    jpaAttributes.put("codePublisher", "Test");
    jpaAttributes.put("codeID", "10");
    jpaAttributes.put("divisionCode", "10.1");
    final AdministrativeDivisionKey act = (AdministrativeDivisionKey) cut.createPrimaryKey(et, jpaAttributes, org);
    assertEquals("Test", act.getCodePublisher());
    assertEquals("10", act.getCodeID());
    assertEquals("10.1", act.getDivisionCode());
  }

  @Test
  void testDeepLinkComplexNotExist() throws ODataJPAProcessorException, ODataJPAModelException {
    final Organization source = new Organization("100");
    final Person target = new Person();
    target.setID("A");
    final JPAAssociationPath path = helper.getJPAAssociationPath("Organizations",
        "AdministrativeInformation/Updated/User");

    cut.linkEntities(source, target, path);

    assertNotNull(source.getAdministrativeInformation());
    assertNotNull(source.getAdministrativeInformation().getUpdated());
    assertEquals(target, source.getAdministrativeInformation().getUpdated().getUser());
  }

  @Test
  void testDirectLink() throws ODataJPAProcessorException, ODataJPAModelException {
    final Organization source = new Organization("100");
    final BusinessPartnerRole target = new BusinessPartnerRole();
    target.setBusinessPartnerID("100");
    target.setRoleCategory("A");
    final JPAAssociationPath path = helper.getJPAAssociationPath("Organizations",
        "Roles");

    cut.linkEntities(source, target, path);

    assertNotNull(source.getRoles());
    assertNotNull(source.getRoles().toArray()[0]);
    assertEquals(target, source.getRoles().toArray()[0]);
  }

  @Test
  void testSetForeignKeyOneKey() throws ODataJPAModelException, ODataJPAProcessorException {
    final Organization source = new Organization("100");
    final BusinessPartnerRole target = new BusinessPartnerRole();
    target.setRoleCategory("A");
    final JPAAssociationPath path = helper.getJPAAssociationPath("Organizations",
        "Roles");

    cut.setForeignKey(source, target, path);
    assertEquals("100", target.getBusinessPartnerID());
  }

  @Test
  void testSetForeignKeyTrhowsExceptionOnMissingGetter() throws ODataJPAModelException,
      ODataJPAProcessorException {
    final OrganizationWithoutGetter source = new OrganizationWithoutGetter("100");
    final BusinessPartnerRole target = new BusinessPartnerRole();
    target.setRoleCategory("A");
    final JPAAssociationPath path = helper.getJPAAssociationPath("Organizations",
        "Roles");
    assertThrows(ODataJPAProcessorException.class, () -> {
      cut.setForeignKey(source, target, path);
    });
  }

  @Test
  void testSetForeignKeyTrhowsExceptionOnMissingSetter() throws ODataJPAModelException,
      ODataJPAProcessorException {
    final Organization source = new Organization("100");
    final BusinessPartnerRoleWithoutSetter target = new BusinessPartnerRoleWithoutSetter();
    final JPAAssociationPath path = helper.getJPAAssociationPath("Organizations",
        "Roles");

    assertThrows(ODataJPAProcessorException.class, () -> {
      cut.setForeignKey(source, target, path);
    });
  }

  @Test
  void testBuildSetterList() throws ODataJPAModelException {
    final List<JPAAttribute> attributes = Arrays.asList(org.getAttribute("creationDateTime").get(),
        org.getAttribute("country").get());

    final Map<JPAAttribute, Method> act = cut.buildSetterList(org.getTypeClass(), attributes);

    assertEquals(2, act.size());
    assertNotNull(act.get(org.getAttribute("creationDateTime").get()));
    assertNotNull(act.get(org.getAttribute("country").get()));
    assertEquals(LocalDateTime.class, act.get(org.getAttribute("creationDateTime").get()).getParameterTypes()[0]);
  }

  @Test
  void testBuildSetterListContainsNullIfForMissingSetter() throws ODataJPAModelException {

    final JPAEntityType country = helper.getJPAEntityType(Country.class);

    final List<JPAAttribute> attributes = Arrays.asList(country.getAttribute("code").get(),
        country.getAttribute("name").get());

    final Map<JPAAttribute, Method> act = cut.buildSetterList(country.getTypeClass(), attributes);

    assertEquals(2, act.size());
    assertNotNull(act.get(country.getAttribute("code").get()));
    assertNull(act.get(country.getAttribute("name").get()));
    assertEquals(String.class, act.get(country.getAttribute("code").get()).getParameterTypes()[0]);
  }

  private JPAEntityType createSingleKeyEntityType() throws ODataJPAModelException {
    final List<JPAAttribute> keyAttributes = new ArrayList<>();
    final JPAAttribute keyAttribute = mock(JPAAttribute.class);
    final JPAEntityType et = mock(JPAEntityType.class);

    when(keyAttribute.getInternalName()).thenReturn("iD");
    keyAttributes.add(keyAttribute);
    when(et.getKey()).thenReturn(keyAttributes);
    return et;
  }
}
