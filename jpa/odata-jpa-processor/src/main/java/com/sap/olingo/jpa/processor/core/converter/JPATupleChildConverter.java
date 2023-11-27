package com.sap.olingo.jpa.processor.core.converter;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import javax.annotation.Nonnull;

import jakarta.persistence.Tuple;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.olingo.commons.api.data.ComplexValue;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.data.ValueType;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.ex.ODataRuntimeException;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.serializer.SerializerException;
import org.apache.olingo.server.api.uri.UriHelper;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmTransientPropertyCalculator;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPACollectionAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAElement;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntitySet;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAServiceDocument;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAStructuredType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.api.JPAODataRequestContextAccess;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessorException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAQueryException;

/**
 * Converts the query result based on Tuples from JPA format into Olingo format.
 * <p>
 * To reduce the memory footprint each converted row is set to null. This is done as currently the query result is
 * stored in an ArrayList and deleting a row, which is not the last row, leads to an array copy, which can consume a lot
 * of time. For the same reason no trimToSize() is called. As an alternative to an ArrayList also a simple linked list
 * could be used, but this comes with the draw back that each entry would consume round about double space in the list.
 * @author Oliver Grande
 *
 */
public class JPATupleChildConverter extends JPATupleResultConverter {
  private static final Log LOGGER = LogFactory.getLog(JPATupleChildConverter.class);

  public JPATupleChildConverter(final JPAServiceDocument sd, final UriHelper uriHelper,
      final ServiceMetadata serviceMetadata, final JPAODataRequestContextAccess requestContext) {

    super(sd, uriHelper, serviceMetadata, requestContext);
  }

  public JPATupleChildConverter(final JPATupleChildConverter converter) {
    this(converter.sd, converter.uriHelper, converter.serviceMetadata, converter.requestContext);
  }

  public Map<String, List<Object>> getCollectionResult(final JPACollectionResult jpaResult,
      final Collection<JPAPath> requestedSelection) throws ODataApplicationException {

    return new JPATupleCollectionConverter(sd, uriHelper, serviceMetadata, requestContext)
        .getResult(jpaResult, requestedSelection);
  }

  @Override
  public Map<String, EntityCollection> getResult(@Nonnull final JPAExpandResult jpaResult,
      @Nonnull final Collection<JPAPath> requestedSelection) throws ODataApplicationException {

    jpaQueryResult = jpaResult;
    this.setName = determineSetName(jpaQueryResult);
    this.jpaConversionTargetEntity = jpaQueryResult.getEntityType();
    this.edmType = determineEdmType();
    final Map<String, List<Tuple>> childResult = jpaResult.getResults();

    final Map<String, EntityCollection> result = new HashMap<>(childResult.size());
    for (final Entry<String, List<Tuple>> tuple : childResult.entrySet()) {
      final EntityCollection entityCollection = new EntityCollection();
      final List<Entity> entities = entityCollection.getEntities();
      final List<Tuple> rows = tuple.getValue();

      for (int i = 0; i < rows.size(); i++) {
        final Tuple row = rows.set(i, null);
        final Entity odataEntity = convertRow(jpaConversionTargetEntity, row, requestedSelection);
        odataEntity.setMediaContentType(determineContentType(jpaConversionTargetEntity, row));
        entities.add(odataEntity);
      }
      result.put(tuple.getKey(), entityCollection);
    }
    childResult.replaceAll((key, value) -> null);
    return result;
  }

  protected Entity convertRow(final JPAEntityType rowEntity, final Tuple row,
      final Collection<JPAPath> requestedSelection) throws ODataApplicationException {

    final Map<String, ComplexValue> complexValueBuffer = new HashMap<>();
    final Entity odataEntity = new Entity();

    odataEntity.setType(edmType.getFullQualifiedName().getFullQualifiedNameAsString());
    final List<Property> properties = odataEntity.getProperties();
    // Creates and add the key of an entity. In general OData allows a server to add additional properties that are not
    // part of $select. As Olingo adds the key properties (with null) anyhow this can be done here already
    createId(rowEntity, row, odataEntity);
    createEtag(rowEntity, row, odataEntity);
    if (requestedSelection.isEmpty())
      convertRowWithOutSelection(rowEntity, row, complexValueBuffer, odataEntity, properties);
    else
      convertRowWithSelection(row, requestedSelection, complexValueBuffer, odataEntity, properties);
    createCollectionProperties(rowEntity, row, properties);
    odataEntity.getNavigationLinks().addAll(createExpand(rowEntity, row, EMPTY_PREFIX, odataEntity.getId().toString()));

    return odataEntity;
  }

  protected void createCollectionProperties(final JPAStructuredType jpaStructuredType, final Tuple row,
      final List<Property> properties) throws ODataJPAQueryException {

    List<Property> result;
    try {
      for (final JPAPath path : jpaStructuredType.getCollectionAttributesPath()) {
        result = properties;
        for (final JPAElement pathElement : path.getPath()) {
          result = findOrCreateComplexProperty(result, pathElement);
        }
        final JPACollectionAttribute collection = (JPACollectionAttribute) path.getLeaf();
        if (collection.isTransient()) {
          addTransientCollection(row, result, collection);
        } else {
          final JPAExpandResult child = jpaQueryResult.getChild(collection.asAssociation());
          if (child != null) {
            addCollection(row, result, collection, child);
          }
        }
      }
    } catch (ODataJPAModelException | ODataJPAProcessorException e) {
      throw new ODataJPAQueryException(ODataJPAQueryException.MessageKeys.QUERY_RESULT_CONV_ERROR,
          HttpStatusCode.INTERNAL_SERVER_ERROR, e);
    }
  }

  /**
   * @param row
   * @param collection
   * @throws ODataJPAProcessorException
   */
  private void addTransientCollection(final Tuple row, final List<Property> result,
      final JPACollectionAttribute collection) throws ODataJPAProcessorException {

    final Optional<EdmTransientPropertyCalculator<?>> calculator = requestContext.getCalculator(collection);
    if (calculator.isPresent()) {
      final Collection<?> collectionResult = calculator.get().calculateCollectionProperty(row);
      result.add(new Property(
          null,
          collection.getExternalName(),
          collection.isComplex() ? ValueType.COLLECTION_COMPLEX : ValueType.COLLECTION_PRIMITIVE,
          collectionResult));
    }
  }

  private void addCollection(final Tuple row, final List<Property> result, final JPACollectionAttribute collection,
      final JPAExpandResult child) throws ODataJPAModelException {

    final Collection<Object> collectionResult = ((JPACollectionResult) child).getPropertyCollection(
        buildConcatenatedKey(row, collection.asAssociation().getLeftColumnsList()));

    result.add(new Property(
        null,
        collection.getExternalName(),
        collection.isComplex() ? ValueType.COLLECTION_COMPLEX : ValueType.COLLECTION_PRIMITIVE,
        collectionResult != null ? collectionResult : Collections.emptyList()));
  }

  private List<Property> findOrCreateComplexProperty(List<Property> result, final JPAElement pathElement)
      throws ODataJPAModelException {
    boolean found = false;
    for (final Property p : result) {
      if (p.getName().equals(pathElement.getExternalName())) {
        result = ((ComplexValue) p.getValue()).getValue();
        found = true;
        break;
      }
    }
    if (!found
        && pathElement instanceof final JPAAttribute attribute
        && attribute.isComplex()
        && !attribute.isCollection()) {
      final Property path = new Property(
          attribute.getStructuredType().getExternalFQN().getFullQualifiedNameAsString(),
          attribute.getExternalName(),
          ValueType.COMPLEX,
          new ComplexValue());
      result.add(path);
      result = ((ComplexValue) path.getValue()).getValue();
    }
    return result;
  }

  protected URI createId(final Entity entity) {

    try {
      // No host-name and port as part of ID; only relative path
      // http://docs.oasis-open.org/odata/odata-atom-format/v4.0/cs02/odata-atom-format-v4.0-cs02.html#_Toc372792702

      final StringBuilder uriString = new StringBuilder(setName);
      uriString.append("(");
      uriString.append(uriHelper.buildKeyPredicate(edmType, entity));
      uriString.append(")");
      return new URI(uriString.toString());
    } catch (final URISyntaxException e) {
      throw new ODataRuntimeException("Unable to create id for entity: " + edmType.getName(), e);
    } catch (final IllegalArgumentException e) {
      LOGGER.debug(e.getMessage() + ": No URI created");
      return null;
    } catch (final SerializerException e) {
      throw new ODataRuntimeException(e);
    }
  }

  protected void createId(final JPAEntityType rowEntity, final Tuple row, final Entity odataEntity)
      throws ODataApplicationException {

    final Map<String, ComplexValue> complexValueBuffer = Collections.emptyMap();
    try {
      for (final JPAAttribute path : rowEntity.getKey()) {
        convertAttribute(row.get(path.getExternalName()), rowEntity.getPath(path.getExternalName()), complexValueBuffer,
            odataEntity.getProperties(), row, EMPTY_PREFIX, null);
      }
    } catch (final ODataJPAModelException e) {
      throw new ODataJPAQueryException(ODataJPAQueryException.MessageKeys.QUERY_RESULT_CONV_ERROR,
          HttpStatusCode.INTERNAL_SERVER_ERROR, e);
    }
    odataEntity.setId(createId(odataEntity));
  }

  private void createEtag(@Nonnull final JPAEntityType rowEntity, final Tuple row, final Entity odataEntity)
      throws ODataJPAQueryException {

    try {
      if (rowEntity.hasEtag()) {
        final String etagAlias = rowEntity.getEtagPath().getAlias();
        final Object etag = row.get(etagAlias);
        if (etag != null) {
          odataEntity.setETag(etag.toString());
        }
      }

    } catch (final ODataJPAModelException e) {
      throw new ODataJPAQueryException(ODataJPAQueryException.MessageKeys.QUERY_RESULT_CONV_ERROR,
          HttpStatusCode.INTERNAL_SERVER_ERROR, e);
    }
  }

  protected EdmEntityType determineEdmType() {
    return serviceMetadata.getEdm().getEntityType(jpaQueryResult.getEntityType().getExternalFQN());
  }

  /**
   * Returns the name of the first entity set that point to the entity type mentioned in <code>jpaQueryResult</code>
   * @param jpaQueryResult
   * @return
   * @throws ODataJPAQueryException
   */
  protected String determineSetName(@Nonnull final JPAExpandResult jpaQueryResult)
      throws ODataJPAQueryException {
    try {
      final JPAEntitySet es = sd.getEntitySet(jpaQueryResult.getEntityType());
      return es != null ? es.getExternalName() : "";
    } catch (final ODataJPAModelException e) {
      throw new ODataJPAQueryException(ODataJPAQueryException.MessageKeys.QUERY_RESULT_ENTITY_SET_ERROR,
          HttpStatusCode.INTERNAL_SERVER_ERROR, e, jpaQueryResult.getEntityType().getExternalFQN()
              .getFullQualifiedNameAsString());
    }
  }

  private String determineContentType(final JPAEntityType jpaEntity, final Tuple row) throws ODataJPAQueryException {

    try {
      if (jpaEntity.hasStream()) {
        if (jpaEntity.getContentType() != null && !jpaEntity.getContentType().isEmpty()) {
          return jpaEntity.getContentType();
        } else {
          Object rowElement = null;
          for (final JPAElement element : jpaEntity.getContentTypeAttributePath().getPath()) {
            rowElement = row.get(element.getExternalName());
          }
          return rowElement != null ? rowElement.toString() : null;
        }
      }
      return null;
    } catch (final ODataJPAModelException e) {
      throw new ODataJPAQueryException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
    }
  }
}
