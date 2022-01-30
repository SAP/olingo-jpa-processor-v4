package com.sap.olingo.jpa.metadata.core.edm.mapper.annotation;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.commons.api.edm.provider.CsdlTerm;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JsonIgnoreProperties(ignoreUnknown = true)
@JacksonXmlRootElement(localName = "Edmx", namespace = "edmx")
public class CsdlDocument {

  @JacksonXmlProperty(localName = "Reference")
  private EdmxReference[] reference;

  @JacksonXmlProperty(localName = "DataServices")
  private EdmxDataServices service;

  EdmxDataServices getDataService() {
    return service;
  }

  List<EdmxReference> getReference() {
    return Arrays.asList(reference);
  }

  public Map<String, CsdlSchema> getSchemas() {

    if (service != null) return service.getSchemas().stream()
        .collect(Collectors.toMap(Schema::getNamespace, Schema::asCsdlSchema));
    return Collections.emptyMap();
  }

  public Map<String, Map<String, CsdlTerm>> getTerms() {

    if (service != null) return service.getSchemas().stream()
        .collect(Collectors.toMap(
            Schema::getNamespace,
            schema -> schema.getTerms().stream()
                .collect(Collectors.toMap(CsdlTerm::getName, t -> t))));
    return Collections.emptyMap();
  }
}
