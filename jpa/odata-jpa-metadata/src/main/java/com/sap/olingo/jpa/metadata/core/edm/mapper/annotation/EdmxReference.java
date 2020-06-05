package com.sap.olingo.jpa.metadata.core.edm.mapper.annotation;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JsonIgnoreProperties(ignoreUnknown = true)
@JacksonXmlRootElement(localName = "Reference", namespace = "edmx")
public class EdmxReference {

  @JacksonXmlProperty(localName = "Uri")
  private String uri;

  @JacksonXmlProperty(localName = "Include")
  private EdmxReferenceInclude[] includes;

  public URI getUri() {
    return URI.create(uri);
  }

  public List<EdmxReferenceInclude> getIncludes() {
    return Arrays.asList(includes);
  }

}
