package com.sap.olingo.jpa.metadata.core.edm.mapper.annotation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessOrder;
import javax.xml.bind.annotation.XmlAccessorOrder;

import org.apache.olingo.commons.api.edm.provider.CsdlComplexType;
import org.apache.olingo.commons.api.edm.provider.CsdlEnumType;
import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.commons.api.edm.provider.CsdlTerm;
import org.apache.olingo.commons.api.edm.provider.CsdlTypeDefinition;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

@XmlAccessorOrder(XmlAccessOrder.UNDEFINED)
@JsonIgnoreProperties(ignoreUnknown = true)
class Schema {
  @JacksonXmlProperty(isAttribute = true)
  private String xmlns;

  @JacksonXmlProperty(isAttribute = true, localName = "Namespace")
  private String namespace;

  @JacksonXmlProperty(isAttribute = true, localName = "Alias")
  private String alias;

  @JacksonXmlProperty(localName = "EnumType")
  private List<EnumType> enumerations = new ArrayList<EnumType>();

  @JacksonXmlProperty(localName = "TypeDefinition")
  private List<TypeDefinition> typeDefinitions = new ArrayList<TypeDefinition>();
//
//  @JacksonXmlProperty(localName = "EntityType")
//  private List<EntityType> entityTypes = new ArrayList<EntityType>();
//
  @JacksonXmlProperty(localName = "ComplexType")
  private List<ComplexType> complexTypes = new ArrayList<ComplexType>();

  @JacksonXmlProperty(localName = "Term")
  private List<Term> terms = new ArrayList<Term>();

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

  private List<CsdlComplexType> asComplexTypes() {
    List<CsdlComplexType> csdlComplexType = new ArrayList<CsdlComplexType>();

    for (ComplexType c : complexTypes) {
      csdlComplexType.add(c);
    }
    return Collections.unmodifiableList(csdlComplexType);
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

  private List<CsdlEnumType> asEnumTypes() {
    List<CsdlEnumType> csdlEnumType = new ArrayList<CsdlEnumType>();

    for (EnumType e : enumerations) {
      csdlEnumType.add(e);
    }
    return Collections.unmodifiableList(csdlEnumType);
  }

  String getNamespace() {
    return namespace;
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

  private List<CsdlTypeDefinition> asTypeDefinitions() {
    List<CsdlTypeDefinition> csdlTypeDefinion = new ArrayList<CsdlTypeDefinition>();

    for (TypeDefinition t : typeDefinitions) {
      csdlTypeDefinion.add(t);
    }
    return Collections.unmodifiableList(csdlTypeDefinion);
  }

  void setAlias(String alias) {
    this.alias = alias;
  }

  void setNamespace(String namespace) {
    this.namespace = namespace;
  }

  /*
   * &lt;element name="Action" type="{http://docs.oasis-open.org/odata/ns/edm}TAction"/>
   * &lt;element name="Function" type="{http://docs.oasis-open.org/odata/ns/edm}TFunction"/>
   * &lt;element name="Annotations" type="{http://docs.oasis-open.org/odata/ns/edm}TAnnotations"/>
   * &lt;element name="EntityContainer" type="{http://docs.oasis-open.org/odata/ns/edm}TEntityContainer"/>
   * &lt;element ref="{http://docs.oasis-open.org/odata/ns/edm}Annotation"/>
   */
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

  private List<CsdlTerm> asCsdlTerms() {
    List<CsdlTerm> csdlTerms = new ArrayList<CsdlTerm>();

    for (Term t : terms) {
      csdlTerms.add(t);
    }
    return Collections.unmodifiableList(csdlTerms);
  }

  @JsonSetter
  void setComplexTypes(ComplexType[] newComplexTypes) {
    for (ComplexType t : newComplexTypes) {
      complexTypes.add(t);
    }
  }

  @JsonSetter
  void setEnumerations(ArrayList<EnumType> enumerations) {
    this.enumerations = enumerations;
  }

  @JsonSetter
  void setTerms(Term[] newTerms) {
    for (Term t : newTerms) {
      terms.add(t);
    }
  }

  @JsonSetter
  void setTypeDefinitions(ArrayList<TypeDefinition> typeDefinitions) {
    this.typeDefinitions = typeDefinitions;
  }
}
