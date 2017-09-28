package com.sap.olingo.jpa.processor.core.processor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessorException;
import com.sap.olingo.jpa.processor.core.testmodel.AdministrativeDivisionKey;
import com.sap.olingo.jpa.processor.core.testmodel.BusinessPartner;
import com.sap.olingo.jpa.processor.core.testmodel.Organization;
import com.sap.olingo.jpa.processor.core.testmodel.PostalAddressData;

public class TestModifyUtil {
  private JPAModifyUtil cut;
  private Map<String, Object> jpaAttributes;
  private BusinessPartner partner;

  @Before
  public void setUp() {
    cut = new JPAModifyUtil();
    jpaAttributes = new HashMap<>();
    partner = new Organization();
  }

  @Test
  public void testSetAttributeOneAttribute() throws ODataJPAProcessorException {
    jpaAttributes.put("iD", "Willi");
    cut.setAttributes(jpaAttributes, partner);
    assertEquals("Willi", partner.getID());
  }

  @Test
  public void testSetAttributeMultipleAttribute() throws ODataJPAProcessorException {
    jpaAttributes.put("iD", "Willi");
    jpaAttributes.put("type", "2");
    cut.setAttributes(jpaAttributes, partner);
    assertEquals("Willi", partner.getID());
    assertEquals("2", partner.getType());
  }

  @Test
  public void testSetAttributeIfAttributeNull() throws ODataJPAProcessorException {
    partner.setType("2");
    jpaAttributes.put("iD", "Willi");
    jpaAttributes.put("type", null);
    cut.setAttributes(jpaAttributes, partner);
    assertEquals("Willi", partner.getID());
    assertNull(partner.getType());
  }

  @Test
  public void testDoNotSetAttributeIfNotInMap() throws ODataJPAProcessorException {
    partner.setType("2");
    jpaAttributes.put("iD", "Willi");
    cut.setAttributes(jpaAttributes, partner);
    assertEquals("Willi", partner.getID());
    assertEquals("2", partner.getType());
  }

  @Test
  public void testSetAttributesDeepOneAttribute() throws ODataJPAProcessorException {
    jpaAttributes.put("iD", "Willi");
    cut.setAttributesDeep(jpaAttributes, partner);
    assertEquals("Willi", partner.getID());
  }

  @Test
  public void testSetAttributesDeepMultipleAttribute() throws ODataJPAProcessorException {
    jpaAttributes.put("iD", "Willi");
    jpaAttributes.put("country", "DEU");
    cut.setAttributesDeep(jpaAttributes, partner);
    assertEquals("Willi", partner.getID());
    assertEquals("DEU", partner.getCountry());
  }

  @Test
  public void testSetAttributeDeepIfAttributeNull() throws ODataJPAProcessorException {
    partner.setType("2");
    jpaAttributes.put("iD", "Willi");
    jpaAttributes.put("type", null);
    cut.setAttributesDeep(jpaAttributes, partner);
    assertEquals("Willi", partner.getID());
    assertNull(partner.getType());
  }

  @Test
  public void testDoNotSetAttributeDeepIfNotInMap() throws ODataJPAProcessorException {
    partner.setType("2");
    jpaAttributes.put("iD", "Willi");
    cut.setAttributesDeep(jpaAttributes, partner);
    assertEquals("Willi", partner.getID());
    assertEquals("2", partner.getType());
  }

  @Test
  public void testSetAttributesDeepShallIgnoreRequestEntities() throws ODataJPAProcessorException {
    JPARequestEntity roles = mock(JPARequestEntity.class);

    jpaAttributes.put("iD", "Willi");
    jpaAttributes.put("roles", roles);
    cut.setAttributesDeep(jpaAttributes, partner);
  }

  @Test
  public void testSetAttributesDeepOneLevelViaGetter() throws ODataJPAProcessorException {
    Map<String, Object> embeddedAttributes = new HashMap<>();
    jpaAttributes.put("iD", "Willi");
    jpaAttributes.put("address", embeddedAttributes);
    embeddedAttributes.put("cityName", "Test Town");
    cut.setAttributesDeep(jpaAttributes, partner);

    assertEquals("Willi", partner.getID());
    assertNotNull(partner.getAddress());
    assertEquals("Test Town", partner.getAddress().getCityName());
  }

  @Test
  public void testDoNotSetAttributesDeepOneLevelIfNotProvided() throws ODataJPAProcessorException {

    jpaAttributes.put("iD", "Willi");
    jpaAttributes.put("address", null);
    cut.setAttributesDeep(jpaAttributes, partner);

    assertEquals("Willi", partner.getID());
    assertNull(partner.getAddress());
  }

  @Test
  public void testSetAttributesDeepOneLevelIfNull() throws ODataJPAProcessorException {
    final PostalAddressData address = new PostalAddressData();
    address.setCityName("Test City");

    partner.setAddress(address);
    jpaAttributes.put("iD", "Willi");
    cut.setAttributesDeep(jpaAttributes, partner);

    assertEquals("Willi", partner.getID());
    assertNotNull(partner.getAddress());
    assertEquals("Test City", partner.getAddress().getCityName());
  }

  @Test
  public void testSetAttributesDeepOneLevelViaSetter() throws ODataJPAProcessorException {
    Map<String, Object> embeddedAttributes = new HashMap<>();
    jpaAttributes.put("iD", "Willi");
    jpaAttributes.put("communicationData", embeddedAttributes);
    embeddedAttributes.put("email", "Test@Town");
    cut.setAttributesDeep(jpaAttributes, partner);

    assertEquals("Willi", partner.getID());
    assertNotNull(partner.getCommunicationData());
    assertEquals("Test@Town", partner.getCommunicationData().getEmail());
  }

  @Test
  public void testSetAttributesDeepTwoLevel() throws ODataJPAProcessorException {
    Map<String, Object> embeddedAttributes = new HashMap<>();
    Map<String, Object> innerEmbeddedAttributes = new HashMap<>();
    jpaAttributes.put("iD", "Willi");
    jpaAttributes.put("administrativeInformation", embeddedAttributes);
    embeddedAttributes.put("updated", innerEmbeddedAttributes);
    innerEmbeddedAttributes.put("by", "Hugo");
    cut.setAttributesDeep(jpaAttributes, partner);

    assertEquals("Willi", partner.getID());
    assertNotNull(partner.getAdministrativeInformation());
    assertNotNull(partner.getAdministrativeInformation().getUpdated());
    assertEquals("Hugo", partner.getAdministrativeInformation().getUpdated().getBy());
  }

  @Test
  public void testCreatePrimaryKeyOneStringKeyField() throws ODataJPAProcessorException, ODataJPAModelException {
    final JPAEntityType et = createSingleKeyEntityType();

    when(et.getKeyType()).thenAnswer(new Answer<Class<?>>() {
      @Override
      public Class<?> answer(InvocationOnMock invocation) throws Throwable {
        return String.class;
      }
    });

    jpaAttributes.put("iD", "Willi");
    String act = (String) cut.createPrimaryKey(et, jpaAttributes);
    assertEquals("Willi", act);
  }

  @Test
  public void testCreatePrimaryKeyOneIntegerKeyField() throws ODataJPAProcessorException, ODataJPAModelException {
    final JPAEntityType et = createSingleKeyEntityType();

    when(et.getKeyType()).thenAnswer(new Answer<Class<?>>() {
      @Override
      public Class<?> answer(InvocationOnMock invocation) throws Throwable {
        return Integer.class;
      }
    });

    jpaAttributes.put("iD", new Integer(10));
    Integer act = (Integer) cut.createPrimaryKey(et, jpaAttributes);
    assertEquals(new Integer(10), act);
  }

  @Test
  public void testCreatePrimaryKeyOneBigIntegerKeyField() throws ODataJPAProcessorException, ODataJPAModelException {
    final JPAEntityType et = createSingleKeyEntityType();

    when(et.getKeyType()).thenAnswer(new Answer<Class<?>>() {
      @Override
      public Class<?> answer(InvocationOnMock invocation) throws Throwable {
        return BigInteger.class;
      }
    });

    jpaAttributes.put("iD", new BigInteger("10"));
    BigInteger act = (BigInteger) cut.createPrimaryKey(et, jpaAttributes);
    assertEquals(new BigInteger("10"), act);
  }

  @Test
  public void testCreatePrimaryKeyMultipleField() throws ODataJPAProcessorException, ODataJPAModelException {
    final JPAEntityType et = mock(JPAEntityType.class);

    when(et.getKeyType()).thenAnswer(new Answer<Class<?>>() {
      @Override
      public Class<?> answer(InvocationOnMock invocation) throws Throwable {
        return AdministrativeDivisionKey.class;
      }
    });

    jpaAttributes.put("codePublisher", "Test");
    jpaAttributes.put("codeID", "10");
    jpaAttributes.put("divisionCode", "10.1");
    AdministrativeDivisionKey act = (AdministrativeDivisionKey) cut.createPrimaryKey(et, jpaAttributes);
    assertEquals("Test", act.getCodePublisher());
    assertEquals("10", act.getCodeID());
    assertEquals("10.1", act.getDivisionCode());
  }

  private JPAEntityType createSingleKeyEntityType() throws ODataJPAModelException {
    final List<JPAAttribute> keyAttributes = new ArrayList<>();
    final JPAAttribute keyAttribut = mock(JPAAttribute.class);
    final JPAEntityType et = mock(JPAEntityType.class);

    when(keyAttribut.getInternalName()).thenReturn("iD");
    keyAttributes.add(keyAttribut);
    when(et.getKey()).thenReturn(keyAttributes);
    return et;
  }
}
