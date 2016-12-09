package com.sap.olingo.jpa.metadata.core.edm.mapper.annotation;

import java.util.ArrayList;
import java.util.List;

import org.apache.olingo.commons.api.edm.geo.SRID;
import org.apache.olingo.commons.api.edm.provider.CsdlTerm;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
class Term extends CsdlTerm {

  Term() {
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
  private SRID srid;

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getType() {
    return type;
  }

  @Override
  public String getBaseTerm() {
    return baseTerm;
  }

  @Override
  public boolean isNullable() {
    return nullable;
  }

  @Override
  public String getDefaultValue() {
    return defaultValue;
  }

  @Override
  public List<String> getAppliesTo() {
    List<String> result = new ArrayList<String>();
    if (appliesTo != null) {
      String[] list = appliesTo.split(" ");
      for (String apply : list) {
        result.add(apply);
      }
    }
    return result;
  }

  @Override
  public Integer getMaxLength() {
    return maxLength;
  }

  @Override
  public Integer getPrecision() {
    return precision;
  }

  @Override
  public Integer getScale() {
    return scale;
  }

  @Override
  public SRID getSrid() {
    return srid;
  }

  @JsonSetter
  void setAppliesTo(String appliesTo) {
    this.appliesTo = appliesTo;
  }

  @Override
  public CsdlTerm setName(String name) {
    this.name = name;
    return this;
  }

  @Override
  public CsdlTerm setType(String type) {
    this.type = type;
    return this;
  }

  @Override
  public CsdlTerm setBaseTerm(String baseTerm) {
    this.baseTerm = baseTerm;
    return this;
  }

  @Override
  public CsdlTerm setAppliesTo(List<String> appliesTo) {
    return this;
  }

  @Override
  public CsdlTerm setDefaultValue(String defaultValue) {
    this.defaultValue = defaultValue;
    return this;
  }

  @Override
  public CsdlTerm setNullable(boolean nullable) {
    this.nullable = nullable;
    return this;
  }

  @Override
  public CsdlTerm setMaxLength(Integer maxLength) {
    this.maxLength = maxLength;
    return this;
  }

  @Override
  public CsdlTerm setPrecision(Integer precision) {
    this.precision = precision;
    return this;
  }

  @Override
  public CsdlTerm setScale(Integer scale) {
    this.scale = scale;
    return this;
  }

  @Override
  public CsdlTerm setSrid(SRID srid) {
    this.srid = srid;
    return this;
  }

  @JsonSetter
  void setSrid(String srid) {
    if (srid != null)
      this.srid = SRID.valueOf(srid);
    else
      this.srid = null;
  }
}
