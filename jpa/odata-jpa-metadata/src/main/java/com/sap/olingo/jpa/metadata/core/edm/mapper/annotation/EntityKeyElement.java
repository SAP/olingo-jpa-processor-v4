package com.sap.olingo.jpa.metadata.core.edm.mapper.annotation;

import java.util.ArrayList;
import java.util.List;

import org.apache.olingo.commons.api.edm.provider.CsdlPropertyRef;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class EntityKeyElement {

  @JacksonXmlProperty(localName = "PropertyRef")
  protected List<PropertyRef> propertyRefs;

  List<CsdlPropertyRef> getPropertyRef() {
    List<CsdlPropertyRef> csdlPropertyRefs = new ArrayList<>();
    for (PropertyRef p : propertyRefs) {
      csdlPropertyRefs.add(p);
    }
    return csdlPropertyRefs;
  }

}
