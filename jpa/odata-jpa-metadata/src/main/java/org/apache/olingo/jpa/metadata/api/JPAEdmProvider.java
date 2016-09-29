package org.apache.olingo.jpa.metadata.api;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;

import javax.persistence.EntityManagerFactory;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlAbstractEdmProvider;
import org.apache.olingo.commons.api.edm.provider.CsdlAnnotations;
import org.apache.olingo.commons.api.edm.provider.CsdlComplexType;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityContainer;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityContainerInfo;
import org.apache.olingo.commons.api.edm.provider.CsdlEntitySet;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.commons.api.edm.provider.CsdlFunction;
import org.apache.olingo.commons.api.edm.provider.CsdlFunctionImport;
import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.commons.api.edm.provider.CsdlTerm;
import org.apache.olingo.commons.api.edmx.EdmxReference;
import org.apache.olingo.commons.api.edmx.EdmxReferenceInclude;
import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAException;
import org.apache.olingo.jpa.metadata.core.edm.mapper.impl.JPAEdmNameBuilder;
import org.apache.olingo.jpa.metadata.core.edm.mapper.impl.ServiceDocument;

public class JPAEdmProvider extends CsdlAbstractEdmProvider {

  final private JPAEdmNameBuilder nameBuilder;
  final private ServiceDocument serviceDocument;

  // TODO edmx: Reference -> Support by Olingo?
  // http://docs.oasis-open.org/odata/odata/v4.0/errata02/os/complete/part3-csdl/odata-v4.0-errata02-os-part3-csdl-complete.html#_Toc406397930
  public JPAEdmProvider(final String namespace, final EntityManagerFactory emf,
      final JPAEdmMetadataPostProcessor postProcessor) throws ODataException {
    super();
    this.nameBuilder = new JPAEdmNameBuilder(namespace);
    serviceDocument = new ServiceDocument(namespace, emf.getMetamodel(), postProcessor);
  }

  @Override
  public CsdlComplexType getComplexType(final FullQualifiedName complexTypeName) throws ODataException {
    for (final CsdlSchema schema : serviceDocument.getEdmSchemas()) {
      if (schema.getNamespace().equals(complexTypeName.getNamespace())) {
        return schema.getComplexType(complexTypeName.getName());
      }
    }
    return null;
  }

  @Override
  public CsdlEntityContainer getEntityContainer() throws ODataException {
    return serviceDocument.getEdmEntityContainer();
  }

  @Override
  public CsdlEntityContainerInfo getEntityContainerInfo(final FullQualifiedName entityContainerName)
      throws ODataException {
    // This method is invoked when displaying the Service Document at e.g.
    // .../DemoService.svc
    if (entityContainerName == null
        || entityContainerName.equals(nameBuilder.buildFQN(nameBuilder.buildContainerName()))) {
      final CsdlEntityContainerInfo entityContainerInfo = new CsdlEntityContainerInfo();
      entityContainerInfo.setContainerName(nameBuilder.buildFQN(nameBuilder.buildContainerName()));
      return entityContainerInfo;
    }
    return null;
  }

  @Override
  public CsdlEntitySet getEntitySet(final FullQualifiedName entityContainerFQN, final String entitySetName)
      throws ODataException {
    final CsdlEntityContainer container = serviceDocument.getEdmEntityContainer();
    if (entityContainerFQN.equals(nameBuilder.buildFQN(container.getName()))) {
      return container.getEntitySet(entitySetName);
    }
    return null;
  }

  @Override
  public CsdlEntityType getEntityType(final FullQualifiedName entityTypeName) throws ODataException {

    for (final CsdlSchema schema : serviceDocument.getEdmSchemas()) {
      if (schema.getNamespace().equals(entityTypeName.getNamespace())) {
        return schema.getEntityType(entityTypeName.getName());
      }
    }
    return null;
  }

  @Override
  public CsdlFunctionImport getFunctionImport(final FullQualifiedName entityContainerFQN,
      final String functionImportName)
      throws ODataException {
    final CsdlEntityContainer container = serviceDocument.getEdmEntityContainer();
    if (entityContainerFQN.equals(nameBuilder.buildFQN(container.getName()))) {
      return container.getFunctionImport(functionImportName);
    }
    return null;
  }

  @Override
  public List<CsdlFunction> getFunctions(final FullQualifiedName functionName) throws ODataException {
    for (final CsdlSchema schema : serviceDocument.getEdmSchemas()) {
      if (schema.getNamespace().equals(functionName.getNamespace())) {
        return schema.getFunctions(functionName.getName());
      }
    }
    return null;
  }

  @Override
  public CsdlAnnotations getAnnotationsGroup(final FullQualifiedName targetName, String qualifier)
      throws ODataException {
    return null;
  }

  @Override
  public CsdlTerm getTerm(final FullQualifiedName termName) throws ODataException {
//    <Term Name="IsLanguageDependent" Type="Core.Tag" DefaultValue="true" AppliesTo="Term Property">
//      <Annotation Term="Core.Description" String="Properties and terms annotated with this term are language-dependent" />
//      <Annotation Term="Core.RequiresType" String="Edm.String" />
//    </Term>

    CsdlTerm t = new CsdlTerm();
    List<String> a = new ArrayList<String>();
    a.add("Term Property");
    t.setName("IsLanguageDependent");
    t.setType("Core.Tag");
    t.setAppliesTo(a);
    return t;
  }

  @Override
  public List<CsdlSchema> getSchemas() throws ODataException {
    return serviceDocument.getEdmSchemas();
  }

  public final ServiceDocument getServiceDocument() {
    return serviceDocument;
  }

  public void setRequestLocales(final Enumeration<Locale> locales) {
    ODataJPAException.setLocales(locales);
  }

  public List<EdmxReference> getReferences() {
    List<EdmxReference> references = new ArrayList<EdmxReference>();
//    <edmx:Reference Uri="http://docs.oasisopen.org/odata/odata/v4.0/os/vocabularies/Org.OData.Core.V1.xml">
//      <edmx:Include Namespace="Org.OData.Core.V1" Alias="Core" />
//    </edmx:Reference>  
    try {
      EdmxReference r = new EdmxReference(new URI(
          "http://docs.oasisopen.org/odata/odata/v4.0/os/vocabularies/Org.OData.Core.V1.xml"));
      EdmxReferenceInclude i = new EdmxReferenceInclude("Org.OData.Core.V1", "Core");
      r.addInclude(i);
      references.add(r);
    } catch (URISyntaxException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    return references;
  }
}
