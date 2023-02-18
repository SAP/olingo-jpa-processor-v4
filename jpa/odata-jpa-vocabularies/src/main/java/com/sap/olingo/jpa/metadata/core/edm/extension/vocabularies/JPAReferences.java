package com.sap.olingo.jpa.metadata.core.edm.extension.vocabularies;

import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlNamed;
import org.apache.olingo.commons.api.edm.provider.CsdlTerm;

public interface JPAReferences {

  public String convertAlias(@Nonnull final String alias);

  public Optional<CsdlTerm> getTerm(@Nonnull final FullQualifiedName termName);

  public <T extends CsdlNamed> Optional<T> getType(@Nonnull final FullQualifiedName fqn);

  public List<CsdlTerm> getTerms(@Nonnull String schemaAlias, @Nonnull AppliesTo appliesTo);

}
