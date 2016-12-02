package com.sap.olingo.jpa.metadata.core.edm.mapper.annotation;

import java.util.ArrayList;
import java.util.List;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlComplexType;
import org.apache.olingo.commons.api.edm.provider.CsdlNavigationProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
class ComplexType extends CsdlComplexType {

  @JacksonXmlProperty(localName = "Name", isAttribute = true)
  private String name;
  @JacksonXmlProperty(localName = "Abstract", isAttribute = true)
  private Boolean isAbstract;
  @JacksonXmlProperty(localName = "OpenType", isAttribute = true)
  private Boolean isOpenType;
  @JacksonXmlProperty(localName = "BaseType", isAttribute = true)
  private FullQualifiedName baseType;
  @JacksonXmlProperty(localName = "Property")
  private List<Property> properties = new ArrayList<Property>();
  @JacksonXmlProperty(localName = "NavigationProperty")
  private List<NavigationProperty> navigationProperties = new ArrayList<NavigationProperty>();

//@JacksonXmlProperty(localName = "Annotation")
//protected List<Annotation> annotations;
  @Override
  public String getName() {
    return name;
  }

  @Override
  public boolean isOpenType() {
    return isOpenType;
  }

  @Override
  public String getBaseType() {
    return baseType.getFullQualifiedNameAsString();
  }

  @Override
  public FullQualifiedName getBaseTypeFQN() {
    return baseType;
  }

  @Override
  public boolean isAbstract() {
    return isAbstract;
  }

  @Override
  public List<CsdlProperty> getProperties() {
    List<CsdlProperty> csdlProperties = new ArrayList<CsdlProperty>();

    for (Property p : properties) {
      csdlProperties.add(p);
    }
    return csdlProperties;
  }

  @Override
  public CsdlProperty getProperty(String name) {

    for (Property p : properties) {
      if (p.getName().equals(name))
        return p;
    }
    return null;
  }

  @Override
  public List<CsdlNavigationProperty> getNavigationProperties() {
    List<CsdlNavigationProperty> csdlNaviProperties = new ArrayList<CsdlNavigationProperty>();

    for (NavigationProperty p : navigationProperties) {
      csdlNaviProperties.add(p);
    }
    return csdlNaviProperties;
  }

  @Override
  public CsdlNavigationProperty getNavigationProperty(String name) {

    for (NavigationProperty p : navigationProperties) {
      if (p.getName().equals(name))
        return p;
    }
    return null;
  }

  @Override
  public CsdlComplexType setName(String name) {
    this.name = name;
    return this;
  }

  @Override
  public CsdlComplexType setOpenType(boolean isOpenType) {
    this.isOpenType = isOpenType;
    return this;
  }

  @JsonSetter
  @Override
  public CsdlComplexType setBaseType(String baseType) {
    this.baseType = new FullQualifiedName(baseType);
    return this;
  }

  @Override
  public CsdlComplexType setBaseType(FullQualifiedName baseType) {
    this.baseType = baseType;
    return this;
  }

  @Override
  public CsdlComplexType setAbstract(boolean isAbstract) {
    this.isAbstract = isAbstract;
    return this;
  }

  @JsonSetter
  void setProperties(Property[] properties) {
    for (Property p : properties) {
      this.properties.add(p);
    }
  }

  @JsonSetter
  public void setNavigationProperties(NavigationProperty[] navigationProperties) {
    for (NavigationProperty p : navigationProperties) {
      this.navigationProperties.add(p);
    }
  }

//  @Override
//  public CsdlComplexType setAnnotations(List<CsdlAnnotation> annotations) {
//    // TODO Auto-generated method stub
//    return super.setAnnotations(annotations);
//  }

//  @Override
//  public List<CsdlAnnotation> getAnnotations() {
//    return annotations();
//  }

}
