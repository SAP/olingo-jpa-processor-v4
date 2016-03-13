package org.apache.olingo.jpa.metadata.core.edm.mapper.api;

import org.apache.olingo.commons.api.edm.FullQualifiedName;

public interface JPAElement {
  public String getExternalName();

  public String getInternalName();

  public FullQualifiedName getExternalFQN();
}
