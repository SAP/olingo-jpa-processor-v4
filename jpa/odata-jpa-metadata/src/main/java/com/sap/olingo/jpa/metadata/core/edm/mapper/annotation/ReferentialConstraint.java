package com.sap.olingo.jpa.metadata.core.edm.mapper.annotation;

import org.apache.olingo.commons.api.edm.provider.CsdlReferentialConstraint;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class ReferentialConstraint extends CsdlReferentialConstraint {

  @JacksonXmlProperty(localName = "Property", isAttribute = true)
  protected String property;
  @JacksonXmlProperty(localName = "ReferencedProperty", isAttribute = true)
  protected String referencedProperty;

  @Override
  public String getProperty() {
    return property;
  }

  @Override
  public String getReferencedProperty() {
    return referencedProperty;
  }

//  @Override
//  public List<CsdlAnnotation> getAnnotations() {
//    return super.getAnnotations();
//  }

//  @XmlElement(name = "Annotation")
//  protected List<Annotation> annotation;
}
