package com.sap.olingo.jpa.metadata.core.edm.mapper.annotation;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessOrder;
import javax.xml.bind.annotation.XmlAccessorOrder;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

@XmlAccessorOrder(XmlAccessOrder.UNDEFINED)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Schema {
  @JacksonXmlProperty(isAttribute = true)
  private String xmlns;

  @JacksonXmlProperty(isAttribute = true, localName = "Namespace")
  private String namespace;

  @JacksonXmlProperty(isAttribute = true, localName = "Alias")
  private String alias;

  @JacksonXmlProperty(localName = "Term")
  private ArrayList<Term> terms = new ArrayList<Term>();

  @JacksonXmlProperty(localName = "EnumType")
  private ArrayList<EnumType> enumerations = new ArrayList<EnumType>();

  @JacksonXmlProperty(localName = "TypeDefinition")
  private ArrayList<TypeDefinition> typeDefinitions = new ArrayList<TypeDefinition>();

  List<Term> getTerms() {
    return terms;
  }

  String getNamespace() {
    return namespace;
  }

  String getAlias() {
    return alias;
  }

  void setTerms(Term[] newTerms) {
    for (Term t : newTerms) {
      terms.add(t);
    }
  }

  ArrayList<EnumType> getEnumerations() {
    return enumerations;
  }

  void setEnumerations(ArrayList<EnumType> enumerations) {
    this.enumerations = enumerations;
  }

  ArrayList<TypeDefinition> getTypeDefinitions() {
    return typeDefinitions;
  }

  void setTypeDefinitions(ArrayList<TypeDefinition> typeDefinitions) {
    this.typeDefinitions = typeDefinitions;
  }
}
