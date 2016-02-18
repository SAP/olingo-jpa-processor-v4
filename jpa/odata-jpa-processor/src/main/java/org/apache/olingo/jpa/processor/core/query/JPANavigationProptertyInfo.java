package org.apache.olingo.jpa.processor.core.query;

import org.apache.olingo.jpa.metadata.core.edm.mapper.impl.JPAAssociationPath;
import org.apache.olingo.server.api.uri.UriResourcePartTyped;

public class JPANavigationProptertyInfo {
  private final UriResourcePartTyped navigationTarget;
  private final JPAAssociationPath associationPath;

  public JPANavigationProptertyInfo(final UriResourcePartTyped uriResiource, final JPAAssociationPath associationPath) {
    super();
    this.navigationTarget = uriResiource;
    this.associationPath = associationPath;
  }

  public UriResourcePartTyped getUriResiource() {
    return navigationTarget;
  }

  public JPAAssociationPath getAssociationPath() {
    return associationPath;
  }

}
