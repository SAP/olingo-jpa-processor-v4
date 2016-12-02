package com.sap.olingo.jpa.metadata.core.edm.mapper.annotation;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.geo.SRID;
import org.apache.olingo.commons.api.edm.provider.CsdlTypeDefinition;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
class TypeDefinition extends CsdlTypeDefinition {

  TypeDefinition() {
    super();
  }

  @JacksonXmlProperty(localName = "Name", isAttribute = true)
  private String name;
  @JacksonXmlProperty(localName = "UnderlyingType", isAttribute = true)
  private String underlyingType;
  @JacksonXmlProperty(localName = "MaxLength", isAttribute = true)
  private Integer maxLength;
  @JacksonXmlProperty(localName = "Precision", isAttribute = true)
  private Integer precision;
  @JacksonXmlProperty(localName = "Scale", isAttribute = true)
  private Integer scale;
  @JacksonXmlProperty(localName = "SRID", isAttribute = true)
  private String srid;
  @JacksonXmlProperty(localName = "Unicode", isAttribute = true)
  private Boolean isUnicode;

//@JacksonXmlProperty(localName = "Annotation")
//protected List<Annotation> annotations;

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getUnderlyingType() {
    return underlyingType;
  }

  @Override
  public Integer getMaxLength() {
    return maxLength;
  }

  @Override
  public Integer getPrecision() {
    return new Integer(precision.intValue());
  }

  @Override
  public Integer getScale() {
    return scale;
  }

  @Override
  public boolean isUnicode() {
    return isUnicode;
  }

  @Override
  public SRID getSrid() {
    if (srid != null)
      return SRID.valueOf(srid);
    else
      return null;
  }

  @Override
  public TypeDefinition setName(String name) {
    this.name = name;
    return this;
  }

  @JsonSetter
  @Override
  public CsdlTypeDefinition setUnderlyingType(String underlyingType) {
    this.underlyingType = underlyingType;
    return this;
  }

  @Override
  public CsdlTypeDefinition setUnderlyingType(FullQualifiedName underlyingType) {
    this.underlyingType = underlyingType.getFullQualifiedNameAsString();
    return this;
  }

  @Override
  public CsdlTypeDefinition setMaxLength(Integer maxLength) {
    this.maxLength = maxLength;
    return this;
  }

  @Override
  public CsdlTypeDefinition setPrecision(Integer precision) {
    this.precision = precision;
    return this;
  }

  @Override
  public CsdlTypeDefinition setScale(Integer scale) {
    this.scale = scale;
    return this;
  }

  @Override
  public CsdlTypeDefinition setUnicode(boolean unicode) {
    this.isUnicode = unicode;
    return this;
  }

  @Override
  public CsdlTypeDefinition setSrid(SRID srid) {
    this.srid = srid.toString();
    return this;
  }

//  @Override
//  public List<CsdlAnnotation> getAnnotations() {
//    return annotations;
//  }

}
