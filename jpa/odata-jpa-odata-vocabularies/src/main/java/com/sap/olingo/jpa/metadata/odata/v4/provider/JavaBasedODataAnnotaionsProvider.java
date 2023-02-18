package com.sap.olingo.jpa.metadata.odata.v4.provider;

import static java.util.Objects.requireNonNull;

import java.lang.annotation.Annotation;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import org.apache.olingo.commons.api.edm.provider.CsdlAnnotation;

import com.sap.olingo.jpa.metadata.core.edm.extension.vocabularies.AnnotationProvider;
import com.sap.olingo.jpa.metadata.core.edm.extension.vocabularies.AppliesTo;
import com.sap.olingo.jpa.metadata.core.edm.extension.vocabularies.JPAAnnotatable;
import com.sap.olingo.jpa.metadata.core.edm.extension.vocabularies.JPAReferences;
import com.sap.olingo.jpa.metadata.core.edm.extension.vocabularies.ODataVocabularyReadException;
import com.sap.olingo.jpa.metadata.core.edm.extension.vocabularies.ReferenceAccess;
import com.sap.olingo.jpa.metadata.core.edm.extension.vocabularies.ReferenceList;
import com.sap.olingo.jpa.metadata.odata.v4.capabilities.terms.FilterExpressionType;

public class JavaBasedODataAnnotaionsProvider implements AnnotationProvider {

  private static final String CAPABILITIES_NAMESPACE = "Org.OData.Capabilities.V1";
  private static final String CAPABILITIES_ALIAS = "Capabilities";
  private static final String CAPABILITIES_PATH = "vocabularies/Org.OData.Capabilities.V1.xml";

  private final JavaAnnotationConverter converter;
  private final String capabilitiesPackageName;

  public JavaBasedODataAnnotaionsProvider() {
    this(new JavaAnnotationConverter());
  }

  public JavaBasedODataAnnotaionsProvider(final JavaAnnotationConverter converter) {
    this.converter = converter;
    this.capabilitiesPackageName = FilterExpressionType.class.getPackage().getName();
  }

  @Override
  public Collection<CsdlAnnotation> getAnnotations(@Nonnull final AppliesTo appliesTo, final JPAAnnotatable annotatable,
      final JPAReferences references) {

    final Map<String, Annotation> annotations = requireNonNull(annotatable).javaAnnotations(capabilitiesPackageName);

    return requireNonNull(references).getTerms(CAPABILITIES_ALIAS, requireNonNull(appliesTo)).stream()
        .map(t -> annotations.get(t.getName()))
        .filter(Objects::nonNull)
        .map(a -> converter.convert(references, a, annotatable))
        .map(c -> c.orElse(null))
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }

  @Override
  public void addReferences(final ReferenceList references) throws ODataVocabularyReadException {

    try {
      final URI uri = new URI(
          "http://docs.oasisopen.org/odata/odata/v4.0/os/vocabularies/Org.OData.Capabilities.V1.xml"); // NOSONAR
      final ReferenceAccess reference = references.addReference(uri, CAPABILITIES_PATH);
      reference.addInclude(CAPABILITIES_NAMESPACE, CAPABILITIES_ALIAS);
    } catch (final URISyntaxException e) {
      throw new ODataVocabularyReadException(CAPABILITIES_ALIAS, CAPABILITIES_PATH, e);
    }
  }

}
