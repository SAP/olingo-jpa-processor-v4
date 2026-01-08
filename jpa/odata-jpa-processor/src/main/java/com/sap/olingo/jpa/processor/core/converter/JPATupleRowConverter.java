package com.sap.olingo.jpa.processor.core.converter;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import jakarta.persistence.Tuple;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.olingo.commons.api.data.ComplexValue;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.data.ValueType;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.uri.UriHelper;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPACollectionAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAElement;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntitySet;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAServiceDocument;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAStructuredType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.api.JPAODataPageExpandInfo;
import com.sap.olingo.jpa.processor.core.api.JPAODataRequestContextAccess;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessorException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAQueryException;
import com.sap.olingo.jpa.processor.core.query.JPAExpandQueryResult;
import com.sap.olingo.jpa.processor.core.serializer.JPAEntityCollectionExtension;
import com.sap.olingo.jpa.processor.core.serializer.JPAEntityCollectionLazy;

public class JPATupleRowConverter extends JPATupleResultConverter implements JPARowConverter {
  private static final Log LOGGER = LogFactory.getLog(JPATupleRowConverter.class);
  private final JPATupleCollectionConverter collectionConverter;
  private final JPATupleResultConverter childConverter;

  public JPATupleRowConverter(final JPAEntityType jpaEntity, final JPAServiceDocument sd, final UriHelper uriHelper,
      final ServiceMetadata serviceMetadata, final JPAODataRequestContextAccess requestContext)
      throws ODataApplicationException {
    super(sd, uriHelper, serviceMetadata, requestContext);
    this.setName = determineSetName(jpaEntity);
    this.jpaConversionTargetEntity = jpaEntity;
    this.edmType = determineEdmType(jpaEntity);
    this.collectionConverter = new JPATupleCollectionConverter(sd, uriHelper, serviceMetadata, requestContext);
    this.childConverter = new JPATupleChildConverter(sd, uriHelper, serviceMetadata, requestContext);
  }

  @Override
  public Entity convertRow(final JPAEntityType rowEntity, final Tuple row, final Collection<JPAPath> requestedSelection,
      final List<JPAODataPageExpandInfo> expandInfo, final JPAExpandResult result)
      throws ODataApplicationException {

    jpaQueryResult = result;
    final Map<String, ComplexValue> complexValueBuffer = new HashMap<>();
    final Entity odataEntity = new Entity();

    odataEntity.setType(edmType.getFullQualifiedName().getFullQualifiedNameAsString());
    final List<Property> properties = odataEntity.getProperties();
    final var navigationLinks = odataEntity.getNavigationLinks();
    // Creates and add the key of an entity. In general OData allows a server to add additional properties that are not
    // part of $select. As Olingo adds the key properties (with null) anyhow this can be done here already
    createId(rowEntity, row, odataEntity, expandInfo);
    createEtag(rowEntity, row, odataEntity);
    if (requestedSelection.isEmpty())
      convertRowWithOutSelection(rowEntity, row, complexValueBuffer, odataEntity, properties, expandInfo);
    else
      convertRowWithSelection(row, requestedSelection, complexValueBuffer, odataEntity, properties, expandInfo);
    createCollectionProperties(rowEntity, row, properties, result.getChildren());
    navigationLinks.addAll(createExpand(rowEntity, row, EMPTY_PREFIX, odataEntity.getId().toString(), expandInfo));
    return odataEntity;
  }

  private void createCollectionProperties(final JPAEntityType rowEntity, final Tuple row,
      final List<Property> properties, final Map<JPAAssociationPath, JPAExpandResult> children)
      throws ODataJPAQueryException {

    try {
      final var requestedCollections = getRequestedCollectionsProperties(rowEntity);
      for (final var requestedCollection : requestedCollections) {
        List<Property> result = properties;
        for (final JPAElement pathElement : requestedCollection.getPath()) {
          result = findOrCreateComplexProperty(result, pathElement);
        }
        final JPACollectionAttribute collection = (JPACollectionAttribute) requestedCollection.getLeaf();
        if (collection.isTransient()) {
          result.add(createTransientCollection(row, collection));
        } else {
          final JPACollectionResult child = (JPACollectionResult) children.get(collection.asAssociation());
          if (child != null) {
            result.add(createPersistedCollection(rowEntity, row, child, requestedCollection, collection));
          }
        }
      }
    } catch (ODataJPAModelException | ODataApplicationException e) {
      throw new ODataJPAQueryException(ODataJPAQueryException.MessageKeys.QUERY_RESULT_CONV_ERROR,
          HttpStatusCode.INTERNAL_SERVER_ERROR, e);
    }
  }

  private Property createPersistedCollection(final JPAEntityType rowEntity, final Tuple row,
      final JPAExpandResult child, final JPAPath requestedCollection, final JPACollectionAttribute collection)
      throws ODataJPAModelException, ODataApplicationException {

    final JPAStructuredType st = determineCollectionRoot(rowEntity, requestedCollection.getPath());
    final var dbResult = child.removeResult(buildConcatenatedKey(row, collection.asAssociation()
        .getLeftColumnsList()));
    final List<Object> collectionResult;
    if (dbResult != null) {
      final String prefix = determinePrefix(collection.asAssociation().getAlias());
      collectionResult = collectionConverter.getResult(child.getRequestedSelection(), collection
          .asAssociation(), st, prefix, dbResult);
    } else {
      collectionResult = null;
    }
    return new Property(
        null,
        collection.getExternalName(),
        collection.isComplex() ? ValueType.COLLECTION_COMPLEX : ValueType.COLLECTION_PRIMITIVE,
        collectionResult != null ? collectionResult : Collections.emptyList());
  }

  private Property createTransientCollection(final Tuple row, final JPACollectionAttribute collection)
      throws ODataJPAProcessorException {

    return requestContext.getCalculator(collection)
        .map(c -> c.calculateCollectionProperty(row))
        .map(collectionResult -> new Property(
            null,
            collection.getExternalName(),
            collection.isComplex() ? ValueType.COLLECTION_COMPLEX : ValueType.COLLECTION_PRIMITIVE,
            collectionResult))
        .orElseGet(() -> {
          LOGGER.warn("No transient property calculator found for" + collection.getInternalName());
          return new Property(
              null,
              collection.getExternalName(),
              collection.isComplex() ? ValueType.COLLECTION_COMPLEX : ValueType.COLLECTION_PRIMITIVE,
              List.of());
        });
  }

  private List<JPAPath> getRequestedCollectionsProperties(final JPAStructuredType rowEntity)
      throws ODataJPAModelException {
    return rowEntity.getCollectionAttributesPath();
  }

  protected void createId(final JPAEntityType rowEntity, final Tuple row, final Entity odataEntity,
      final List<JPAODataPageExpandInfo> expandInfo) throws ODataApplicationException {

    final Map<String, ComplexValue> complexValueBuffer = Collections.emptyMap();
    try {
      for (final JPAAttribute path : rowEntity.getKey()) {
        convertAttribute(row.get(path.getExternalName()), rowEntity.getPath(path.getExternalName()), complexValueBuffer,
            odataEntity.getProperties(), row, EMPTY_PREFIX, null, expandInfo);
      }
    } catch (final ODataJPAModelException e) {
      throw new ODataJPAQueryException(ODataJPAQueryException.MessageKeys.QUERY_RESULT_CONV_ERROR,
          HttpStatusCode.INTERNAL_SERVER_ERROR, e);
    }
    odataEntity.setId(createId(odataEntity));
  }

  protected String determineSetName(@Nonnull final JPAEntityType jpaEntityType) throws ODataJPAQueryException {

    try {
      final JPAEntitySet es = sd.getEntitySet(jpaEntityType);
      return es != null ? es.getExternalName() : "";
    } catch (final ODataJPAModelException e) {
      throw new ODataJPAQueryException(ODataJPAQueryException.MessageKeys.QUERY_RESULT_ENTITY_SET_ERROR,
          HttpStatusCode.INTERNAL_SERVER_ERROR, e, jpaEntityType.getExternalFQN()
              .getFullQualifiedNameAsString());
    }
  }

  protected EdmEntityType determineEdmType(final JPAEntityType jpaEntityType) {
    return serviceMetadata.getEdm().getEntityType(jpaEntityType.getExternalFQN());
  }

  @Override
  public Object getResult(final JPAExpandResult jpaResult, final Collection<JPAPath> requestedSelection)
      throws ODataApplicationException {
    return null;
  }

  @Override
  public Map<String, List<Object>> getCollectionResult(final JPACollectionResult jpaResult,
      final Collection<JPAPath> requestedSelection) throws ODataApplicationException {
    return new JPATupleCollectionConverter(sd, uriHelper, serviceMetadata, requestContext)
        .getResult(jpaResult, requestedSelection);
  }

  @Override
  public JPAEntityCollectionExtension getResult(final JPAExpandQueryResult jpaExpandQueryResult,
      final Collection<JPAPath> requestedSelection, final String parentKey,
      final List<JPAODataPageExpandInfo> expandInfo) throws ODataApplicationException {

    if (JPAExpandResult.ROOT_RESULT_KEY.equals(parentKey))
      return new JPAEntityCollectionLazy(jpaExpandQueryResult, this);
    return childConverter.getResult(jpaExpandQueryResult, requestedSelection, parentKey, expandInfo);
  }

  @Override
  protected Log getLogger() {
    return LOGGER;
  }

}
