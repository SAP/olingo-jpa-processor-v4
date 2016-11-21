package com.sap.olingo.jpa.metadata.core.edm.mapper.annotation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JsonIgnoreProperties(ignoreUnknown = true)
@JacksonXmlRootElement(localName = "DataServices", namespace = "edmx")
public class EdmxDataServices {

  @JacksonXmlProperty(localName = "Schema")
  private Schema[] schemas;

  Schema[] getSchemas() {
    return schemas;
  }
}
