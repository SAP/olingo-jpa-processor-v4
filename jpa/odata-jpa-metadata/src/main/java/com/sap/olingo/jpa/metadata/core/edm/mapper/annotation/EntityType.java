package com.sap.olingo.jpa.metadata.core.edm.mapper.annotation;

import java.util.ArrayList;
import java.util.List;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.commons.api.edm.provider.CsdlNavigationProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlPropertyRef;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EntityType extends CsdlEntityType {

//protected List<Object> keyOrPropertyOrNavigationProperty;

  @JacksonXmlProperty(localName = "Name", isAttribute = true)
  private String name;
  @JacksonXmlProperty(localName = "Abstract", isAttribute = true)
  private Boolean isAbstract;
  @JacksonXmlProperty(localName = "BaseType", isAttribute = true)
  private String baseType;
  @JacksonXmlProperty(localName = "OpenType", isAttribute = true)
  protected Boolean isOpenType;
  @JacksonXmlProperty(localName = "HasStream", isAttribute = true)
  protected Boolean hasStream;

  @JacksonXmlProperty(localName = "Property")
  private List<Property> properties;
  @JacksonXmlProperty(localName = "NavigationProperty")
  private List<NavigationProperty> navigationProperties;
  @JacksonXmlProperty(localName = "Key")
  private List<EntityKeyElement> key;
//@XmlElement(name = "Annotation", type = Annotation.class)

  @Override
  public boolean hasStream() {
    return hasStream;
  }

  @Override
  public List<CsdlPropertyRef> getKey() {
//  <xs:element name="Key" type="edm:TEntityKeyElement" minOccurs="0" maxOccurs="1" />    
    if (key != null || !key.isEmpty())
      return key.get(0).getPropertyRef();
    return new ArrayList<CsdlPropertyRef>();
  }

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
    return baseType;
  }

  @Override
  public FullQualifiedName getBaseTypeFQN() {
    return new FullQualifiedName(baseType);
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

//  @Override
//  public List<CsdlAnnotation> getAnnotations() {
//    // TODO Auto-generated method stub
//    return super.getAnnotations();
//  }

//  @Override
//  protected <T extends CsdlNamed> T getOneByName(String name, Collection<T> items) {
//    return super.getOneByName(name, items);
//  }

//  @Override
//  protected <T extends CsdlNamed> List<T> getAllByName(String name, Collection<T> items) {
//    return super.getAllByName(name, items);
//  }
}
