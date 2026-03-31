package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlNamed;
import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.commons.api.edm.provider.CsdlTerm;
import org.apache.olingo.commons.api.edmx.EdmxReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.sap.olingo.jpa.metadata.api.JPAEdmMetadataPostProcessor;
import com.sap.olingo.jpa.metadata.core.edm.extension.vocabularies.Applicability;
import com.sap.olingo.jpa.metadata.core.edm.extension.vocabularies.ODataVocabularyReadException;
import com.sap.olingo.jpa.metadata.core.edm.extension.vocabularies.ReferenceAccess;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAServiceDocument;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extension.IntermediateEntityTypeAccess;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extension.IntermediateNavigationPropertyAccess;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extension.IntermediatePropertyAccess;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extension.IntermediateReferenceList;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extension.IntermediateReferenceList.IntermediateReferenceAccess;

class IntermediateReferencesTest extends TestMappingRoot {

  private static final String TEST_V1_PATH = "annotations/Org.Olingo.Test.V1.xml";
  private static final String MEASURES_V1_PATH = "annotations/Org.OData.Measures.V1.xml";
  private static final String CORE_V1_PATH = "annotations/Org.OData.Core.V1.xml";
  private static final String TEST_V1_URL =
      "http://org.example/odata/odata/v4.0/os/vocabularies/Org.Olingo.Test.V1.xml";
  private static final String MEASURES_V1_URL =
      "https://oasis-tcs.github.io/odata-vocabularies/vocabularies/Org.OData.Measures.V1.xml";
  private static final String CORE_V1_URL =
      "https://oasis-tcs.github.io/odata-vocabularies/vocabularies/Org.OData.Core.V1.xml";
  private IntermediateReferences cut;

  @BeforeEach
  void setup() {
    cut = new IntermediateReferences();
  }

  @Disabled("This test may not run because of proxy setting problems!! -> find alternative for Integration tests")
  @Test
  void checkAddOnlyURI() throws ODataJPAModelException {
    final String uri = CORE_V1_URL;
    cut.addReference(uri);
    final List<EdmxReference> act = cut.getEdmReferences();
    assertEquals(1, act.size());
    assertEquals(act.get(0).getUri().toString(), uri);
  }

  @Test
  void checkThrowsExceptionOnEmptyPath() {

    assertThrows(ODataJPAModelException.class, () -> cut.addReference(CORE_V1_URL, ""));
  }

  @Test
  void checkAddURIAndPath() throws ODataJPAModelException {
    final String uri = MEASURES_V1_URL;
    final String path = MEASURES_V1_PATH;
    cut.addReference(uri, path);
    final List<EdmxReference> act = cut.getEdmReferences();
    assertEquals(1, act.size());
    assertEquals(uri, act.get(0).getUri().toString());
    assertEquals(path, ((IntermediateReferenceList.IntermediateReferenceAccess) cut.references.get(0)).getPath());
    assertEquals(uri, ((IntermediateReferenceList.IntermediateReferenceAccess) cut.references.get(0)).getURI()
        .toString());
  }

  @Test
  void checkConvertedToCsdlContainsInclude() throws ODataJPAModelException {
    JPAServiceDocument serviceDocument;
    serviceDocument = new IntermediateServiceDocument(PUNIT_NAME, emf.getMetamodel(), new PostProcessor(), null,
        Collections.emptyList(), true);
    assertEquals(1, serviceDocument.getReferences().size());

    final EdmxReference reference = serviceDocument.getReferences().get(0);
    assertEquals(1, reference.getIncludes().size());
  }

  @Test
  void checkConvertedToCsdlContainsIncludeAnnotation() throws ODataJPAModelException {
    JPAServiceDocument serviceDocument;
    serviceDocument = new IntermediateServiceDocument(PUNIT_NAME, emf.getMetamodel(), new PostProcessor(), null,
        Collections.emptyList(), true);
    assertEquals(1, serviceDocument.getReferences().size());

    final EdmxReference reference = serviceDocument.getReferences().get(0);
    assertEquals(1, reference.getIncludeAnnotations().size());
  }

  @Test
  void checkGetOneSchema() throws ODataJPAModelException {
    final String uri = MEASURES_V1_URL;
    final IntermediateReferenceAccess reference = cut.addReference(uri, MEASURES_V1_PATH);
    reference.addInclude("Org.OData.Measures.V1", "");

    assertEquals(1, cut.getSchemas().size());
  }

  @Test
  void checkGetTwoSchemas() throws ODataJPAModelException {
    final String uri = TEST_V1_URL;
    final IntermediateReferenceAccess reference = cut.addReference(uri, TEST_V1_PATH);
    reference.addInclude("Org.Olingo.Test.V1.xml", "");

    assertEquals(2, cut.getSchemas().size());
  }

  @Test
  void checkGetComplexType() throws ODataJPAModelException {
    final String uri = TEST_V1_URL;
    final IntermediateReferenceAccess reference = cut.addReference(uri, TEST_V1_PATH);
    reference.addInclude("Org.Olingo.Test.V1.xml", "");

    for (final CsdlSchema schema : cut.getSchemas())
      if (schema.getNamespace().equals("Org.OData.Capabilities.V1")) {
        assertNotNull(schema.getComplexType("UpdateRestrictionsType"));
        return;
      }
    fail();
  }

  @Test
  void checkGetTermByNamespace() throws ODataJPAModelException {
    final String uri = MEASURES_V1_URL;
    final IntermediateReferenceAccess reference = cut.addReference(uri, MEASURES_V1_PATH);
    reference.addInclude("Org.OData.Measures.V1", "");
    final FullQualifiedName fqn = new FullQualifiedName("Org.OData.Measures.V1", "ISOCurrency");
    assertNotNull(cut.getTerm(fqn));
  }

  @Test
  void checkGetTermByAlias() throws ODataJPAModelException {
    final String uri = MEASURES_V1_URL;
    final IntermediateReferenceAccess reference = cut.addReference(uri, MEASURES_V1_PATH);
    reference.addInclude("Org.OData.Measures.V1", "Measures");
    final FullQualifiedName fqn = new FullQualifiedName("Measures", "ISOCurrency");
    assertNotNull(cut.getTerm(fqn));
  }

  @Test
  void checkReturnEmptyOptionalUnknownTerm() throws ODataJPAModelException {
    final String uri = MEASURES_V1_URL;
    final IntermediateReferenceAccess reference = cut.addReference(uri, MEASURES_V1_PATH);
    reference.addInclude("Org.OData.Measures.V1", "Measures");
    final FullQualifiedName fqn = new FullQualifiedName("Measures", "Dummy");
    assertFalse(cut.getTerm(fqn).isPresent());
  }

  @Test
  void checkReturnEmptyOptionalOnUnknownNamespace() throws ODataJPAModelException {
    final String uri = MEASURES_V1_URL;
    final IntermediateReferenceAccess reference = cut.addReference(uri, MEASURES_V1_PATH);
    reference.addInclude("Org.OData.Measures.V1", "Measures");
    final FullQualifiedName fqn = new FullQualifiedName("Dummy", "ISOCurrency");
    assertFalse(cut.getTerm(fqn).isPresent());
  }

  @Test
  void checkAddIncludeWithoutNameSpace() throws ODataJPAModelException {
    final String uri = MEASURES_V1_URL;
    final IntermediateReferenceAccess reference = cut.addReference(uri, MEASURES_V1_PATH);
    reference.addInclude("Org.OData.Org.OData.Core.V1");
    assertEquals(1, cut.aliasDirectory.size());
  }

  @Test
  void checkAddIncludeWithNameSpace() throws ODataJPAModelException {
    final String uri = MEASURES_V1_URL;
    final IntermediateReferenceAccess reference = cut.addReference(uri, MEASURES_V1_PATH);
    reference.addInclude("Org.OData.Org.OData.Core.V1", "Core");
    assertEquals(2, cut.aliasDirectory.size());
  }

  @Test
  void checkThrowsExceptionOnNullTermNamespace() throws ODataJPAModelException {

    final String uri = MEASURES_V1_URL;
    final IntermediateReferenceAccess reference = cut.addReference(uri, MEASURES_V1_PATH);
    assertThrows(ODataJPAModelException.class, () -> reference.addIncludeAnnotation(null));
  }

  @Test
  void checkThrowsExceptionOnEmptyTermNamespace() throws ODataJPAModelException {
    final String uri = MEASURES_V1_URL;
    final IntermediateReferenceAccess reference = cut.addReference(uri, MEASURES_V1_PATH);
    assertThrows(ODataJPAModelException.class, () -> reference.addIncludeAnnotation(""));
  }

  @Test
  void checkGetTerms() throws ODataJPAModelException {

    final IntermediateReferenceAccess reference = cut.addReference(CORE_V1_URL, CORE_V1_PATH);
    reference.addInclude("Org.OData.Core.V1", "Core");
    final List<CsdlTerm> act = cut.getTerms("Core", Applicability.ENTITY_SET);
    assertNotNull(act);
    assertEquals(6, act.size());

    assertTrue(act.stream().filter(term -> term.getName().equals("ResourcePath")).findFirst().isPresent());
    assertTrue(act.stream().filter(term -> term.getName().equals("OptimisticConcurrency")).findFirst().isPresent());
  }

  private static Stream<Arguments> getTypeFqn() {
    return Stream.of(
        Arguments.of(new FullQualifiedName("Org.OData.Core.V1", "RevisionType"), "Complex type via namespace"),
        Arguments.of(new FullQualifiedName("Core", "RevisionType"), "Complex type via alice"),
        Arguments.of(new FullQualifiedName("Org.OData.Core.V1", "Tag"), "Simple type via namespace"),
        Arguments.of(new FullQualifiedName("Core", "Tag"), "Simple type via alice"),
        Arguments.of(new FullQualifiedName("Org.OData.Core.V1", "Permission"), "Simple type via namespace"),
        Arguments.of(new FullQualifiedName("Core", "Permission"), "Simple type via alice"),
        Arguments.of(new FullQualifiedName("Org.OData.Core.V1", "RevisionKind"), "Enumeration type via namespace"),
        Arguments.of(new FullQualifiedName("Core", "RevisionKind"), "Enumeration type via alice"));
  }

  @ParameterizedTest
  @MethodSource("getTypeFqn")
  void checkGetType(final FullQualifiedName fqn, final String testDescription)
      throws ODataJPAModelException {

    cut.addReference(CORE_V1_URL, CORE_V1_PATH);
    final Optional<CsdlNamed> act = cut.getType(fqn);
    assertNotNull(act);
    assertTrue(act.isPresent(), testDescription);
  }

  @Test
  void checkAddReferenceWithURI() throws URISyntaxException, ODataVocabularyReadException {
    final URI uri = new URI(CORE_V1_URL);
    final ReferenceAccess act = cut.addReference(uri, CORE_V1_PATH);
    assertNotNull(act);
  }

  @Test
  void checkAddReferenceWithURIRethrowsException() throws URISyntaxException {
    final URI uri = new URI(CORE_V1_URL);
    final ODataVocabularyReadException act = assertThrows(ODataVocabularyReadException.class, () -> cut.addReference(
        uri, "Test"));
    assertNotNull(act.getCause());
  }

  class PostProcessor implements JPAEdmMetadataPostProcessor {

    @Override
    public void processNavigationProperty(final IntermediateNavigationPropertyAccess property,
        final String jpaManagedTypeClassName) {
      // Not needed
    }

    @Override
    public void processProperty(final IntermediatePropertyAccess property, final String jpaManagedTypeClassName) {
      // Not needed
    }

    @Override
    public void processEntityType(final IntermediateEntityTypeAccess entity) {
      // Not needed
    }

    @Override
    public void provideReferences(final IntermediateReferenceList references) throws ODataJPAModelException {
      final String uri = MEASURES_V1_URL;
      final IntermediateReferenceAccess reference = references.addReference(uri,
          MEASURES_V1_PATH);
      reference.addInclude("Org.OData.Core.V1", "Core");
      reference.addIncludeAnnotation("Org.OData.Core.V1");
    }
  }
}
