package com.sap.olingo.jpa.metadata.core.edm.mapper.annotation;

import java.util.ArrayList;
import java.util.List;

import org.apache.olingo.commons.api.edm.geo.SRID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
class Term {

  public Term() {
    super();
  }

  @JacksonXmlProperty(localName = "Name", isAttribute = true)
  private String name;

  @JacksonXmlProperty(localName = "Type", isAttribute = true)
  private String type;

  @JacksonXmlProperty(localName = "BaseTerm", isAttribute = true)
  private String baseTerm;

  @JacksonXmlProperty(localName = "Nullable", isAttribute = true)
  private boolean nullable;

  @JacksonXmlProperty(localName = "DefaultValue", isAttribute = true)
  private String defaultValue;

  @JacksonXmlProperty(localName = "AppliesTo", isAttribute = true)
  private String appliesTo;

  @JacksonXmlProperty(localName = "MaxLength", isAttribute = true)
  private int maxLength;

  @JacksonXmlProperty(localName = "Precision", isAttribute = true)
  private int precision;

  @JacksonXmlProperty(localName = "Scale", isAttribute = true)
  private int scale;

  @JacksonXmlProperty(localName = "SRID", isAttribute = true)
  private String srid;

  String getName() {
    return name;
  }

  String getType() {
    return type;
  }

  String getBaseTerm() {
    return baseTerm;
  }

  boolean isNullable() {
    return nullable;
  }

  String getDefaultValue() {
    return defaultValue;
  }

  List<String> getAppliesTo() {
    List<String> result = new ArrayList<String>();
    if (appliesTo != null) {
      String[] list = appliesTo.split(" ");
      for (String apply : list) {
        result.add(apply);
      }
    }
    return result;
  }

  int getMaxLength() {
    return maxLength;
  }

  int getPrecision() {
    return precision;
  }

  int getScale() {
    return scale;
  }

  SRID getSrid() {
    if (srid != null)
      return SRID.valueOf(srid);
    else
      return null;
  }
}
