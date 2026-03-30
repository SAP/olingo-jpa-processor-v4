package com.sap.olingo.jpa.metadata.odata.v4.provider;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlNamed;
import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.commons.api.edm.provider.CsdlTerm;

import com.sap.olingo.jpa.metadata.core.edm.extension.vocabularies.Applicability;
import com.sap.olingo.jpa.metadata.core.edm.extension.vocabularies.JPAReferences;
import com.sap.olingo.jpa.metadata.core.edm.mapper.vocabularies.CsdlDocument;

public class References implements JPAReferences {
  private final CsdlDocument vocabulary;

  public References(final CsdlDocument vocabulary) {
    super();
    this.vocabulary = vocabulary;
  }

  @Override
  public String convertAlias(final String alias) {
    return vocabulary.getSchemas().values().stream()
        .filter(schema -> schema.getAlias().equals(alias))
        .findFirst()
        .map(s -> s.getNamespace())
        .orElse(null);
  }

  @Override
  public Optional<CsdlTerm> getTerm(final FullQualifiedName termName) {

    final Map<String, CsdlTerm> termsMap = getSchema(termName.getNamespace())
        .map(s -> vocabulary.getTerms().get(s.getNamespace()))
        .orElseGet(Collections::emptyMap);

    return termsMap.entrySet().stream()
        .filter(t -> t.getKey().equals(termName.getName()))
        .findFirst()
        .map(Entry::getValue);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T extends CsdlNamed> Optional<T> getType(final FullQualifiedName fqn) {
    // namespace of fqn can either be the namespace of the schema or the alias of the schema
    final Optional<CsdlSchema> schema = getSchema(fqn.getNamespace());

    final Optional<T> complexType = schema.map(s -> (T) s.getComplexType(fqn.getName()));
    if (!complexType.isPresent()) {
      final Optional<T> simpleType = schema.map(s -> (T) s.getTypeDefinition(fqn.getName()));
      if (!simpleType.isPresent())
        return schema.map(s -> (T) s.getEnumType(fqn.getName()));
      return simpleType;
    }
    return complexType;
  }

  Optional<CsdlSchema> getSchema(final String namespace) {
    return Optional.ofNullable(Optional.ofNullable(vocabulary.getSchemas().get(namespace))
        .orElseGet(() -> vocabulary.getSchemas().get(convertAlias(namespace))));
  }

  @Override
  public List<CsdlTerm> getTerms(final String capabilitiesAlias, final Applicability appliesTo) {
    return null;
  }

}
