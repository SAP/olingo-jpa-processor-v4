package com.sap.olingo.jpa.metadata.core.edm.mapper.annotation;

import org.apache.olingo.commons.api.edm.provider.CsdlPropertyRef;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class PropertyRef extends CsdlPropertyRef {

  @Override
  @JacksonXmlProperty(localName = "Name", isAttribute = true)
  public CsdlPropertyRef setName(final String name) {
    return super.setName(name);
  }

  @Override
  @JacksonXmlProperty(localName = "Alias", isAttribute = true)
  public CsdlPropertyRef setAlias(final String alias) {
    return super.setAlias(alias);
  }

}
