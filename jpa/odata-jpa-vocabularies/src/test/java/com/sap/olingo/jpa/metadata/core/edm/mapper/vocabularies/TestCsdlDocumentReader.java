package com.sap.olingo.jpa.metadata.core.edm.mapper.vocabularies;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

class TestCsdlDocumentReader {
  private static final String CORE_ANNOTATIONS = "annotations/Org.OData.Core.V1.xml";
  private CsdlDocumentReader cut;
  private Charset charset;

  @BeforeEach
  void setup() {
    cut = new CsdlDocumentReader();
    charset = Charset.defaultCharset();
  }

  @Test
  void testReadFromURIThrowsNullPointerOnNull() throws ODataJPAVocabulariesException, IOException {
    assertThrows(NullPointerException.class, () -> cut.readFromURI(null));
  }

  @Test
  void testReadFromResourceThrowsNullPointerOnNull() throws ODataJPAVocabulariesException, IOException {
    assertThrows(NullPointerException.class, () -> cut.readFromResource(null, charset));
    assertThrows(NullPointerException.class, () -> cut.readFromResource(CORE_ANNOTATIONS, null));
  }

  @Test
  void testReadFromResourceReturnsNullOnEmptyPath() throws ODataJPAVocabulariesException, IOException {
    assertNull(cut.readFromResource("", charset));
  }

  @Test
  void testReadFromResourceReturnsVocabulary() throws IOException, ODataJPAVocabulariesException {

    final CsdlDocument act = cut.readFromResource(CORE_ANNOTATIONS, charset);
    assertNotNull(act);
    assertFalse(act.getSchemas().isEmpty());
    assertNotNull(act.getSchemas().get("Org.OData.Core.V1"));
  }

  @Test
  void testReadFromResourceThrowsExceptionOnUnknownPath() throws IOException {
    assertThrows(ODataJPAVocabulariesException.class, () -> {
      cut.readFromResource("annotations/Org.OData.Core.V2.xml", charset);
    });
  }

  @Test
  void testReadFromResourceThrowsExceptionOnEmptyXML() throws IOException, ODataJPAVocabulariesException {

    assertThrows(IOException.class, () -> {
      cut.readFromResource("annotations/empty.xml", charset);
    });
  }

  @Test
  void testReadDocumentContainsReferences() throws IOException, ODataJPAVocabulariesException {
//  <edmx:Reference Uri="http://docs.oasis-open.org/odata/odata-vocabularies/v4.0/vocabularies/Org.OData.Validation.V1.xml">
//    <edmx:Include Alias="Validation" Namespace="Org.OData.Validation.V1" />
//  </edmx:Reference>
    final CsdlDocument act = cut.readFromResource(CORE_ANNOTATIONS, charset);
    assertNotNull(act.getReference());
    assertEquals(1, act.getReference().size());
    final EdmxReference ref = act.getReference().get(0);
    assertNotNull(ref.getIncludes());
    assertEquals(1, ref.getIncludes().size());
    final EdmxReferenceInclude include = ref.getIncludes().get(0);
    assertEquals("Validation", include.getAlias());
    assertEquals("Org.OData.Validation.V1", include.getNamespace());
  }

  @Disabled("This test may not run because of proxy setting problems!! -> find alternative for Integration tests")
  @Test
  void testReadFromURI() throws URISyntaxException, JsonParseException, JsonMappingException,
      MalformedURLException, IOException {
    final URI uri = new URI("http://docs.oasis-open.org/odata/odata/v4.0/os/vocabularies/Org.OData.Core.V1.xml");
    final CsdlDocument actVocabulary = cut.readFromURI(uri);
    assertNotNull(actVocabulary);
    assertNotNull(actVocabulary.getDataService());

    final List<Schema> actSchemas = actVocabulary.getDataService().getSchemas();
    assertEquals(1, actSchemas.size());
    assertEquals("Org.OData.Core.V1", actSchemas.get(0).getNamespace());
  }
}
