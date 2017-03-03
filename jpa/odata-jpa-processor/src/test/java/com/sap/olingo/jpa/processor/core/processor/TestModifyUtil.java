package com.sap.olingo.jpa.processor.core.processor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessorException;
import com.sap.olingo.jpa.processor.core.testmodel.BusinessPartner;
import com.sap.olingo.jpa.processor.core.testmodel.Organization;

public class TestModifyUtil {
  private JPAModifyUtil cut;
  private Map<String, Object> jpaAttributes;
  private BusinessPartner partner;

  @Before
  public void setUp() {
    cut = new JPAModifyUtil();
    jpaAttributes = new HashMap<String, Object>();
    partner = new Organization();
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
  public void testSetAttributesDeepShallIgnoreRequestEntities() throws ODataJPAProcessorException {
    JPARequestEntity roles = mock(JPARequestEntity.class);

    jpaAttributes.put("iD", "Willi");
    jpaAttributes.put("roles", roles);
    cut.setAttributesDeep(jpaAttributes, partner);
  }

  @Test
  public void testSetAttributesDeepOneLevelViaGetter() throws ODataJPAProcessorException {
    Map<String, Object> embeddedAttributes = new HashMap<String, Object>();
    jpaAttributes.put("iD", "Willi");
    jpaAttributes.put("address", embeddedAttributes);
    embeddedAttributes.put("cityName", "Test Town");
    cut.setAttributesDeep(jpaAttributes, partner);

    assertEquals("Willi", partner.getID());
    assertNotNull(partner.getAddress());
    assertEquals("Test Town", partner.getAddress().getCityName());
  }

  @Test
  public void testSetAttributesDeepOneLevelViaSetter() throws ODataJPAProcessorException {
    Map<String, Object> embeddedAttributes = new HashMap<String, Object>();
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
    Map<String, Object> embeddedAttributes = new HashMap<String, Object>();
    Map<String, Object> innerEmbeddedAttributes = new HashMap<String, Object>();
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
}
