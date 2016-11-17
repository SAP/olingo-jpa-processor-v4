package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.net.URISyntaxException;
import java.util.List;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edmx.EdmxReference;
import org.junit.Before;
import org.junit.Test;

import com.sap.olingo.jpa.metadata.api.JPAEdmMetadataPostProcessor;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extention.IntermediateNavigationPropertyAccess;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extention.IntermediatePropertyAccess;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extention.IntermediateReferenceList;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extention.IntermediateReferenceList.IntermediateReferenceAccess;

public class TestIntermediateReferences extends TestMappingRoot {
  private IntermediateReferences cut;

  @Before
  public void setup() throws ODataJPAModelException {
    cut = new IntermediateReferences();
  }

  @Test
  public void checkAddOnlyURI() throws ODataJPAModelException, URISyntaxException {
    String uri = "http://docs.oasisopen.org/odata/odata/v4.0/os/vocabularies/Org.OData.Core.V1.xml";
    cut.addReference(uri);
    List<EdmxReference> act = cut.getEdmReferences();
    assertEquals(act.size(), 1);
    assertEquals(act.get(0).getUri().toString(), uri);
  }

  @Test
  public void checkAddURIandPath() throws ODataJPAModelException, URISyntaxException {
    String uri = "http://docs.oasisopen.org/odata/odata/v4.0/os/vocabularies/Org.OData.Measures.V1.xml";
    cut.addReference(uri, "annotations/Org.OData.Measures.V1.xml");
    List<EdmxReference> act = cut.getEdmReferences();
    assertEquals(1, act.size());
    assertEquals(uri, act.get(0).getUri().toString());
  }

  @Test
  public void checkConvertedToEdmx() throws ODataJPAModelException {
    ServiceDocument serviceDocument;
    serviceDocument = new ServiceDocument(PUNIT_NAME, emf.getMetamodel(), new PostProcessor());
    assertEquals(1, serviceDocument.getReferences().size());

    EdmxReference ref = serviceDocument.getReferences().get(0);
    assertEquals(1, ref.getIncludes().size());
  }

  @Test
  public void checkGetTermByNamespace() throws ODataJPAModelException {
    String uri = "http://docs.oasisopen.org/odata/odata/v4.0/os/vocabularies/Org.OData.Measures.V1.xml";
    IntermediateReferenceAccess ref = cut.addReference(uri, "annotations/Org.OData.Measures.V1.xml");
    ref.addInclude("Org.OData.Measures.V1", "");
    FullQualifiedName fqn = new FullQualifiedName("Org.OData.Measures.V1", "ISOCurrency");
    assertNotNull(cut.getTerm(fqn));
  }

  @Test
  public void checkGetTermByAlias() throws ODataJPAModelException {
    String uri = "http://docs.oasisopen.org/odata/odata/v4.0/os/vocabularies/Org.OData.Measures.V1.xml";
    IntermediateReferenceAccess ref = cut.addReference(uri, "annotations/Org.OData.Measures.V1.xml");
    ref.addInclude("Org.OData.Measures.V1", "Measures");
    FullQualifiedName fqn = new FullQualifiedName("Measures", "ISOCurrency");
    assertNotNull(cut.getTerm(fqn));
  }

  class PostProcessor extends JPAEdmMetadataPostProcessor {
    @Override
    public void processNavigationProperty(final IntermediateNavigationPropertyAccess property,
        final String jpaManagedTypeClassName) {

    }

    @Override
    public void processProperty(final IntermediatePropertyAccess property, final String jpaManagedTypeClassName) {

    }

    @Override
    public void provideReferences(final IntermediateReferenceList references) throws ODataJPAModelException {
      String uri = "http://docs.oasisopen.org/odata/odata/v4.0/os/vocabularies/Org.OData.Measures.V1.xml";
      IntermediateReferenceAccess reference = references.addReference(uri, "annotations/Org.OData.Measures.V1.xml");
      reference.addInclude("Org.OData.Core.V1", "Core");
    }
  }
}
