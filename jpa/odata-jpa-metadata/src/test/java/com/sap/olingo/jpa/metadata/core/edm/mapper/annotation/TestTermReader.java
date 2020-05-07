package com.sap.olingo.jpa.metadata.core.edm.mapper.annotation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import org.apache.olingo.commons.api.edm.provider.CsdlTerm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;

public class TestTermReader {
  private TermReader cut;

  @BeforeEach
  public void setup() {
    cut = new TermReader();
  }

  @Test
  public void testConvertEDMXReturnsNullIfEDMXNull() throws ODataJPAModelException, IOException {
    assertNull(cut.convertEDMX(null));
  }

  @Test
  public void testConvertEDMXReturnsNullIfDataServiceNull() throws ODataJPAModelException, IOException {
    final Edmx edmx = mock(Edmx.class);
    when(edmx.getDataService()).thenReturn(null);
    assertNull(cut.convertEDMX(edmx));
  }

  @Test
  public void testGetTermsByUriThrowsExceptionOnNull() throws IOException {
    final URI nullUri = null;
    assertThrows(NullPointerException.class, () -> cut.getTerms(nullUri));
  }

  @Test
  public void testGetTermsByPathThrowsExceptionOnNull() throws IOException {
    final String nullString = null;
    assertThrows(NullPointerException.class, () -> cut.getTerms(nullString));
  }

  @Test
  public void testReadFromResource() throws JsonParseException, JsonMappingException, IOException,
      ODataJPAModelException {
    Edmx actEdmx = cut.readFromResource("annotations/Org.OData.Measures.V1.xml");
    assertNotNull(actEdmx);
    assertNotNull(actEdmx.getDataService());

    List<Schema> actSchemas = actEdmx.getDataService().getSchemas();
    assertEquals(1, actSchemas.size());
    assertEquals("Org.OData.Measures.V1", actSchemas.get(0).getNamespace());
  }

  @Test
  public void testGetTermsOneSchemaFromPath() throws JsonParseException, JsonMappingException, IOException,
      ODataJPAModelException {
    Map<String, Map<String, CsdlTerm>> act;
    act = cut.getTerms("annotations/Org.OData.Core.V1.xml");
    assertNotNull(act.get("Org.OData.Core.V1"));
    Map<String, CsdlTerm> terms = act.get("Org.OData.Core.V1");
    assertEquals(28, terms.size());
  }

  @Test
  public void testGetAppliesTo() throws JsonParseException, JsonMappingException, IOException, ODataJPAModelException {
    Map<String, Map<String, CsdlTerm>> act;
    act = cut.getTerms("annotations/Org.OData.Core.V1.xml");
    assertNotNull(act.get("Org.OData.Core.V1"));
    Map<String, CsdlTerm> terms = act.get("Org.OData.Core.V1");
    CsdlTerm term = terms.get("IsLanguageDependent");
    assertEquals(2, term.getAppliesTo().size());
    assertTrue("Term".equals(term.getAppliesTo().get(0)) || "Term".equals(term.getAppliesTo().get(1)));
    assertTrue("Property".equals(term.getAppliesTo().get(0)) || "Property".equals(term.getAppliesTo().get(1)));
  }

  @Test
  public void testGetTermsTwoSchemaFromPath() throws JsonParseException, JsonMappingException, IOException,
      ODataJPAModelException {
    Map<String, Map<String, CsdlTerm>> act;
    act = cut.getTerms("annotations/Org.Olingo.Test.V1.xml");
    assertNotNull(act.get("Org.OData.Measures.V1"));
    assertNotNull(act.get("Org.OData.Capabilities.V1"));
  }

  // TODO This test may not run because of proxy setting problems!! -> find alternative for Integration tests
  @Disabled
  @Test
  public void testReadFromURI() throws URISyntaxException, JsonParseException, JsonMappingException,
      MalformedURLException, IOException {
    URI uri = new URI("http://docs.oasis-open.org/odata/odata/v4.0/os/vocabularies/Org.OData.Core.V1.xml");
    Edmx actEdmx = cut.readFromURI(uri);
    assertNotNull(actEdmx);
    assertNotNull(actEdmx.getDataService());

    List<Schema> actSchemas = actEdmx.getDataService().getSchemas();
    assertEquals(1, actSchemas.size());
    assertEquals("Org.OData.Core.V1", actSchemas.get(0).getNamespace());
  }

  // TODO This test may not run because of proxy setting problems!! -> find alternative for Integration tests
  @Disabled
  @Test
  public void testGetTermsOneSchemaFromURI() throws URISyntaxException, JsonParseException, JsonMappingException,
      MalformedURLException, IOException {
    URI uri = new URI("http://docs.oasis-open.org/odata/odata/v4.0/os/vocabularies/Org.OData.Core.V1.xml");
    Map<String, Map<String, CsdlTerm>> act;
    act = cut.getTerms(uri);
    assertNotNull(act.get("Org.OData.Core.V1"));
  }
}
