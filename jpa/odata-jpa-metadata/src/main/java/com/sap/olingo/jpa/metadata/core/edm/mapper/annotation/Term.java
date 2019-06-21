package com.sap.olingo.jpa.metadata.core.edm.mapper.annotation;

import java.util.ArrayList;
import java.util.List;

import org.apache.olingo.commons.api.edm.geo.SRID;
import org.apache.olingo.commons.api.edm.provider.CsdlTerm;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
class Term extends CsdlTerm {

  @JacksonXmlProperty(localName = "AppliesTo", isAttribute = true)
  void setAppliesTo(String appliesTo) {
    List<String> result = new ArrayList<>();
    if (appliesTo != null) {
      String[] list = appliesTo.split(" ");
      for (String apply : list) {
        result.add(apply);
      }
    }
    super.setAppliesTo(result);
  }

  @Override
  @JacksonXmlProperty(localName = "Name", isAttribute = true)
  public CsdlTerm setName(final String name) {
    return super.setName(name);
  }

  @Override
  @JacksonXmlProperty(localName = "Type", isAttribute = true)
  public CsdlTerm setType(final String type) {
    return super.setType(type);
  }

  @Override
  @JacksonXmlProperty(localName = "BaseTerm", isAttribute = true)
  public CsdlTerm setBaseTerm(final String baseTerm) {
    return super.setBaseTerm(baseTerm);
  }

  @Override
  public CsdlTerm setAppliesTo(final List<String> appliesTo) {
    return this;
  }

  @Override
  @JacksonXmlProperty(localName = "DefaultValue", isAttribute = true)
  public CsdlTerm setDefaultValue(final String defaultValue) {
    return super.setDefaultValue(defaultValue);
  }

  @Override
  @JacksonXmlProperty(localName = "Nullable", isAttribute = true)
  public CsdlTerm setNullable(final boolean nullable) {
    return super.setNullable(nullable);
  }

  @Override
  @JacksonXmlProperty(localName = "MaxLength", isAttribute = true)
  public CsdlTerm setMaxLength(final Integer maxLength) {
    return super.setMaxLength(maxLength);
  }

  @Override
  @JacksonXmlProperty(localName = "Precision", isAttribute = true)
  public CsdlTerm setPrecision(final Integer precision) {
    return super.setPrecision(precision);
  }

  @Override
  @JacksonXmlProperty(localName = "Scale", isAttribute = true)
  public CsdlTerm setScale(final Integer scale) {
    return super.setScale(scale);
  }

  @JacksonXmlProperty(localName = "SRID", isAttribute = true)
  void setSrid(final String srid) {
    if (srid != null)
      super.setSrid(SRID.valueOf(srid));
  }
}
