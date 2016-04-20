package org.apache.olingo.jpa.metadata.core.edm.mapper.extention;

import org.apache.olingo.commons.api.edm.provider.CsdlOnDelete;

public interface IntermediateNavigationPropertyAccess extends IntermediateModelItemAccess {
  public void setOnDelete(CsdlOnDelete onDelete);
}
