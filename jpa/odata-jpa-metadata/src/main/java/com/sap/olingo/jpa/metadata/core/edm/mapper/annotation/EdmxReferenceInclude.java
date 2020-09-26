package com.sap.olingo.jpa.metadata.core.edm.mapper.annotation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JsonIgnoreProperties(ignoreUnknown = true)
@JacksonXmlRootElement(localName = "Reference", namespace = "edmx")
public class EdmxReferenceInclude {

  @JacksonXmlProperty(localName = "Namespace")
  private String namespace;

  @JacksonXmlProperty(localName = "Alias")
  private String alias;

  public String getNamespace() {
    return namespace;
  }

  public String getAlias() {
    return alias;
  }

}
