package com.sap.olingo.jpa.metadata.core.edm.mapper.annotation;

import java.util.Objects;

import org.apache.olingo.commons.api.edm.geo.SRID;
import org.apache.olingo.commons.api.edm.provider.CsdlTypeDefinition;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
class TypeDefinition extends CsdlTypeDefinition {

  @Override
  @JacksonXmlProperty(localName = "Name", isAttribute = true)
  public CsdlTypeDefinition setName(final String name) {
    return super.setName(name);
  }

  @Override
  @JacksonXmlProperty(localName = "UnderlyingType", isAttribute = true)
  public CsdlTypeDefinition setUnderlyingType(final String underlyingType) {
    return super.setUnderlyingType(underlyingType);
  }

  @Override
  @JacksonXmlProperty(localName = "MaxLength", isAttribute = true)
  public CsdlTypeDefinition setMaxLength(final Integer maxLength) {
    return super.setMaxLength(maxLength);
  }

  @Override
  @JacksonXmlProperty(localName = "Precision", isAttribute = true)
  public CsdlTypeDefinition setPrecision(final Integer precision) {
    return super.setPrecision(precision);
  }

  @Override
  @JacksonXmlProperty(localName = "Scale", isAttribute = true)
  public CsdlTypeDefinition setScale(final Integer scale) {
    return super.setScale(scale);
  }

  @Override
  @JacksonXmlProperty(localName = "Unicode", isAttribute = true)
  public CsdlTypeDefinition setUnicode(final boolean unicode) {
    return super.setUnicode(unicode);
  }

  @JacksonXmlProperty(localName = "SRID", isAttribute = true)
  void setSrid(final String srid) {
    Objects.requireNonNull(srid);
    super.setSrid(SRID.valueOf(srid));
  }

}
