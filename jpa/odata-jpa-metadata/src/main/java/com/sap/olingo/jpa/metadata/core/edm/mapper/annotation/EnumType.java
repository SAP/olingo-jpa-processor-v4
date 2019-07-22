package com.sap.olingo.jpa.metadata.core.edm.mapper.annotation;

import org.apache.olingo.commons.api.edm.provider.CsdlEnumType;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EnumType extends CsdlEnumType {

  @Override
  @JacksonXmlProperty(localName = "Name", isAttribute = true)
  public CsdlEnumType setName(final String name) {
    return super.setName(name);
  }

  @Override
  @JacksonXmlProperty(localName = "IsFlags", isAttribute = true)
  public CsdlEnumType setFlags(final boolean isFlags) {
    return super.setFlags(isFlags);
  }

  @Override
  @JacksonXmlProperty(localName = "UnderlyingType")
  public CsdlEnumType setUnderlyingType(final String underlyingType) {
    return super.setUnderlyingType(underlyingType);
  }

  @JacksonXmlProperty(localName = "Member")
  public void setMembers(final Member[] members) {
    for (final Member m : members) {
      this.getMembers().add(m);
    }
  }
}
