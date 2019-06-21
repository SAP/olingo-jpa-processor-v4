package com.sap.olingo.jpa.metadata.core.edm.mapper.annotation;

import org.apache.olingo.commons.api.edm.provider.CsdlOnDelete;
import org.apache.olingo.commons.api.edm.provider.CsdlOnDeleteAction;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class OnDelete extends CsdlOnDelete {

  @JacksonXmlProperty(localName = "OnDelete", isAttribute = true)
  public void setAction(final String action) {
    super.setAction(CsdlOnDeleteAction.valueOf(action));
  }
}
