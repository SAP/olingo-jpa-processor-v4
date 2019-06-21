package com.sap.olingo.jpa.metadata.core.edm.mapper.annotation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.olingo.commons.api.edm.provider.CsdlComplexType;
import org.apache.olingo.commons.api.edm.provider.CsdlEnumType;
import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.commons.api.edm.provider.CsdlTerm;
import org.apache.olingo.commons.api.edm.provider.CsdlTypeDefinition;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

/**
 * http://docs.oasis-open.org/odata/ns/edmx
 * @author Oliver Grande
 *
 */

@JsonIgnoreProperties(ignoreUnknown = true)
class Schema {
  @JacksonXmlProperty(isAttribute = true)
  private String xmlns;

  @JacksonXmlProperty(isAttribute = true, localName = "Namespace")
  private String namespace;

  @JacksonXmlProperty(isAttribute = true, localName = "Alias")
  private String alias;

  @JacksonXmlProperty(localName = "EnumType")
  private List<EnumType> enumerations = new ArrayList<>();

  @JacksonXmlProperty(localName = "TypeDefinition")
  private List<TypeDefinition> typeDefinitions = new ArrayList<>();

  @JacksonXmlProperty(localName = "ComplexType")
  private List<ComplexType> complexTypes = new ArrayList<>();

  @JacksonXmlProperty(localName = "Term")
  private List<Term> terms = new ArrayList<>();

  CsdlSchema asCsdlSchema() {
    CsdlSchema csdlSchema = new CsdlSchema();
    csdlSchema.setAlias(alias);
    csdlSchema.setNamespace(namespace);
    csdlSchema.setTerms(asCsdlTerms());
    csdlSchema.setEnumTypes(asEnumTypes());
    csdlSchema.setComplexTypes(asComplexTypes());
    csdlSchema.setTypeDefinitions(asTypeDefinitions());
    return csdlSchema;
  }

  String getAlias() {
    return alias;
  }

  ComplexType getComplexType(String name) {

    for (ComplexType c : complexTypes) {
      if (c.getName().equals(name)) {
        return c;
      }
    }
    return null;
  }

  List<ComplexType> getComplexTypes() {
    return Collections.unmodifiableList(complexTypes);
  }

  EnumType getEnumType(String name) {
    for (EnumType e : enumerations) {
      if (e.getName().equals(name)) {
        return e;
      }
    }
    return null;
  }

  List<EnumType> getEnumTypes() {
    return Collections.unmodifiableList(enumerations);
  }

  String getNamespace() {
    return namespace;
  }

  Term getTerm(String name) {
    for (Term t : terms) {
      if (t.getName().equals(name)) {
        return t;
      }
    }
    return null;
  }

  List<Term> getTerms() {
    return Collections.unmodifiableList(terms);
  }

  TypeDefinition getTypeDefinition(String name) {

    for (TypeDefinition e : typeDefinitions) {
      if (e.getName().equals(name)) {
        return e;
      }
    }
    return null;
  }

  List<TypeDefinition> getTypeDefinitions() {
    return Collections.unmodifiableList(typeDefinitions);
  }

  void setAlias(String alias) {
    this.alias = alias;
  }

  @JsonSetter
  void setComplexTypes(ComplexType[] newComplexTypes) {
    complexTypes.addAll(Arrays.asList(newComplexTypes));
  }

  @JsonSetter
  void setEnumerations(EnumType enumeration) {
    this.enumerations.add(enumeration);
  }

  void setNamespace(String namespace) {
    this.namespace = namespace;
  }

  @JsonSetter
  void setTerms(Term[] newTerms) {
    terms.addAll(Arrays.asList(newTerms));
  }

  @JsonSetter
  void setTypeDefinitions(TypeDefinition typeDefinition) {
    this.typeDefinitions.add(typeDefinition);
  }

  private List<CsdlComplexType> asComplexTypes() {
    return Collections.unmodifiableList(complexTypes);
  }

  private List<CsdlTerm> asCsdlTerms() {
    return Collections.unmodifiableList(terms);
  }

  private List<CsdlEnumType> asEnumTypes() {
    return Collections.unmodifiableList(enumerations);
  }

  private List<CsdlTypeDefinition> asTypeDefinitions() {
    return Collections.unmodifiableList(typeDefinitions);
  }
}
