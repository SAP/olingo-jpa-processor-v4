package com.sap.olingo.jpa.metadata.core.edm.mapper.annotation;

import java.util.ArrayList;
import java.util.List;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlEnumMember;
import org.apache.olingo.commons.api.edm.provider.CsdlEnumType;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EnumType extends CsdlEnumType {

  @JacksonXmlProperty(localName = "Name", isAttribute = true)
  private String name;

  @JacksonXmlProperty(localName = "IsFlags", isAttribute = true)
  private boolean isFlags;

  @JacksonXmlProperty(localName = "UnderlyingType")
  private FullQualifiedName underlyingType;

  @JacksonXmlProperty(localName = "Member")
  private Member[] members;

  @Override
  public String getName() {
    return name;
  }

  @Override
  public boolean isFlags() {
    return isFlags;
  }

  @Override
  public String getUnderlyingType() {
    return underlyingType.getFullQualifiedNameAsString();
  }

  @Override
  public List<CsdlEnumMember> getMembers() {
    List<CsdlEnumMember> csdlMembers = new ArrayList<CsdlEnumMember>();
    for (CsdlEnumMember m : members) {
      csdlMembers.add(m);
    }
    return csdlMembers;
  }

  @Override
  public CsdlEnumMember getMember(String name) {
    for (CsdlEnumMember m : members) {
      if (m.getName().equals(name))
        return super.getMember(name);
    }
    return null;
  }

  @Override
  public CsdlEnumMember getMember(Integer index) {
    return members[index];
  }

  @Override
  public CsdlEnumType setName(String name) {
    this.name = name;
    return this;
  }

  @Override
  public CsdlEnumType setFlags(boolean isFlags) {
    this.isFlags = isFlags;
    return this;
  }

  @Override
  @JsonSetter
  public CsdlEnumType setUnderlyingType(String underlyingType) {
    this.underlyingType = new FullQualifiedName(underlyingType);
    return this;
  }

  @Override
  public CsdlEnumType setUnderlyingType(FullQualifiedName underlyingType) {
    this.underlyingType = underlyingType;
    return this;
  }

  @JsonSetter
  public void setMembers(Member[] members) {
    this.members = members;
  }

//  @Override
//  public List<CsdlAnnotation> getAnnotations() {
//    return annotations;
//  }

//@JacksonXmlProperty(localName = "Annotation")
//protected List<Annotation> annotations;

}
