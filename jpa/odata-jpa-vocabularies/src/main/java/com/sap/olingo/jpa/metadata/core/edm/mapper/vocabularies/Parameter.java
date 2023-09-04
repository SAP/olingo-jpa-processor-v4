package com.sap.olingo.jpa.metadata.core.edm.mapper.vocabularies;

import java.util.Objects;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.geo.SRID;
import org.apache.olingo.commons.api.edm.provider.CsdlParameter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Parameter extends CsdlParameter {

  @Override
  @JacksonXmlProperty(localName = "Name")
  public CsdlParameter setName(final String name) {
    Objects.requireNonNull(name, "Name is a required attribute of parameter");
    return super.setName(name);
  }

  @Override
  @JacksonXmlProperty(localName = "Type")
  public CsdlParameter setType(final String type) {
    Objects.requireNonNull(type, "Type is a required attribute of parameters");
    if (type.startsWith("Collection")) {
      setCollection(true);
      return super.setType(new FullQualifiedName(type.split("[()]")[1]));
    }
    return super.setType(new FullQualifiedName(type));
  }

  @Override
  @JacksonXmlProperty(localName = "Nullable")
  public CsdlParameter setNullable(final boolean nullable) {
    return super.setNullable(nullable);
  }

  @Override
  @JacksonXmlProperty(localName = "MaxLength", isAttribute = true)
  public CsdlParameter setMaxLength(Integer maxLength) {
    return super.setMaxLength(maxLength);
  }

  @Override
  @JacksonXmlProperty(localName = "Precision", isAttribute = true)
  public CsdlParameter setPrecision(final Integer precision) {
    return super.setPrecision(precision);
  }

  @Override
  @JacksonXmlProperty(localName = "Scale", isAttribute = true)
  public CsdlParameter setScale(final Integer scale) {
    return super.setScale(scale);
  }

  @JacksonXmlProperty(localName = "SRID", isAttribute = true)
  void setSrid(final String srid) {
    Objects.requireNonNull(srid);
    super.setSrid(SRID.valueOf(srid));
  }
}