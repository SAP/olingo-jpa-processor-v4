package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.net.URISyntaxException;
import java.util.List;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.commons.api.edmx.EdmxReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.sap.olingo.jpa.metadata.api.JPAEdmMetadataPostProcessor;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAServiceDocument;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extension.IntermediateEntityTypeAccess;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extension.IntermediateNavigationPropertyAccess;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extension.IntermediatePropertyAccess;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extension.IntermediateReferenceList;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extension.IntermediateReferenceList.IntermediateReferenceAccess;

public class TestIntermediateReferences extends TestMappingRoot {

  private static final String TEST_V1_URL =
      "http://org.example/odata/odata/v4.0/os/vocabularies/Org.Olingo.Test.V1.xml";
  private static final String MEASURES_V1_URL =
      "https://oasis-tcs.github.io/odata-vocabularies/vocabularies/Org.OData.Measures.V1.xml";
  private static final String CORE_V1_URL =
      "https://oasis-tcs.github.io/odata-vocabularies/vocabularies/Org.OData.Core.V1.xml";
  private IntermediateReferences cut;

  @BeforeEach
  public void setup() throws ODataJPAModelException {
    cut = new IntermediateReferences();
  }

  // TODO This test may not run because of proxy setting problems!! -> find alternative for Integration tests
  @Disabled
  @Test
  public void checkAddOnlyURI() throws ODataJPAModelException, URISyntaxException {
    final String uri = CORE_V1_URL;
    cut.addReference(uri);
    final List<EdmxReference> act = cut.getEdmReferences();
    assertEquals(1, act.size());
    assertEquals(act.get(0).getUri().toString(), uri);
  }

  @Test
  public void checkThrowsExceptionOnEmptyPath() throws ODataJPAModelException, URISyntaxException {

    assertThrows(ODataJPAModelException.class, () -> cut.addReference(CORE_V1_URL, ""));
  }

  @Test
  public void checkAddURIandPath() throws ODataJPAModelException, URISyntaxException {
    final String uri = MEASURES_V1_URL;
    final String path = "annotations/Org.OData.Measures.V1.xml";
    cut.addReference(uri, path);
    final List<EdmxReference> act = cut.getEdmReferences();
    assertEquals(1, act.size());
    assertEquals(uri, act.get(0).getUri().toString());
    assertEquals(path, ((IntermediateReferenceList.IntermediateReferenceAccess) cut.references.get(0)).getPath());
    assertEquals(uri, ((IntermediateReferenceList.IntermediateReferenceAccess) cut.references.get(0)).getURI()
        .toString());
  }

  @Test
  public void checkConvertedToCsdlContainsInclude() throws ODataJPAModelException {
    JPAServiceDocument serviceDocument;
    serviceDocument = new IntermediateServiceDocument(PUNIT_NAME, emf.getMetamodel(), new PostProcessor(), null);
    assertEquals(1, serviceDocument.getReferences().size());

    final EdmxReference ref = serviceDocument.getReferences().get(0);
    assertEquals(1, ref.getIncludes().size());
  }

  @Test
  public void checkConvertedToCsdlContainsIncludeAnnotation() throws ODataJPAModelException {
    JPAServiceDocument serviceDocument;
    serviceDocument = new IntermediateServiceDocument(PUNIT_NAME, emf.getMetamodel(), new PostProcessor(), null);
    assertEquals(1, serviceDocument.getReferences().size());

    final EdmxReference ref = serviceDocument.getReferences().get(0);
    assertEquals(1, ref.getIncludeAnnotations().size());
  }

  @Test
  public void checkGetOneSchema() throws ODataJPAModelException {
    final String uri = MEASURES_V1_URL;
    final IntermediateReferenceAccess ref = cut.addReference(uri, "annotations/Org.OData.Measures.V1.xml");
    ref.addInclude("Org.OData.Measures.V1", "");

    assertEquals(1, cut.getSchemas().size());
  }

  @Test
  public void checkGetTwoSchemas() throws ODataJPAModelException {
    final String uri = TEST_V1_URL;
    final IntermediateReferenceAccess ref = cut.addReference(uri, "annotations/Org.Olingo.Test.V1.xml");
    ref.addInclude("Org.Olingo.Test.V1.xml", "");

    assertEquals(2, cut.getSchemas().size());
  }

  @Test
  public void checkGetComplexType() throws ODataJPAModelException {
    final String uri = TEST_V1_URL;
    final IntermediateReferenceAccess ref = cut.addReference(uri, "annotations/Org.Olingo.Test.V1.xml");
    ref.addInclude("Org.Olingo.Test.V1.xml", "");

    for (final CsdlSchema schema : cut.getSchemas())
      if (schema.getNamespace().equals("Org.OData.Capabilities.V1")) {
        assertNotNull(schema.getComplexType("UpdateRestrictionsType"));
        return;
      }
    fail();
  }

  @Test
  public void checkGetTermByNamespace() throws ODataJPAModelException {
    final String uri = MEASURES_V1_URL;
    final IntermediateReferenceAccess ref = cut.addReference(uri, "annotations/Org.OData.Measures.V1.xml");
    ref.addInclude("Org.OData.Measures.V1", "");
    final FullQualifiedName fqn = new FullQualifiedName("Org.OData.Measures.V1", "ISOCurrency");
    assertNotNull(cut.getTerm(fqn));
  }

  @Test
  public void checkGetTermByAlias() throws ODataJPAModelException {
    final String uri = MEASURES_V1_URL;
    final IntermediateReferenceAccess ref = cut.addReference(uri, "annotations/Org.OData.Measures.V1.xml");
    ref.addInclude("Org.OData.Measures.V1", "Measures");
    final FullQualifiedName fqn = new FullQualifiedName("Measures", "ISOCurrency");
    assertNotNull(cut.getTerm(fqn));
  }

  @Test
  public void checkReturnNullOnUnknowTerm() throws ODataJPAModelException {
    final String uri = MEASURES_V1_URL;
    final IntermediateReferenceAccess ref = cut.addReference(uri, "annotations/Org.OData.Measures.V1.xml");
    ref.addInclude("Org.OData.Measures.V1", "Measures");
    final FullQualifiedName fqn = new FullQualifiedName("Measures", "Dummy");
    assertNull(cut.getTerm(fqn));
  }

  @Test
  public void checkReturnNullOnUnknowNamespace() throws ODataJPAModelException {
    final String uri = MEASURES_V1_URL;
    final IntermediateReferenceAccess ref = cut.addReference(uri, "annotations/Org.OData.Measures.V1.xml");
    ref.addInclude("Org.OData.Measures.V1", "Measures");
    final FullQualifiedName fqn = new FullQualifiedName("Dummy", "ISOCurrency");
    assertNull(cut.getTerm(fqn));
  }

  @Test
  public void checkAddIncludeWithoutNameSpace() throws ODataJPAModelException {
    final String uri = MEASURES_V1_URL;
    final IntermediateReferenceAccess ref = cut.addReference(uri, "annotations/Org.OData.Measures.V1.xml");
    ref.addInclude("Org.OData.Measures.V1");
    assertTrue(cut.aliasDirectory.isEmpty());
  }

  @Test
  public void checkThrowsExcpetionOnNullTermNamespace() throws ODataJPAModelException {
    final String uri = MEASURES_V1_URL;
    final IntermediateReferenceAccess ref = cut.addReference(uri, "annotations/Org.OData.Measures.V1.xml");
    assertThrows(ODataJPAModelException.class, () -> ref.addIncludeAnnotation(null));
  }

  @Test
  public void checkThrowsExcpetionOnEmptyTermNamespace() throws ODataJPAModelException {
    final String uri = MEASURES_V1_URL;
    final IntermediateReferenceAccess ref = cut.addReference(uri, "annotations/Org.OData.Measures.V1.xml");
    assertThrows(ODataJPAModelException.class, () -> ref.addIncludeAnnotation(""));
  }

  class PostProcessor extends JPAEdmMetadataPostProcessor {

    @Override
    public void processNavigationProperty(final IntermediateNavigationPropertyAccess property,
        final String jpaManagedTypeClassName) {}

    @Override
    public void processProperty(final IntermediatePropertyAccess property, final String jpaManagedTypeClassName) {}

    @Override
    public void processEntityType(final IntermediateEntityTypeAccess entity) {}

    @Override
    public void provideReferences(final IntermediateReferenceList references) throws ODataJPAModelException {
      final String uri = MEASURES_V1_URL;
      final IntermediateReferenceAccess reference = references.addReference(uri,
          "annotations/Org.OData.Measures.V1.xml");
      reference.addInclude("Org.OData.Core.V1", "Core");
      reference.addIncludeAnnotation("Org.OData.Core.V1");
    }
  }
}
