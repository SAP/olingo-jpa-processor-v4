package com.sap.olingo.jpa.metadata.odata.v4.provider;

import java.net.URI;
import java.net.URISyntaxException;

import com.sap.olingo.jpa.metadata.odata.v4.core.terms.Immutable;

public class JavaBasedCoreAnnotationsProvider extends JavaBasedODataAnnotationsProvider {

  static final String NAMESPACE = "Org.OData.Core.V1";
  static final String ALIAS = "Core";
  static final String PATH = "vocabularies/Org.OData.Core.V1.xml";

  public JavaBasedCoreAnnotationsProvider() {
    this(new JavaAnnotationConverter());
  }

  public JavaBasedCoreAnnotationsProvider(final JavaAnnotationConverter converter) {
    super(converter, Immutable.class.getPackage().getName());
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
        "https://raw.githubusercontent.com/oasis-tcs/odata-vocabularies/main/vocabularies/Org.OData.Core.V1.xml");// NOSONAR
  }

}
