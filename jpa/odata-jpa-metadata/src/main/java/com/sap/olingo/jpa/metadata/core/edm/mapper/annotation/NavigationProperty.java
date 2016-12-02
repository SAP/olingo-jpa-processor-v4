package com.sap.olingo.jpa.metadata.core.edm.mapper.annotation;

import java.util.ArrayList;
import java.util.List;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlNavigationProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlReferentialConstraint;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class NavigationProperty extends CsdlNavigationProperty {

  @JacksonXmlProperty(localName = "Name", isAttribute = true)
  private String name;
  @JacksonXmlProperty(localName = "Type", isAttribute = true)
  private FullQualifiedName type;
  @JacksonXmlProperty(localName = "Nullable", isAttribute = true)
  private Boolean isNullable;
  @JacksonXmlProperty(localName = "Partner", isAttribute = true)
  private String partner;
  @JacksonXmlProperty(localName = "ContainsTarget", isAttribute = true)
  private Boolean containsTarget;

  @JacksonXmlProperty(localName = "ReferentialConstraint")
  List<ReferentialConstraint> referentialConstraints;

//  @JacksonXmlProperty(localName = "OnDelete")
//  List<OnDelete> oneDelete;

  @Override
  public String getName() {
    return name;
  }

  @Override
  public boolean isCollection() {
    return super.isCollection();
  }

  @Override
  public FullQualifiedName getTypeFQN() {
    // TODO Auto-generated method stub
    return super.getTypeFQN();
  }

  @Override
  public String getType() {
    // TODO Auto-generated method stub
    return super.getType();
  }

  @Override
  public String getPartner() {
    return partner;
  }

  @Override
  public boolean isContainsTarget() {
    return containsTarget;
  }

  @Override
  public List<CsdlReferentialConstraint> getReferentialConstraints() {
    List<CsdlReferentialConstraint> csdlConstraints = new ArrayList<CsdlReferentialConstraint>();
    for (CsdlReferentialConstraint rc : referentialConstraints) {
      csdlConstraints.add(rc);
    }
    return csdlConstraints;
  }

  @Override
  public Boolean isNullable() {
    return isNullable;
  }

  @Override
  public CsdlNavigationProperty setCollection(boolean isCollection) {
    return this;
  }

  @Override
  public CsdlNavigationProperty setName(String name) {
    this.name = name;
    return this;
  }

  @Override
  public CsdlNavigationProperty setType(FullQualifiedName type) {
    this.type = type;
    return this;
  }

  @JsonSetter
  @Override
  public CsdlNavigationProperty setType(String type) {
    this.type = new FullQualifiedName(type);
    return this;
  }

  @Override
  public CsdlNavigationProperty setPartner(String partner) {
    this.partner = partner;
    return this;
  }

  @Override
  public CsdlNavigationProperty setContainsTarget(boolean containsTarget) {
    this.containsTarget = containsTarget;
    return this;
  }

  @Override
  public CsdlNavigationProperty setReferentialConstraints(List<CsdlReferentialConstraint> referentialConstraints) {
    return this;
  }

  @JsonSetter
  void setReferentialConstraints(ReferentialConstraint[] referentialConstraints) {
    for (ReferentialConstraint rc : referentialConstraints) {
      this.referentialConstraints.add(rc);
    }
  }

  @Override
  public CsdlNavigationProperty setNullable(Boolean nullable) {
    this.isNullable = nullable;
    return this;
  }

//  @Override
//  public List<CsdlAnnotation> getAnnotations() {
//    return super.getAnnotations();
//  }

//  @Override
//  public CsdlOnDelete getOnDelete() {
//    // TODO Auto-generated method stub
//    return super.getOnDelete();
//  }

//  @Override
//  public List<CsdlAnnotation> getAnnotations() {
//    // TODO Auto-generated method stub
//    return super.getAnnotations();
//  }
}
