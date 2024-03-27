package com.sap.olingo.jpa.metadata.odata.v4.provider;

import java.net.URI;
import java.net.URISyntaxException;

import com.sap.olingo.jpa.metadata.odata.v4.capabilities.terms.FilterExpressionType;

public class JavaBasedCapabilitiesAnnotationsProvider extends JavaBasedODataAnnotationsProvider {
//
  static final String NAMESPACE = "Org.OData.Capabilities.V1";
  static final String ALIAS = "Capabilities";
  static final String PATH = "vocabularies/Org.OData.Capabilities.V1.xml";

  public JavaBasedCapabilitiesAnnotationsProvider() {
    this(new JavaAnnotationConverter());
  }

  public JavaBasedCapabilitiesAnnotationsProvider(final JavaAnnotationConverter converter) {
    super(converter, FilterExpressionType.class.getPackage().getName());
  }

  @Override
  String getAlias() {
    return ALIAS;
  }

  @Override
  String getNameSpace() {
    return NAMESPACE;
  }

  @Override
  String getPath() {
    return PATH;
  }

  @Override
  URI getUri() throws URISyntaxException {
    return new URI(
        "https://raw.githubusercontent.com/oasis-tcs/odata-vocabularies/main/vocabularies/Org.OData.Capabilities.V1.xml");// NOSONAR
  }

}
