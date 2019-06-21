package com.sap.olingo.jpa.metadata.core.edm.mapper.annotation;

import java.util.Arrays;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlNavigationProperty;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class NavigationProperty extends CsdlNavigationProperty {

  @Override
  public CsdlNavigationProperty setCollection(boolean isCollection) {
    return this;
  }

  @Override
  @JacksonXmlProperty(localName = "Name", isAttribute = true)
  public CsdlNavigationProperty setName(final String name) {
    return super.setName(name);
  }

  @Override
  @JacksonXmlProperty(localName = "Type", isAttribute = true)
  public CsdlNavigationProperty setType(final String type) {
    return super.setType(new FullQualifiedName(type));
  }

  @Override
  @JacksonXmlProperty(localName = "Partner", isAttribute = true)
  public CsdlNavigationProperty setPartner(final String partner) {
    return super.setPartner(partner);
  }

  @Override
  @JacksonXmlProperty(localName = "ContainsTarget", isAttribute = true)
  public CsdlNavigationProperty setContainsTarget(final boolean containsTarget) {
    return super.setContainsTarget(containsTarget);
  }

  @JacksonXmlProperty(localName = "ReferentialConstraint")
  void setReferentialConstraints(final ReferentialConstraint[] referentialConstraints) {
    super.setReferentialConstraints(Arrays.asList(referentialConstraints));
  }

  @Override
  @JacksonXmlProperty(localName = "Nullable", isAttribute = true)
  public CsdlNavigationProperty setNullable(final Boolean nullable) {
    return super.setNullable(nullable);
  }

  @JacksonXmlProperty(localName = "OnDelete", isAttribute = true)
  public CsdlNavigationProperty setOnDelete(final OnDelete onDelete) {
    return super.setOnDelete(onDelete);
  }

}
