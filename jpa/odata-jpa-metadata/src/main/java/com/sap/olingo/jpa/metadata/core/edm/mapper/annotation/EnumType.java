package com.sap.olingo.jpa.metadata.core.edm.mapper.annotation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EnumType {
  @JacksonXmlProperty(localName = "Name", isAttribute = true)
  private String name;

  @JacksonXmlProperty(localName = "IsFlags", isAttribute = true)
  private boolean isFlags;

  @JacksonXmlProperty(localName = "Member")
  private Member[] members;

}
