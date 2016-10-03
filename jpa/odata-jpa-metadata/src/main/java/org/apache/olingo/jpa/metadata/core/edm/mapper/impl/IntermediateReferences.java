package org.apache.olingo.jpa.metadata.core.edm.mapper.impl;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.olingo.commons.api.edmx.EdmxReference;
import org.apache.olingo.commons.api.edmx.EdmxReferenceInclude;
import org.apache.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import org.apache.olingo.jpa.metadata.core.edm.mapper.extention.IntermediateReferenceList;

class IntermediateReferences implements IntermediateReferenceList {
  final List<IntermediateReference> references = new ArrayList<IntermediateReference>();
  final List<EdmxReference> edmxReferences = new ArrayList<EdmxReference>();

  @Override
  public IntermediateReferenceAccess addReference(final String uri) throws ODataJPAModelException {
    return addReference(uri, null);
  }

  @Override
  public IntermediateReferenceAccess addReference(final String uri, final String path) throws ODataJPAModelException {
    URI sourceURI;
    try {
      sourceURI = new URI(uri);
    } catch (URISyntaxException e) {
      throw new ODataJPAModelException(e);
    }
    IntermediateReference reference = new IntermediateReference(sourceURI, path);
    references.add(reference);
    return reference;
  }

  List<EdmxReference> getEdmReferences() {
    if (references.size() != edmxReferences.size()) {
      edmxReferences.removeAll(edmxReferences);
      for (IntermediateReference r : references) {
        edmxReferences.add(r.getEdmReference());
      }
    }
    return edmxReferences;
  }

  private class IntermediateReference implements IntermediateReferenceList.IntermediateReferenceAccess {
    final private URI uri;
    final private String path;
    final EdmxReference edmxReference;
    final private List<IntermediateReferenceInclude> includes = new ArrayList<IntermediateReferenceInclude>();

    public IntermediateReference(final URI uri, final String path) {
      super();
      this.uri = uri;
      this.path = path;
      edmxReference = new EdmxReference(uri);
    }

    EdmxReference getEdmReference() {
      return edmxReference;
    }

    @Override
    public void addInclude(final String namespace, final String alias) {
      IntermediateReferenceInclude include = new IntermediateReferenceInclude(namespace, alias);
      this.includes.add(include);
      edmxReference.addInclude(include.getEdmInclude());
    }

    @Override
    public void addInclude(String namespace) {
      addInclude(namespace, null);
    }

    @Override
    public String getPath() {
      return path;
    }

    @Override
    public URI getURI() {
      return uri;
    }

    private class IntermediateReferenceInclude {
      private final String namespace;
      private final String alias;

      public IntermediateReferenceInclude(final String namespace, final String alias) {
        this.namespace = namespace;
        this.alias = alias;
      }

      EdmxReferenceInclude getEdmInclude() {
        return new EdmxReferenceInclude(namespace, alias);
      }
    }
  }
}
