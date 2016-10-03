package org.apache.olingo.jpa.metadata.core.edm.mapper.annotation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

@JsonIgnoreProperties({ "Annotation" })
class Term {

  @JacksonXmlProperty(localName = "Name", isAttribute = true)
  private String name;

  @JacksonXmlProperty(localName = "Type", isAttribute = true)
  private String type;

  @JacksonXmlProperty(localName = "BaseTerm", isAttribute = true)
  private String baseTerm;

  @JacksonXmlProperty(localName = "Nullable", isAttribute = true)
  private boolean nullable;

  @JacksonXmlProperty(localName = "DefaultValue", isAttribute = true)
  private String defaultValue;

  @JacksonXmlProperty(localName = "AppliesTo", isAttribute = true)
  private String appliesTo;

  @JacksonXmlProperty(localName = "MaxLength", isAttribute = true)
  private int maxLength;

  @JacksonXmlProperty(localName = "Precision", isAttribute = true)
  private int precision;

  @JacksonXmlProperty(localName = "Scale", isAttribute = true)
  private int scale;

  @JacksonXmlProperty(localName = "SRID", isAttribute = true)
  private int srid;
}
