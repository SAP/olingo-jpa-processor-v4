package com.sap.olingo.jpa.metadata.core.edm.mapper.annotation;

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
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;

public class TestVocabularyReader {
  private static final String CORE_ANNOTATIONS = "annotations/Org.OData.Core.V1.xml";
  private VocabularyReader cut;
  private Charset charset;

  @BeforeEach
  public void setup() {
    cut = new VocabularyReader();
    charset = Charset.defaultCharset();
  }

  @Test
  public void testReadFromURIThrowsNullPointerOnNull() throws ODataJPAModelException, IOException {
    assertThrows(NullPointerException.class, () -> cut.readFromURI(null));
  }

  @Test
  public void testReadFromResourceThrowsNullPointerOnNull() throws ODataJPAModelException, IOException {
    assertThrows(NullPointerException.class, () -> cut.readFromResource(null, charset));
    assertThrows(NullPointerException.class, () -> cut.readFromResource(CORE_ANNOTATIONS, null));
  }

  @Test
  public void testReadFromResourceReturnsNullOnEmptyPath() throws ODataJPAModelException, IOException {
    assertNull(cut.readFromResource("", charset));
  }

  @Test
  public void testReadFromResourceReturnsVocabulary() throws IOException, ODataJPAModelException {

    final Vocabulary act = cut.readFromResource(CORE_ANNOTATIONS, charset);
    assertNotNull(act);
    assertFalse(act.getSchemas().isEmpty());
    assertNotNull(act.getSchemas().get("Org.OData.Core.V1"));
  }

  @Test
  public void testtReadFromResourceThrowsExceptionOnUnknownPath() throws IOException {
    assertThrows(ODataJPAModelException.class, () -> {
      cut.readFromResource("annotations/Org.OData.Core.V2.xml", charset);
    });
  }

  @Test
  public void testtReadFromResourceThrowsExceptionOnEmptyXML() throws IOException, ODataJPAModelException {

    assertThrows(IOException.class, () -> {
      cut.readFromResource("annotations/empty.xml", charset);
    });
  }

  // TODO This test may not run because of proxy setting problems!! -> find alternative for Integration tests
  @Disabled
  @Test
  public void testReadFromURI() throws URISyntaxException, JsonParseException, JsonMappingException,
      MalformedURLException, IOException {
    final URI uri = new URI("http://docs.oasis-open.org/odata/odata/v4.0/os/vocabularies/Org.OData.Core.V1.xml");
    final Vocabulary actVocabulary = cut.readFromURI(uri);
    assertNotNull(actVocabulary);
    assertNotNull(actVocabulary.getDataService());

    final List<Schema> actSchemas = actVocabulary.getDataService().getSchemas();
    assertEquals(1, actSchemas.size());
    assertEquals("Org.OData.Core.V1", actSchemas.get(0).getNamespace());
  }
}
