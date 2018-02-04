package com.sap.olingo.jpa.processor.core.converter;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.AttributeConverter;
import javax.persistence.Tuple;
import javax.persistence.TupleElement;

import org.apache.olingo.commons.api.Constants;
import org.apache.olingo.commons.api.data.ComplexValue;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.data.Link;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.data.ValueType;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.ex.ODataRuntimeException;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.serializer.SerializerException;
import org.apache.olingo.server.api.uri.UriHelper;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationAttribute;
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
import com.sap.olingo.jpa.processor.core.exception.ODataJPAQueryException;
import com.sap.olingo.jpa.processor.core.query.JPACollectionQueryResult;
import com.sap.olingo.jpa.processor.core.query.JPAExpandQueryResult;

/**
 * Converts the query result from JPA format into Olingo format.<p>
 * To reduce the memory footprint each converted row is set to null. This is done as currently the query result is
 * stored in an ArrayList and deleting a row, which is not the last row, leads to an array copy, which can consume a lot
 * of time. For the same reason no trimToSize() is called. As an alternative to an ArrayList also a simple linked list
 * could be used, but this comes with the draw back that each entry would consume round about double space in the list.
 * @author Oliver Grande
 *
 */
public class JPATupleChildConverter {

  protected static final String EMPTY_PREFIX = "";
  public static final String ACCESS_MODIFIER_GET = "get";
  public static final String ACCESS_MODIFIER_SET = "set";
  public static final String ACCESS_MODIFIER_IS = "is";
  protected JPAEntityType jpaConversionTargetEntity;
  protected JPAExpandResult jpaQueryResult;
  protected final UriHelper uriHelper;
  protected String setName;
  protected final JPAServiceDocument sd;
  protected final ServiceMetadata serviceMetadata;
  protected EdmEntityType edmType;

  public JPATupleChildConverter(JPAServiceDocument sd, UriHelper uriHelper, ServiceMetadata serviceMetadata) {
    this.uriHelper = uriHelper;
    this.sd = sd;
    this.serviceMetadata = serviceMetadata;
  }

  public JPATupleChildConverter(JPATupleChildConverter converter) {
    this(converter.sd, converter.uriHelper, converter.serviceMetadata);
  }

  public Map<String, List<Object>> getCollectionResult(JPACollectionQueryResult jpaResult)
      throws ODataJPAQueryException {

    jpaQueryResult = jpaResult;
    final List<JPAElement> p = jpaResult.getAssoziation().getPath();
    final boolean isComplex = ((JPACollectionAttribute) p.get(p.size() - 1)).isComplex();
    final Map<String, List<Tuple>> childResult = jpaResult.getResults();
    final Map<String, List<Object>> result = new HashMap<>(childResult.size());
    this.jpaConversionTargetEntity = jpaResult.getEntityType();
    for (Entry<String, List<Tuple>> tuple : childResult.entrySet()) {
      final List<Object> collection = new ArrayList<>();
      final List<Tuple> rows = tuple.getValue();
      for (int i = 0; i < rows.size(); i++) {
        final Tuple row = rows.set(i, null);
        if (isComplex) {
          final ComplexValue value = new ComplexValue();
          for (final TupleElement<?> element : row.getElements()) {
            try {
              final JPAPath path = jpaConversionTargetEntity.getPath(element.getAlias());
              if (path != null) {
                final JPAAttribute attribute = path.getLeaf();
                convertPrimitiveAttribute(row.get(element.getAlias()), value.getValue(), path, attribute);
              }
            } catch (ODataJPAModelException e) {
              throw new ODataJPAQueryException(ODataJPAQueryException.MessageKeys.QUERY_RESULT_CONV_ERROR,
                  HttpStatusCode.INTERNAL_SERVER_ERROR, e);
            }
          }
          collection.add(value);
        } else {
          for (final TupleElement<?> element : row.getElements()) {
            try {
              final JPAPath path = jpaConversionTargetEntity.getPath(element.getAlias());
              if (path != null) {
                collection.add(row.get(element.getAlias()));
              }
            } catch (ODataJPAModelException e) {
              throw new ODataJPAQueryException(ODataJPAQueryException.MessageKeys.QUERY_RESULT_CONV_ERROR,
                  HttpStatusCode.INTERNAL_SERVER_ERROR, e);
            }
          }
        }
      }
      result.put(tuple.getKey(), collection);
    }
    childResult.replaceAll((k, v) -> null);
    return result;
  }

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

  protected String buildConcatenatedKey(final Tuple row, final List<JPAPath> leftColumns) {
    final StringBuilder buffer = new StringBuilder();
    for (final JPAPath item : leftColumns) {
      buffer.append(JPAPath.PATH_SEPERATOR);
      // TODO Tuple returns the converted value in case a @Convert(converter = annotation is given
      buffer.append(row.get(item.getAlias()));
    }
    buffer.deleteCharAt(0);
    return buffer.toString();
  }

  protected void convertAttribute(final Object value, final JPAPath jpaPath,
      final Map<String, ComplexValue> complexValueBuffer, final List<Property> properties, final Tuple parentRow,
      final String prefix) throws ODataJPAModelException, ODataApplicationException {

    if (jpaPath != null) {
      final JPAAttribute attribute = (JPAAttribute) jpaPath.getPath().get(0);
      if (attribute != null && !attribute.isKey() && attribute.isComplex()) {
        convertComplexAttribute(value, jpaPath.getAlias(), complexValueBuffer, properties, attribute, parentRow,
            prefix);
      } else if (attribute != null) {
        convertPrimitiveAttribute(value, properties, jpaPath, attribute);
      }
    }
  }

  protected Entity convertRow(JPAEntityType rowEntity, Tuple row) throws ODataApplicationException {
    final Map<String, ComplexValue> complexValueBuffer = new HashMap<>();
    final Entity odataEntity = new Entity();

    odataEntity.setType(edmType.getFullQualifiedName().getFullQualifiedNameAsString());
    final List<Property> properties = odataEntity.getProperties();
    // TODO store @Version to fill ETag Header
    for (final TupleElement<?> element : row.getElements()) {
      try {
        convertAttribute(row.get(element.getAlias()), rowEntity.getPath(element.getAlias()), complexValueBuffer,
            properties, row, EMPTY_PREFIX);
      } catch (ODataJPAModelException e) {
        throw new ODataJPAQueryException(ODataJPAQueryException.MessageKeys.QUERY_RESULT_CONV_ERROR,
            HttpStatusCode.INTERNAL_SERVER_ERROR, e);
      }
    }
    odataEntity.setId(createId(odataEntity));
    properties.addAll(createCollectionProperties(rowEntity, row));
    odataEntity.getNavigationLinks().addAll(createExpand(rowEntity, row, EMPTY_PREFIX));
    return odataEntity;
  }

  protected List<Property> createCollectionProperties(final JPAStructuredType jpaStructuredType, final Tuple row)
      throws ODataJPAQueryException {
    final List<Property> result = new ArrayList<>();
    try {
      for (JPACollectionAttribute collection : jpaStructuredType.getDeclaredCollectionAttributes()) {
        JPAExpandResult child = jpaQueryResult.getChild(collection.asAssociation());
        if (child != null) {
          final List<Object> collectionResult = ((JPACollectionQueryResult) child).getPropertyCollection(
              buildConcatenatedKey(row, collection.asAssociation().getLeftColumnsList()));

          result.add(new Property(
              null,
              collection.getExternalName(),
              collection.isComplex() ? ValueType.COLLECTION_COMPLEX : ValueType.COLLECTION_PRIMITIVE,
              collectionResult != null ? collectionResult : new ArrayList<>(1)));
        }
      }
    } catch (

    ODataJPAModelException e) {
      throw new ODataJPAQueryException(ODataJPAQueryException.MessageKeys.QUERY_RESULT_CONV_ERROR,
          HttpStatusCode.INTERNAL_SERVER_ERROR, e);
    }
    return result;
  }

  protected void createComplexValue(final Map<String, ComplexValue> complexValueBuffer,
      final List<Property> properties, final JPAAttribute attribute, final Tuple parentRow, final String bufferKey)
      throws ODataJPAModelException, ODataApplicationException {

    final ComplexValue complexValue = new ComplexValue();
    complexValueBuffer.put(bufferKey, complexValue);
    properties.add(new Property(
        attribute.getStructuredType().getExternalFQN().getFullQualifiedNameAsString(),
        attribute.getExternalName(),
        ValueType.COMPLEX,
        complexValue));
    complexValue.getNavigationLinks().addAll(createExpand(attribute.getStructuredType(), parentRow, bufferKey));

  }

  protected Collection<Link> createExpand(final JPAStructuredType jpaStructuredType, final Tuple row,
      final String prefix) throws ODataApplicationException {
    final List<Link> entityExpandLinks = new ArrayList<>();

    JPAAssociationPath path = null;
    try {
      for (final JPAAssociationAttribute a : jpaStructuredType.getDeclaredAssociations()) {
        path = jpaConversionTargetEntity.getAssociationPath(buildPath(prefix, a));
        JPAExpandResult child = jpaQueryResult.getChild(path);

        if (child != null) {
          final Link expand = getLink(path, row, child);
          // TODO Check how to convert Organizations('3')/AdministrativeInformation?$expand=Created/User
          entityExpandLinks.add(expand);
        }
      }
    } catch (ODataJPAModelException e) {
      throw new ODataJPAQueryException(ODataJPAQueryException.MessageKeys.QUERY_RESULT_NAVI_PROPERTY_ERROR,
          HttpStatusCode.INTERNAL_SERVER_ERROR, path != null ? path.getAlias() : EMPTY_PREFIX);
    }
    return entityExpandLinks;
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

  private String buildPath(final JPAAttribute attribute, final String prefix) {
    return EMPTY_PREFIX.equals(prefix) ? attribute.getExternalName() : prefix + JPAPath.PATH_SEPERATOR + attribute
        .getExternalName();
  }

  private String buildPath(final String prefix, final JPAAssociationAttribute association) {
    return EMPTY_PREFIX.equals(prefix) ? association.getExternalName() : prefix + JPAPath.PATH_SEPERATOR + association
        .getExternalName();
  }

  private void convertComplexAttribute(final Object value, final String externalName,
      final Map<String, ComplexValue> complexValueBuffer, final List<Property> properties,
      final JPAAttribute attribute, final Tuple parentRow, final String prefix) throws ODataJPAModelException,
      ODataApplicationException {

    final String bufferKey = buildPath(attribute, prefix);

    if (!complexValueBuffer.containsKey(bufferKey)) {
      createComplexValue(complexValueBuffer, properties, attribute, parentRow, bufferKey);
    }

    final List<Property> values = complexValueBuffer.get(bufferKey).getValue();
    final int splitIndex = attribute.getExternalName().length() + JPAPath.PATH_SEPERATOR.length();
    final String attributeName = externalName.substring(splitIndex);
    convertAttribute(value, attribute.getStructuredType().getPath(attributeName), complexValueBuffer, values,
        parentRow, buildPath(attribute, prefix));
  }

  @SuppressWarnings("unchecked")
  private <T extends Object, S extends Object> void convertPrimitiveAttribute(final Object value,
      final List<Property> properties, final JPAPath jpaPath, final JPAAttribute attribute) {

    Object odataValue;
    if (attribute != null && attribute.getConverter() != null) {
      AttributeConverter<T, S> converter = attribute.getConverter();
      odataValue = converter.convertToDatabaseColumn((T) value);
    } else if (attribute != null && value != null && attribute.isEnum())
      odataValue = ((Enum<?>) value).ordinal();
    else if (attribute != null && value != null && attribute.isCollection()) {
      return;
    } else
      odataValue = value;
    if (attribute != null && attribute.isKey() && attribute.isComplex()) {

      properties.add(new Property(
          null,
          jpaPath.getLeaf().getExternalName(),
          attribute.isEnum() ? ValueType.ENUM : ValueType.PRIMITIVE,
          odataValue));
    } else {
      // ...$select=Name1,Address/Region
      properties.add(new Property(
          null,
          attribute.getExternalName(),
          attribute.isEnum() ? ValueType.ENUM : ValueType.PRIMITIVE,
          odataValue));
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

  private Integer determineCount(final JPAAssociationPath assoziation, final Tuple parentRow,
      final JPAExpandResult child)
      throws ODataJPAQueryException {
    try {
      Long count = child.getCount(buildConcatenatedKey(parentRow, assoziation.getLeftColumnsList()));
      return count != null ? Integer.valueOf(count.intValue()) : null;
    } catch (ODataJPAModelException e) {
      throw new ODataJPAQueryException(ODataJPAQueryException.MessageKeys.QUERY_RESULT_CONV_ERROR,
          HttpStatusCode.INTERNAL_SERVER_ERROR, e);
    }
  }

  private Link getLink(final JPAAssociationPath assoziation, final Tuple parentRow, final JPAExpandResult child)
      throws ODataJPAQueryException {
    final Link link = new Link();
    link.setTitle(assoziation.getLeaf().getExternalName());
    link.setRel(Constants.NS_NAVIGATION_LINK_REL + link.getTitle());
    link.setType(Constants.ENTITY_NAVIGATION_LINK_TYPE);
    try {
      final EntityCollection expandCollection = ((JPAExpandQueryResult) child).getEntityCollection(
          buildConcatenatedKey(parentRow, assoziation.getLeftColumnsList()));

      expandCollection.setCount(determineCount(assoziation, parentRow, child));
      if (assoziation.getLeaf().isCollection()) {
        link.setInlineEntitySet(expandCollection);
        // TODO link.setHref(parentUri.toASCIIString());
      } else {
        if (expandCollection.getEntities() != null && !expandCollection.getEntities().isEmpty()) {
          final Entity expandEntity = expandCollection.getEntities().get(0);
          link.setInlineEntity(expandEntity);
          // TODO link.setHref(expandCollection.getId().toASCIIString());
        }
      }
    } catch (ODataJPAModelException e) {
      throw new ODataJPAQueryException(ODataJPAQueryException.MessageKeys.QUERY_RESULT_CONV_ERROR,
          HttpStatusCode.INTERNAL_SERVER_ERROR, e);
    }
    return link;
  }

}
