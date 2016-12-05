package com.sap.olingo.jpa.metadata.core.edm.mapper.annotation;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.geo.SRID;
import org.apache.olingo.commons.api.edm.provider.CsdlMapping;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Property extends CsdlProperty {

  @JacksonXmlProperty(localName = "Name", isAttribute = true)
  private String name;
  @JacksonXmlProperty(localName = "Type", isAttribute = true)
  private FullQualifiedName type;
  @JacksonXmlProperty(localName = "Nullable", isAttribute = true)
  private Boolean isNullable;
  @JacksonXmlProperty(localName = "DefaultValue", isAttribute = true)
  private String defaultValue;
  @JacksonXmlProperty(localName = "Unicode", isAttribute = true)
  private Boolean isUnicode;
  @JacksonXmlProperty(localName = "MaxLength", isAttribute = true)
  private Integer maxLength;
  @JacksonXmlProperty(localName = "Precision", isAttribute = true)
  private Integer precision;
  @JacksonXmlProperty(localName = "Scale", isAttribute = true)
  private Integer scale;
  @JacksonXmlProperty(localName = "SRID", isAttribute = true)
  private SRID srid;

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getType() {
    // TODO Auto-generated method stub
    return super.getType();
  }

  @Override
  public FullQualifiedName getTypeAsFQNObject() {
    // TODO Auto-generated method stub
    return super.getTypeAsFQNObject();
  }

  @Override
  public boolean isCollection() {
//    final int collStartIdx = typeExpression.indexOf("Collection(");
//    final int collEndIdx = typeExpression.lastIndexOf(')');
    return false;
  }

  @Override
  public String getDefaultValue() {
    return defaultValue;
  }

  @Override
  public boolean isNullable() {
    return isNullable;
  }

  @Override
  public Integer getMaxLength() {
    return new Integer(maxLength);
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
  public boolean isUnicode() {
    return isUnicode;
  }

  @Override
  public String getMimeType() {
    return super.getMimeType();
  }

  @Override
  public CsdlMapping getMapping() {
    return super.getMapping();
  }

  @Override
  public SRID getSrid() {
    return srid;
  }

  @Override
  public CsdlProperty setName(String name) {
    this.name = name;
    return this;
  }

  @Override
  public CsdlProperty setType(FullQualifiedName type) {
    this.type = type;
    return this;
  }

  @JsonSetter
  @Override
  public CsdlProperty setType(String type) {
    this.type = new FullQualifiedName(type);
    return this;
  }

  @Override
  public CsdlProperty setCollection(boolean isCollection) {
    return this;
  }

  @Override
  public CsdlProperty setDefaultValue(String defaultValue) {
    this.defaultValue = defaultValue;
    return this;
  }

  @Override
  public CsdlProperty setNullable(boolean nullable) {
    this.isNullable = nullable;
    return this;
  }

  @Override
  public CsdlProperty setMaxLength(Integer maxLength) {
    this.maxLength = maxLength;
    return this;
  }

  @Override
  public CsdlProperty setPrecision(Integer precision) {
    this.precision = precision;
    return this;
  }

  @Override
  public CsdlProperty setScale(Integer scale) {
    this.scale = scale;
    return this;
  }

  @Override
  public CsdlProperty setUnicode(boolean unicode) {
    this.isUnicode = unicode;
    return this;
  }

  @Override
  public CsdlProperty setMimeType(String mimeType) {
    return this;
  }

  @Override
  public CsdlProperty setMapping(CsdlMapping mapping) {
    return this;
  }

  @Override
  public CsdlProperty setSrid(SRID srid) {
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
