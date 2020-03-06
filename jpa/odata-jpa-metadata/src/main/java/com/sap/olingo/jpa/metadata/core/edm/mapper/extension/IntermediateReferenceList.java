package com.sap.olingo.jpa.metadata.core.edm.mapper.extension;

import java.net.URI;

import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;

public interface IntermediateReferenceList {
  public IntermediateReferenceAccess addReference(final String uri) throws ODataJPAModelException;

  public IntermediateReferenceAccess addReference(final String uri, final String path) throws ODataJPAModelException;

  public interface IntermediateReferenceAccess {
    public URI getURI();

    public String getPath();

    public void addInclude(final String namespace, final String alias);

    public void addInclude(final String namespace);
  }
}