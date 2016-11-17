package com.sap.olingo.jpa.metadata.core.edm.mapper.annotation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JsonIgnoreProperties(ignoreUnknown = true)
@JacksonXmlRootElement(localName = "Edmx", namespace = "edmx")
public class Edmx {
  @JacksonXmlProperty(localName = "DataServices")
  private EdmxDataServices service;

  EdmxDataServices getDataService() {
    return service;
  }
}
