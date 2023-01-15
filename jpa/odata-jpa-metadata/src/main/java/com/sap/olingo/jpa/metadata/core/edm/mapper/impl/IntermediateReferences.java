package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import static com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException.MessageKeys.ANNOTATION_PARSE_ERROR;
import static com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException.MessageKeys.ANNOTATION_PATH_NOT_FOUND;
import static com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException.MessageKeys.MISSING_TERM_NAMESPACE;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import javax.annotation.Nonnull;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlNamed;
import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.commons.api.edm.provider.CsdlTerm;
import org.apache.olingo.commons.api.edmx.EdmxReference;
import org.apache.olingo.commons.api.edmx.EdmxReferenceInclude;
import org.apache.olingo.commons.api.edmx.EdmxReferenceIncludeAnnotation;

import com.sap.olingo.jpa.metadata.core.edm.mapper.annotation.CsdlDocument;
import com.sap.olingo.jpa.metadata.core.edm.mapper.annotation.CsdlDocumentReader;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extension.IntermediateReferenceList;

final class IntermediateReferences implements IntermediateReferenceList {

  final List<IntermediateReference> references = new ArrayList<>();
  private List<EdmxReference> edmxReferences = new ArrayList<>();
  final Map<String, Map<String, CsdlTerm>> terms = new HashMap<>();
  final Map<String, CsdlSchema> schemas = new HashMap<>();
  final Map<String, String> aliasDirectory = new HashMap<>();

  @Override
  public IntermediateReferenceAccess addReference(final String uri) throws ODataJPAModelException {

    try {
      final URI sourceURI = new URI(uri);
      final CsdlDocument vocabulary = new CsdlDocumentReader().readFromURI(sourceURI);
      return createReference(sourceURI, "", vocabulary);
    } catch (final URISyntaxException e) {
      throw new ODataJPAModelException(e);
    } catch (final IOException e) {
      throw new ODataJPAModelException(ANNOTATION_PARSE_ERROR, e, uri, e.getMessage());
    }
  }

  @Override
  public IntermediateReferenceAccess addReference(@Nonnull final String uri, @Nonnull final String path)
      throws ODataJPAModelException {

    return addReference(uri, path, Charset.defaultCharset());
  }

  @Override
  public IntermediateReferenceAccess addReference(@Nonnull final String uri, @Nonnull final String path,
      @Nonnull final Charset charset) throws ODataJPAModelException {

    try {
      final URI sourceURI = new URI(uri);
      final CsdlDocument vocabulary = new CsdlDocumentReader().readFromResource(path, charset);
      if (vocabulary == null)
        // Path '%1$s' to read the file containing vocabulary '%2$s' is wrong
        throw new ODataJPAModelException(ANNOTATION_PATH_NOT_FOUND, path, uri);
      return createReference(sourceURI, path, vocabulary);
    } catch (final URISyntaxException e) {
      throw new ODataJPAModelException(e);
    } catch (final IOException e) {
      // Parsing of %1$s failed with message %2$s
      throw new ODataJPAModelException(ANNOTATION_PARSE_ERROR, e, path, e.getMessage());
    }
  }

  public List<CsdlSchema> getSchemas() {
    final List<CsdlSchema> result = new ArrayList<>();
    for (final Entry<String, CsdlSchema> schema : schemas.entrySet())
      result.add(schema.getValue());
    return result;
  }

  public CsdlTerm getTerm(final FullQualifiedName termName) {
    Map<String, CsdlTerm> termsMap = terms.get(termName.getNamespace());
    if (termsMap == null) {
      final String namespace = convertAlias(termName.getNamespace());
      if (namespace != null) termsMap = terms.get(namespace);
    }
    if (termsMap == null)
      return null;
    return termsMap.get(termName.getName());
  }

  @SuppressWarnings("unchecked")
  public <T extends CsdlNamed> Optional<T> getType(final FullQualifiedName fqn) {
    // namespace of fqn can either be the namespace of the schema or the alias of the schema
    final Optional<CsdlSchema> schema = Optional.ofNullable(Optional.ofNullable(schemas.get(fqn.getNamespace()))
        .orElseGet(() -> schemas.get(convertAlias(fqn.getNamespace()))));
    final Optional<T> complexType = schema.map(s -> (T) s.getComplexType(fqn.getName()));
    if (!complexType.isPresent()) {
      final Optional<T> simpleType = schema.map(s -> (T) s.getTypeDefinition(fqn.getName()));
      if (!simpleType.isPresent())
        return schema.map(s -> (T) s.getEnumType(fqn.getName()));
      return simpleType;
    }
    return complexType;
  }

  String convertAlias(final String alias) {
    return aliasDirectory.get(alias);
  }

  List<EdmxReference> getEdmReferences() {
    if (references.size() != edmxReferences.size()) {
      edmxReferences = new ArrayList<>(references.size());
      for (final IntermediateReference r : references)
        edmxReferences.add(r.getEdmReference());
    }
    return edmxReferences;
  }

  Optional<CsdlSchema> getSchema(final String namespace) {
    return Optional.ofNullable(Optional.ofNullable(schemas.get(namespace))
        .orElseGet(() -> schemas.get(convertAlias(namespace))));
  }

  private IntermediateReference createReference(final URI sourceURI, final String path, final CsdlDocument vocabulary) {

    final IntermediateReference reference = new IntermediateReference(sourceURI, path);
    schemas.putAll(vocabulary.getSchemas());
    terms.putAll(vocabulary.getTerms());
    references.add(reference);
    aliasDirectory.putAll(getAliases(vocabulary.getSchemas()));
    return reference;
  }

  private Map<String, String> getAliases(final Map<String, CsdlSchema> schemas) {
    final Map<String, String> aliases = new HashMap<>();
    for (final Entry<String, CsdlSchema> schema : schemas.entrySet()) {
      if (schema.getValue().getAlias() != null)
        aliases.put(schema.getValue().getAlias(), schema.getKey());
    }
    return aliases;
  }

  private class IntermediateReference implements IntermediateReferenceList.IntermediateReferenceAccess {
    private final URI uri;
    private final String path;
    final EdmxReference edmxReference;
    private final List<IntermediateReferenceInclude> includes = new ArrayList<>();
    private final List<IntermediateReferenceAnnotationInclude> annotation = new ArrayList<>();

    public IntermediateReference(final URI uri, final String path) {
      super();
      this.uri = uri;
      this.path = path;
      edmxReference = new EdmxReference(uri);
    }

    @Override
    public void addInclude(final String namespace) {
      addInclude(namespace, null);
    }

    @Override
    public void addInclude(final String namespace, final String alias) {
      final IntermediateReferenceInclude include = new IntermediateReferenceInclude(namespace, alias);
      this.includes.add(include);
      edmxReference.addInclude(include.getEdmInclude());
      if (alias != null)
        aliasDirectory.put(alias, namespace);
    }

    @Override
    public void addIncludeAnnotation(final String termNamespace) throws ODataJPAModelException {
      addIncludeAnnotation(termNamespace, null, null);
    }

    @Override
    public void addIncludeAnnotation(@Nonnull final String termNamespace, final String qualifier,
        final String targetNamespace) throws ODataJPAModelException {

      final IntermediateReferenceAnnotationInclude include = new IntermediateReferenceAnnotationInclude(termNamespace,
          qualifier, targetNamespace);
      this.annotation.add(include);
      edmxReference.addIncludeAnnotation(include.getEdmIncludeAnnotation());
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

    private class IntermediateReferenceAnnotationInclude {

      private final String termNamespace;
      private final String qualifier;
      private final String targetNamespace;

      public IntermediateReferenceAnnotationInclude(final String termNamespace, final String qualifier,
          final String targetNamespace) throws ODataJPAModelException {

        super();
        if (termNamespace == null || termNamespace.isEmpty())
          throw new ODataJPAModelException(MISSING_TERM_NAMESPACE);
        this.termNamespace = termNamespace;
        this.qualifier = qualifier;
        this.targetNamespace = targetNamespace;
      }

      EdmxReferenceIncludeAnnotation getEdmIncludeAnnotation() {
        return new EdmxReferenceIncludeAnnotation(termNamespace, qualifier, targetNamespace);
      }
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