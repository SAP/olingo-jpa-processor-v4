package com.sap.olingo.jpa.metadata.core.edm.mapper.annotation;

import java.util.Objects;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.geo.SRID;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Property extends CsdlProperty {

  @Override
  @JacksonXmlProperty(localName = "Name", isAttribute = true)
  public CsdlProperty setName(final String name) {
    Objects.requireNonNull(name);
    return super.setName(name);
  }

  @Override
  @JacksonXmlProperty(localName = "Type", isAttribute = true)
  public CsdlProperty setType(final String type) {
    Objects.requireNonNull(type);
    if (type.startsWith("Collection")) {
      setCollection(true);
      return super.setType(new FullQualifiedName(type.split("[()]")[1]));
    }
    return super.setType(new FullQualifiedName(type));
  }

  @Override
  @JacksonXmlProperty(localName = "DefaultValue", isAttribute = true)
  public CsdlProperty setDefaultValue(final String defaultValue) {
    return super.setDefaultValue(defaultValue);
  }

  @Override
  @JacksonXmlProperty(localName = "Nullable", isAttribute = true)
  public CsdlProperty setNullable(final boolean nullable) {
    return super.setNullable(nullable);
  }

  @Override
  @JacksonXmlProperty(localName = "MaxLength", isAttribute = true)
  public CsdlProperty setMaxLength(final Integer maxLength) {
    return super.setMaxLength(maxLength);
  }

  @Override
  @JacksonXmlProperty(localName = "Precision", isAttribute = true)
  public CsdlProperty setPrecision(final Integer precision) {
    return super.setPrecision(precision);
  }

  @Override
  @JacksonXmlProperty(localName = "Scale", isAttribute = true)
  public CsdlProperty setScale(final Integer scale) {
    return super.setScale(scale);
  }

  @Override
  @JacksonXmlProperty(localName = "Unicode", isAttribute = true)
  public CsdlProperty setUnicode(final boolean unicode) {
    return super.setUnicode(unicode);
  }

  @JacksonXmlProperty(localName = "SRID", isAttribute = true)
  void setSrid(final String srid) {
    Objects.requireNonNull(srid);
    super.setSrid(SRID.valueOf(srid));
  }
}
