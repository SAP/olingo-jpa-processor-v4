package com.sap.olingo.jpa.metadata.core.edm.mapper.util;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlAnnotation;
import org.apache.olingo.commons.api.edm.provider.CsdlComplexType;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlTerm;
import org.apache.olingo.commons.api.edm.provider.CsdlTypeDefinition;

import com.sap.olingo.jpa.metadata.core.edm.extension.vocabularies.JPAReferences;

public final class AnnotationTestHelper {

  private AnnotationTestHelper() {
    super();
  }

  public static CsdlTerm addTermToCoreReferences(final JPAReferences reference, final String term, final String type,
      final CsdlTypeDefinition typeDefintion) {

    final var csdlTerm = mock(CsdlTerm.class);
    when(reference.getTerm(new FullQualifiedName("Org.OData.Core.V1", term)))
        .thenReturn(Optional.of(csdlTerm));
    when(reference.getType(new FullQualifiedName("Core", type)))
        .thenReturn(Optional.of(typeDefintion));

    when(csdlTerm.getName()).thenReturn(term);
    when(csdlTerm.getType()).thenReturn("Core." + type);
    return csdlTerm;
  }

  public static CsdlTerm addTermToCoreReferences(final JPAReferences reference, final String term,
      final String type, final List<CsdlProperty> properties) {

    final var termFilter = mock(CsdlTerm.class);
    final var complexType = new CsdlComplexType();
    complexType.setProperties(properties);
    when(reference.getTerm(new FullQualifiedName("Org.OData.Core.V1", term)))
        .thenReturn(Optional.of(termFilter));
    when(reference.getType(new FullQualifiedName("Core", type)))
        .thenReturn(Optional.of(complexType));

    when(termFilter.getName()).thenReturn(term);
    when(termFilter.getType()).thenReturn("Core." + type);
    return termFilter;
  }

  public static CsdlTerm addTermToCapabilitiesReferences(final JPAReferences reference, final String term,
      final String type, final List<CsdlProperty> properties) {

    final var termFilter = mock(CsdlTerm.class);
    final var complexType = new CsdlComplexType();
    complexType.setProperties(properties);
    when(reference.getTerm(new FullQualifiedName("Org.OData.Capabilities.V1", term)))
        .thenReturn(Optional.of(termFilter));
    when(reference.getType(new FullQualifiedName("Capabilities", type)))
        .thenReturn(Optional.of(complexType));

    when(termFilter.getName()).thenReturn(term);
    when(termFilter.getType()).thenReturn("Capabilities." + type);
    return termFilter;
  }

  public static CsdlAnnotation createCoreAnnotation(final String term) {
    final var annotation = mock(CsdlAnnotation.class);
    when(annotation.getTerm()).thenReturn("Org.OData.Core.V1." + term);
    return annotation;
  }

  public static CsdlAnnotation createCapabilitiesAnnotation(final String term) {
    final var annotation = mock(CsdlAnnotation.class);
    when(annotation.getTerm()).thenReturn("Org.OData.Capabilities.V1." + term);
    return annotation;
  }

  public static CsdlProperty createTermProperty(final String name, final String type) {
    final var property = mock(CsdlProperty.class);
    when(property.getType()).thenReturn(type);
    when(property.isCollection()).thenReturn(false);
    when(property.getName()).thenReturn(name);
    return property;
  }

  public static CsdlProperty createTermCollectionProperty(final String name, final String type) {
    final var property = mock(CsdlProperty.class);
    when(property.getType()).thenReturn(type);
    when(property.isCollection()).thenReturn(true);
    when(property.getName()).thenReturn(name);
    return property;
  }

}
