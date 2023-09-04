package com.sap.olingo.jpa.metadata.core.edm.mapper.vocabularies;

import org.apache.olingo.commons.api.edm.provider.CsdlNamed;

interface Named {
  public String getName();

  public CsdlNamed setName(final String name);
}
