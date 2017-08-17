package com.sap.olingo.jpa.metadata.core.edm.mapper.annotation;

import org.apache.olingo.commons.api.edm.provider.CsdlPropertyRef;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class PropertyRef extends CsdlPropertyRef {

  @JacksonXmlProperty(localName = "Name", isAttribute = true)
  private String name;

  @JacksonXmlProperty(localName = "Alias", isAttribute = true)
  private String alias;

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getAlias() {
    return alias;
  }
}
