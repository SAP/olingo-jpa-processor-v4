package com.sap.olingo.jpa.metadata.core.edm.mapper.annotation;

import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EntityType extends CsdlEntityType {

//protected List<Object> keyOrPropertyOrNavigationProperty;

  // private String name;
  // private Boolean isAbstract;
  // private String baseType;
  // protected Boolean isOpenType;
  // protected Boolean hasStream;
  // private List<Property> properties;
  // private List<NavigationProperty> navigationProperties;
  // private List<EntityKeyElement> key;
//@XmlElement(name = "Annotation", type = Annotation.class)

  @Override
  @JacksonXmlProperty(localName = "HasStream", isAttribute = true)
  public CsdlEntityType setHasStream(final boolean hasStream) {
    return super.setHasStream(hasStream);
  }

  @JacksonXmlProperty(localName = "Key")
  public void setKey(final EntityKeyElement keys) {
    this.setKey(keys.getPropertyRef());
  }

  @Override
  @JacksonXmlProperty(localName = "Name", isAttribute = true)
  public CsdlEntityType setName(final String name) {
    return super.setName(name);
  }

  @Override
  @JacksonXmlProperty(localName = "OpenType", isAttribute = true)
  public CsdlEntityType setOpenType(final boolean isOpenType) {
    return super.setOpenType(isOpenType);
  }

  @Override
  @JacksonXmlProperty(localName = "BaseType", isAttribute = true)
  public CsdlEntityType setBaseType(String baseType) {
    return super.setBaseType(baseType);
  }

  @Override
  @JacksonXmlProperty(localName = "Abstract", isAttribute = true)
  public CsdlEntityType setAbstract(boolean isAbstract) {
    return super.setAbstract(isAbstract);
  }

  @JacksonXmlProperty(localName = "Property")
  public void setProperties(Property[] properties) {
    for (Property p : properties) {
      this.properties.add(p);
    }
  }

  @JacksonXmlProperty(localName = "NavigationProperty")
  public void setNavigationProperties(NavigationProperty[] navigationProperties) {
    for (NavigationProperty p : navigationProperties) {
      this.navigationProperties.add(p);
    }
  }
}
