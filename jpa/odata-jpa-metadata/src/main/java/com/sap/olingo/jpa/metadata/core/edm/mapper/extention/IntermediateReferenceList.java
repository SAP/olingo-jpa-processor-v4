package com.sap.olingo.jpa.metadata.core.edm.mapper.extention;

import java.net.URI;
import java.nio.charset.Charset;

import javax.annotation.Nonnull;

import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;

public interface IntermediateReferenceList {

  public IntermediateReferenceAccess addReference(@Nonnull final String uri) throws ODataJPAModelException;

  public IntermediateReferenceAccess addReference(@Nonnull final String uri, @Nonnull final String path)
      throws ODataJPAModelException;

  public IntermediateReferenceAccess addReference(@Nonnull final String uri, @Nonnull final String path,
      @Nonnull final Charset charset) throws ODataJPAModelException;

  public interface IntermediateReferenceAccess {
    public URI getURI();

    public String getPath();

    public void addInclude(final String namespace, final String alias);

    public void addInclude(final String namespace);
  }
}
