package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.commons.api.edm.provider.CsdlTerm;
import org.apache.olingo.commons.api.edmx.EdmxReference;
import org.apache.olingo.commons.api.edmx.EdmxReferenceInclude;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.sap.olingo.jpa.metadata.core.edm.mapper.annotation.SchemaReader;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException.MessageKeys;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extention.IntermediateReferenceList;

class IntermediateReferences implements IntermediateReferenceList {
  final List<IntermediateReference> references = new ArrayList<IntermediateReference>();
  final List<EdmxReference> edmxReferences = new ArrayList<EdmxReference>();
  final Map<String, Map<String, CsdlTerm>> terms = new HashMap<String, Map<String, CsdlTerm>>();
  final Map<String, CsdlSchema> schemas = new HashMap<String, CsdlSchema>();
  final Map<String, String> aliasDirectory = new HashMap<String, String>();

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
    if (path != null && !path.isEmpty()) {
      try {
        Map<? extends String, ? extends CsdlSchema> newSchemas = new SchemaReader().getSchemas(path);
        schemas.putAll(newSchemas);
        extractTerms(newSchemas);
      } catch (JsonParseException e) {
        // Parsing of %1$s failed with message %2$s
        throw new ODataJPAModelException(MessageKeys.ANNOTATION_PARSE_ERROR, e, path, e.getMessage());
      } catch (JsonMappingException e) {
        // Parsing of %1$s failed with message %2$s
        throw new ODataJPAModelException(MessageKeys.ANNOTATION_PARSE_ERROR, e, path, e.getMessage());
      } catch (IOException e) {
        // Parsing of %1$s failed with message %2$s
        throw new ODataJPAModelException(MessageKeys.ANNOTATION_PARSE_ERROR, e, path, e.getMessage());
      }
    }
    references.add(reference);
    return reference;
  }

  private void extractTerms(Map<? extends String, ? extends CsdlSchema> newSchemas) {
    for (Entry<String, CsdlSchema> schema : schemas.entrySet()) {
      Map<String, CsdlTerm> schemaTerms = new HashMap<String, CsdlTerm>();
      for (CsdlTerm term : schema.getValue().getTerms()) {
        schemaTerms.put(term.getName(), term);
      }
      terms.put(schema.getKey(), schemaTerms);
    }
  }

  public CsdlTerm getTerm(FullQualifiedName termName) {
    Map<String, CsdlTerm> schema = terms.get(termName.getNamespace());
    if (schema == null) {
      for (IntermediateReference r : references) {
        String namespace = r.convertAlias(termName.getNamespace());
        if (namespace != null) {
          schema = terms.get(namespace);
        }
      }
    }
    return schema.get(termName.getName());
  }

  public List<CsdlSchema> getSchemas() {

    List<CsdlSchema> result = new ArrayList<CsdlSchema>();
    for (Entry<String, CsdlSchema> schema : schemas.entrySet()) {
      result.add(schema.getValue());
    }
    return result;
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

    String convertAlias(String alias) {
      return aliasDirectory.get(alias);
    }

    @Override
    public void addInclude(String namespace) {
      addInclude(namespace, null);
    }

    @Override
    public void addInclude(final String namespace, final String alias) {
      IntermediateReferenceInclude include = new IntermediateReferenceInclude(namespace, alias);
      this.includes.add(include);
      edmxReference.addInclude(include.getEdmInclude());
      aliasDirectory.put(alias, namespace);
    }

    @Override
    public String getPath() {
      return path;
    }

    @Override
    public URI getURI() {
      return uri;
    }

    EdmxReference getEdmReference() {
      return edmxReference;
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
