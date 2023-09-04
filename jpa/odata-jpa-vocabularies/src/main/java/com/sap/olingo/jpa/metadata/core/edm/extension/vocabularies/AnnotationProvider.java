package com.sap.olingo.jpa.metadata.core.edm.extension.vocabularies;

import java.util.Collection;

import javax.annotation.Nonnull;

import org.apache.olingo.commons.api.edm.provider.CsdlAnnotation;

public interface AnnotationProvider {

  Collection<CsdlAnnotation> getAnnotations(@Nonnull final Applicability appliesTo,
      @Nonnull ODataAnnotatable annotatable, @Nonnull JPAReferences references);

  void addReferences(@Nonnull ReferenceList references) throws ODataVocabularyReadException;

}
