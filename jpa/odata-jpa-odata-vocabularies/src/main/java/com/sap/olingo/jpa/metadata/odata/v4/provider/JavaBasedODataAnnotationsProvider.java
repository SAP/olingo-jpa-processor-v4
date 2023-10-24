package com.sap.olingo.jpa.metadata.odata.v4.provider;

import static java.util.Objects.requireNonNull;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Objects;

import javax.annotation.Nonnull;

import org.apache.olingo.commons.api.edm.provider.CsdlAnnotation;

import com.sap.olingo.jpa.metadata.core.edm.extension.vocabularies.AnnotationProvider;
import com.sap.olingo.jpa.metadata.core.edm.extension.vocabularies.Applicability;
import com.sap.olingo.jpa.metadata.core.edm.extension.vocabularies.JPAReferences;
import com.sap.olingo.jpa.metadata.core.edm.extension.vocabularies.ODataAnnotatable;
import com.sap.olingo.jpa.metadata.core.edm.extension.vocabularies.ODataVocabularyReadException;
import com.sap.olingo.jpa.metadata.core.edm.extension.vocabularies.ReferenceList;

abstract class JavaBasedODataAnnotationsProvider implements AnnotationProvider {

  final JavaAnnotationConverter converter;
  final String packageName;

  JavaBasedODataAnnotationsProvider(final JavaAnnotationConverter converter, final String name) {
    this.converter = converter;
    this.packageName = name;
  }

  @Override
  public Collection<CsdlAnnotation> getAnnotations(@Nonnull final Applicability appliesTo,
      final ODataAnnotatable annotatable, final JPAReferences references) {

    final var annotations = requireNonNull(annotatable).javaAnnotations(packageName);

    return requireNonNull(references).getTerms(getAlias(), requireNonNull(appliesTo)).stream()
        .map(term -> annotations.get(term.getName()))
        .filter(Objects::nonNull)
        .map(a -> converter.convert(references, a, annotatable))
        .map(converted -> converted.orElse(null))
        .filter(Objects::nonNull)
        .toList();
  }

  @Override
  public void addReferences(final ReferenceList references) throws ODataVocabularyReadException {

    try {
      final var uri = getUri();
      final var reference = references.addReference(uri, getPath());
      reference.addInclude(getNameSpace(), getAlias());
    } catch (final URISyntaxException e) {
      throw new ODataVocabularyReadException(getAlias(), getPath(), e);
    }
  }

  abstract String getAlias();

  abstract URI getUri() throws URISyntaxException;

  abstract String getNameSpace();

  abstract String getPath();

}
