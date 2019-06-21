package com.sap.olingo.jpa.metadata.core.edm.mapper.annotation;

import org.apache.olingo.commons.api.edm.provider.CsdlReferentialConstraint;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class ReferentialConstraint extends CsdlReferentialConstraint {

  @Override
  @JacksonXmlProperty(localName = "Property", isAttribute = true)
  public CsdlReferentialConstraint setProperty(String property) {
    return super.setProperty(property);
  }

  @Override
  @JacksonXmlProperty(localName = "ReferencedProperty", isAttribute = true)
  public CsdlReferentialConstraint setReferencedProperty(String referencedProperty) {
    return super.setReferencedProperty(referencedProperty);
  }
}
