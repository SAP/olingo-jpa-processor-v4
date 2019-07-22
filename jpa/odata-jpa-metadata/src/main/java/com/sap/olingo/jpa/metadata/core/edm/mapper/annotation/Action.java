package com.sap.olingo.jpa.metadata.core.edm.mapper.annotation;

import java.util.Arrays;
import java.util.Objects;

import org.apache.olingo.commons.api.edm.provider.CsdlAction;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Action extends CsdlAction {

  @Override
  @JacksonXmlProperty(localName = "Name")
  public CsdlAction setName(final String name) {
    Objects.requireNonNull(name, "Name is a required attribute of actions");
    return super.setName(name);
  }

  @Override
  @JacksonXmlProperty(localName = "IsBound")
  public CsdlAction setBound(final boolean isBound) {
    return super.setBound(isBound);
  }

  @Override
  @JacksonXmlProperty(localName = "EntitySetPath")
  public CsdlAction setEntitySetPath(String entitySetPath) {
    return super.setEntitySetPath(entitySetPath);
  }

  @JacksonXmlProperty(localName = "Parameter")
  public void setParameters(final Parameter[] parameters) {
    this.parameters.addAll(Arrays.asList(parameters));
  }

  @JacksonXmlProperty(localName = "ReturnType")
  public void setReturnType(final ReturnType returnType) {
    super.setReturnType(returnType);
  }

}
