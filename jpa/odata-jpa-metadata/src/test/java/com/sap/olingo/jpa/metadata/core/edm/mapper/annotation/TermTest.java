package com.sap.olingo.jpa.metadata.core.edm.mapper.annotation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;

import org.apache.olingo.commons.api.edm.geo.SRID;
import org.apache.olingo.commons.api.edm.provider.CsdlTerm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;

class TermTest {
  private static final String TEST_ANNOTATIONS = "annotations/Org.Olingo.Test.V1.xml";
  private static final String CORE_ANNOTATIONS = "annotations/Org.OData.Core.V1.xml";
  private CsdlDocument cutCore;
  private CsdlDocument cutTest;
  private Charset charset;

  @BeforeEach
  void setup() throws ODataJPAModelException, IOException {
    final CsdlDocumentReader reader = new CsdlDocumentReader();
    charset = Charset.defaultCharset();
    cutCore = reader.readFromResource(CORE_ANNOTATIONS, charset);
    cutTest = reader.readFromResource(TEST_ANNOTATIONS, charset);
  }

  @Test
  void testGetTermsOneSchemaFromPath() throws JsonParseException, JsonMappingException, IOException,
      ODataJPAModelException {

    final Map<String, Map<String, CsdlTerm>> act = cutCore.getTerms();
    assertNotNull(act.get("Org.OData.Core.V1"));
    final Map<String, CsdlTerm> terms = act.get("Org.OData.Core.V1");
    assertEquals(28, terms.size());
  }

  @Test
  void testGetAppliesTo() throws JsonParseException, JsonMappingException, IOException, ODataJPAModelException {
    final Map<String, Map<String, CsdlTerm>> act = cutCore.getTerms();
    assertNotNull(act.get("Org.OData.Core.V1"));
    final Map<String, CsdlTerm> terms = act.get("Org.OData.Core.V1");
    final CsdlTerm term = terms.get("IsLanguageDependent");
    assertEquals(2, term.getAppliesTo().size());
    assertTrue("Term".equals(term.getAppliesTo().get(0)) || "Term".equals(term.getAppliesTo().get(1)));
    assertTrue("Property".equals(term.getAppliesTo().get(0)) || "Property".equals(term.getAppliesTo().get(1)));
    assertEquals("true", term.getDefaultValue());
  }

  @Test
  void testGetTermsTwoSchemaFromPath() throws JsonParseException, JsonMappingException, IOException,
      ODataJPAModelException {
    final Map<String, Map<String, CsdlTerm>> act = cutTest.getTerms();
    assertNotNull(act.get("Org.OData.Measures.V1"));
    assertNotNull(act.get("Org.OData.Capabilities.V1"));
  }

  @Test
  void testGetTermWithScalePresition() throws JsonParseException, JsonMappingException, IOException,
      ODataJPAModelException {
    final Map<String, Map<String, CsdlTerm>> act = cutTest.getTerms();

    final Map<String, CsdlTerm> terms = act.get("Org.OData.Measures.V1");
    final CsdlTerm term = terms.get("MultipleOf");
    assertNotNull(term);
    assertEquals(10, term.getScale());
    assertEquals(5, term.getPrecision());
  }

  @Test
  void testGetTermWithBaseTypeMaxLength() throws JsonParseException, JsonMappingException, IOException,
      ODataJPAModelException {
    final Map<String, Map<String, CsdlTerm>> act = cutTest.getTerms();

    final Map<String, CsdlTerm> terms = act.get("Org.OData.Measures.V1");
    final CsdlTerm term = terms.get("Unit2");
    assertNotNull(term);
    assertEquals("Unit", term.getBaseTerm());
    assertEquals(2, term.getMaxLength());
  }

  @Test
  void testGetTermWithSrid() throws JsonParseException, JsonMappingException, IOException,
      ODataJPAModelException {
    final Map<String, Map<String, CsdlTerm>> act = cutTest.getTerms();

    final Map<String, CsdlTerm> terms = act.get("Org.OData.Measures.V1");
    final CsdlTerm term = terms.get("Geo");
    assertNotNull(term);
    assertEquals(SRID.valueOf("1234"), term.getSrid());
  }

  @Test
  void testGetTermWithSridVariableNotSupported() throws JsonParseException, JsonMappingException, IOException,
      ODataJPAModelException {

    final Term cut = new Term();
    cut.setSrid("variable");
    assertTrue(cut.getSrid().toString().contains("variable"));
  }

  @Test
  void testGetTermWithScaleVariableNotSupported() throws JsonParseException, JsonMappingException, IOException,
      ODataJPAModelException {

    final Term cut = new Term();
    assertThrows(ODataJPAModelException.class, () -> cut.setScale("variable"));
  }
}
