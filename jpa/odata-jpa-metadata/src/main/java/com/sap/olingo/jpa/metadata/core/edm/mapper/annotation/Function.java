package com.sap.olingo.jpa.metadata.core.edm.mapper.annotation;

import java.util.Arrays;
import java.util.Objects;

import org.apache.olingo.commons.api.edm.provider.CsdlFunction;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

/**
 * Function as part of a OData vocabulary. <br>
 * @author Oliver Grande
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Function extends CsdlFunction {

  @Override
  @JacksonXmlProperty(localName = "IsBound")
  public CsdlFunction setBound(final boolean isBound) {
    return super.setBound(isBound);
  }

  @Override
  @JacksonXmlProperty(localName = "IsComposable")
  public CsdlFunction setComposable(final boolean isComposable) {
    return super.setComposable(isComposable);
  }

  @Override
  @JacksonXmlProperty(localName = "EntitySetPath")
  public CsdlFunction setEntitySetPath(String entitySetPath) {
    return super.setEntitySetPath(entitySetPath);
  }

  @Override
  @JacksonXmlProperty(localName = "Name")
  public CsdlFunction setName(final String name) {
    Objects.requireNonNull(name, "Name is a required attribute of functions");
    return super.setName(name);
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
