package org.apache.olingo.jpa.metadata.core.edm.mapper.annotation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Schema {
  @JacksonXmlProperty(isAttribute = true)
  private String xmlns;

  @JacksonXmlProperty(isAttribute = true)
  private String Namespace;

  @JacksonXmlProperty(isAttribute = true)
  private String Alias;

  @JacksonXmlProperty(localName = "Term")
  private Term[] terms;

  @JacksonXmlProperty(localName = "EnumType")
  private EnumType[] enumerations;

  @JacksonXmlProperty(localName = "TypeDefinition")
  private TypeDefinition[] typeDefinitions;

  Term[] getTerms() {
    return terms;
  }

  String getNamespace() {
    return Namespace;
  }

  String getAlias() {
    return Alias;
  }
}
