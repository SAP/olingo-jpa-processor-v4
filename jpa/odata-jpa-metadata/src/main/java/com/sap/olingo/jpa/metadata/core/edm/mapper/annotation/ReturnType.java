package com.sap.olingo.jpa.metadata.core.edm.mapper.annotation;

import java.util.Objects;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.geo.SRID;
import org.apache.olingo.commons.api.edm.provider.CsdlReturnType;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ReturnType extends CsdlReturnType {

  @Override
  @JacksonXmlProperty(localName = "Type")
  public CsdlReturnType setType(final String type) {
    Objects.requireNonNull(type, "Type is a required attribute of return type");
    if (type.startsWith("Collection")) {
      setCollection(true);
      return super.setType(new FullQualifiedName(type.split("[()]")[1]));
    }
    return super.setType(new FullQualifiedName(type));
  }

  @Override
  @JacksonXmlProperty(localName = "Nullable")
  public CsdlReturnType setNullable(final boolean nullable) {
    return super.setNullable(nullable);
  }

  @Override
  @JacksonXmlProperty(localName = "MaxLength", isAttribute = true)
  public CsdlReturnType setMaxLength(final Integer maxLength) {
    return super.setMaxLength(maxLength);
  }

  @Override
  @JacksonXmlProperty(localName = "Precision", isAttribute = true)
  public CsdlReturnType setPrecision(final Integer precision) {
    return super.setPrecision(precision);
  }

  @Override
  @JacksonXmlProperty(localName = "Scale", isAttribute = true)
  public CsdlReturnType setScale(final Integer scale) {
    return super.setScale(scale);
  }

  @JacksonXmlProperty(localName = "SRID", isAttribute = true)
  void setSrid(final String srid) {
    Objects.requireNonNull(srid);
    super.setSrid(SRID.valueOf(srid));
  }

}
