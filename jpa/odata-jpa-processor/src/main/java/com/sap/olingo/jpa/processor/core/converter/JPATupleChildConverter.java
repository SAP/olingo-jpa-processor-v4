package com.sap.olingo.jpa.processor.core.converter;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.Tuple;
import javax.persistence.TupleElement;

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

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPACollectionAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAElement;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntitySet;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAServiceDocument;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAStructuredType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAQueryException;

/**
 * Converts the query result based on Tuples from JPA format into Olingo format.<p>
 * To reduce the memory footprint each converted row is set to null. This is done as currently the query result is
 * stored in an ArrayList and deleting a row, which is not the last row, leads to an array copy, which can consume a lot
 * of time. For the same reason no trimToSize() is called. As an alternative to an ArrayList also a simple linked list
 * could be used, but this comes with the draw back that each entry would consume round about double space in the list.
 * @author Oliver Grande
 *
 */
public class JPATupleChildConverter extends JPATupleResultConverter {

  public JPATupleChildConverter(final JPAServiceDocument sd, final UriHelper uriHelper,
      final ServiceMetadata serviceMetadata) {

    super(sd, uriHelper, serviceMetadata);
  }

  public JPATupleChildConverter(JPATupleChildConverter converter) {
    this(converter.sd, converter.uriHelper, converter.serviceMetadata);
  }

  public Map<String, List<Object>> getCollectionResult(JPACollectionResult jpaResult)
      throws ODataApplicationException {

    return new JPATupleCollectionConverter(sd, uriHelper, serviceMetadata).getResult(jpaResult);
  }

  @Override
  public Map<String, EntityCollection> getResult(JPAExpandResult jpaResult)
      throws ODataApplicationException {

    jpaQueryResult = jpaResult;
    this.setName = determineSetName(jpaQueryResult, sd);
    this.jpaConversionTargetEntity = jpaQueryResult.getEntityType();
    this.edmType = determineEdmType();
    final Map<String, List<Tuple>> childResult = jpaResult.getResults();

    final Map<String, EntityCollection> result = new HashMap<>(childResult.size());
    for (Entry<String, List<Tuple>> tuple : childResult.entrySet()) {
      final EntityCollection entityCollection = new EntityCollection();
      final List<Entity> entities = entityCollection.getEntities();
      final List<Tuple> rows = tuple.getValue();

      for (int i = 0; i < rows.size(); i++) {
        final Tuple row = rows.set(i, null);
        final Entity odataEntity = convertRow(jpaConversionTargetEntity, row);
        odataEntity.setMediaContentType(determineContentType(jpaConversionTargetEntity, row));
        entities.add(odataEntity);
      }
      result.put(tuple.getKey(), entityCollection);
    }
    childResult.replaceAll((k, v) -> null);
    return result;
  }

  protected Entity convertRow(JPAEntityType rowEntity, Tuple row) throws ODataApplicationException {
    final Map<String, ComplexValue> complexValueBuffer = new HashMap<>();
    final Entity odataEntity = new Entity();

    odataEntity.setType(edmType.getFullQualifiedName().getFullQualifiedNameAsString());
    final List<Property> properties = odataEntity.getProperties();

    // TODO store @Version to fill ETag Header
    try {
      for (final JPAAttribute path : rowEntity.getKey()) {
        convertAttribute(row.get(path.getExternalName()), rowEntity.getPath(path.getExternalName()), complexValueBuffer,
            properties, row, EMPTY_PREFIX, "");
      }
    } catch (final ODataJPAModelException e) {
      throw new ODataJPAQueryException(ODataJPAQueryException.MessageKeys.QUERY_RESULT_CONV_ERROR,
          HttpStatusCode.INTERNAL_SERVER_ERROR, e);
    }
    odataEntity.setId(createId(odataEntity));
    for (final TupleElement<?> element : row.getElements()) {
      try {
        if (odataEntity.getProperty(element.getAlias()) == null)
          convertAttribute(row.get(element.getAlias()), rowEntity.getPath(element.getAlias()), complexValueBuffer,
              properties, row, EMPTY_PREFIX, odataEntity.getId().toString());
      } catch (ODataJPAModelException e) {
        throw new ODataJPAQueryException(ODataJPAQueryException.MessageKeys.QUERY_RESULT_CONV_ERROR,
            HttpStatusCode.INTERNAL_SERVER_ERROR, e);
      }
    }

    createCollectionProperties(rowEntity, row, properties);
    odataEntity.getNavigationLinks().addAll(createExpand(rowEntity, row, EMPTY_PREFIX, odataEntity.getId().toString()));

    return odataEntity;
  }

  protected void createCollectionProperties(final JPAStructuredType jpaStructuredType, final Tuple row,
      final List<Property> properties) throws ODataJPAQueryException {

    List<Property> result;

    try {
      for (JPAPath path : jpaStructuredType.getCollectionAttributesPath()) {
        result = properties;
        for (int i = 0; i < path.getPath().size() - 1; i++) {
          for (final Property p : result) {
            if (p.getName().equals(path.getPath().get(i).getExternalName())) {
              result = ((ComplexValue) p.getValue()).getValue();
              break;
            }
          }
        }
        final JPACollectionAttribute collection = (JPACollectionAttribute) path.getLeaf();
        final JPAExpandResult child = jpaQueryResult.getChild(collection.asAssociation());
        if (child != null) {
          final Collection<Object> collectionResult = ((JPACollectionResult) child).getPropertyCollection(
              buildConcatenatedKey(row, collection.asAssociation().getLeftColumnsList()));

          result.add(new Property(
              null,
              collection.getExternalName(),
              collection.isComplex() ? ValueType.COLLECTION_COMPLEX : ValueType.COLLECTION_PRIMITIVE,
              collectionResult != null ? collectionResult : new ArrayList<>(1)));
        }
      }
    } catch (ODataJPAModelException e) {
      throw new ODataJPAQueryException(ODataJPAQueryException.MessageKeys.QUERY_RESULT_CONV_ERROR,
          HttpStatusCode.INTERNAL_SERVER_ERROR, e);
    }
  }

  protected URI createId(final Entity entity) {

    try {
      // TODO Clarify host-name and port as part of ID see
      // http://docs.oasis-open.org/odata/odata-atom-format/v4.0/cs02/odata-atom-format-v4.0-cs02.html#_Toc372792702

      final StringBuilder uriString = new StringBuilder(setName);
      uriString.append("(");
      uriString.append(uriHelper.buildKeyPredicate(edmType, entity));
      uriString.append(")");
      return new URI(uriString.toString());
    } catch (URISyntaxException e) {
      throw new ODataRuntimeException("Unable to create id for entity: " + edmType.getName(), e);
    } catch (IllegalArgumentException e) {
      return null;
    } catch (SerializerException e) {
      throw new ODataRuntimeException(e);
    }
  }

  protected EdmEntityType determineEdmType() {
    try {
      final JPAEntitySet es = sd.getEntitySet(jpaQueryResult.getEntityType());
      return serviceMetadata.getEdm().getEntityType(es.getODataEntityType().getExternalFQN());
    } catch (ODataJPAModelException e) {
      throw new ODataRuntimeException(e);
    }
  }

  protected String determineSetName(final JPAExpandResult jpaQueryResult, final JPAServiceDocument sd)
      throws ODataJPAQueryException {
    try {
      return sd.getEntitySet(jpaQueryResult.getEntityType()).getExternalName();
    } catch (ODataJPAModelException e) {
      throw new ODataJPAQueryException(ODataJPAQueryException.MessageKeys.QUERY_RESULT_ENTITY_SET_ERROR,
          HttpStatusCode.INTERNAL_SERVER_ERROR, jpaQueryResult.getEntityType().getExternalFQN()
              .getFullQualifiedNameAsString());
    }
  }

  private String determineContentType(final JPAEntityType jpaEntity, final Tuple row) throws ODataJPAQueryException {

    try {
      if (jpaEntity.hasStream()) {
        if (jpaEntity.getContentType() != null && !jpaEntity.getContentType().isEmpty())
          return jpaEntity.getContentType();
        else {
          Object rowElement = null;
          for (final JPAElement element : jpaEntity.getContentTypeAttributePath().getPath()) {
            rowElement = row.get(element.getExternalName());
          }
          return rowElement != null ? rowElement.toString() : null;
        }
      }
      return null;
    } catch (ODataJPAModelException e) {
      throw new ODataJPAQueryException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
    }
  }
}
