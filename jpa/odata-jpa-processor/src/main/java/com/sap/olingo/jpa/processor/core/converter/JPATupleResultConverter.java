package com.sap.olingo.jpa.processor.core.converter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.persistence.AttributeConverter;
import javax.persistence.Tuple;

import org.apache.olingo.commons.api.Constants;
import org.apache.olingo.commons.api.data.ComplexValue;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.data.Link;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.data.ValueType;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.uri.UriHelper;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAElement;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAServiceDocument;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAStructuredType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAQueryException;
import com.sap.olingo.jpa.processor.core.query.JPAConvertableResult;

/**
 * Abstract super class for result converter, which convert Tuple based results.
 * @author Oliver Grande
 *
 */
abstract class JPATupleResultConverter implements JPAResultConverter {

  protected static final String EMPTY_PREFIX = "";
  protected JPAEntityType jpaConversionTargetEntity;
  protected JPAExpandResult jpaQueryResult;
  protected final UriHelper uriHelper;
  protected String setName;
  protected final JPAServiceDocument sd;
  protected final ServiceMetadata serviceMetadata;
  protected EdmEntityType edmType;

  public JPATupleResultConverter(final JPAServiceDocument sd, final UriHelper uriHelper,
      final ServiceMetadata serviceMetadata) {
    this.uriHelper = uriHelper;
    this.sd = sd;
    this.serviceMetadata = serviceMetadata;
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

  protected String buildPath(final String prefix, final JPAAssociationAttribute association) {
    return EMPTY_PREFIX.equals(prefix) ? association.getExternalName() : prefix + JPAPath.PATH_SEPERATOR + association
        .getExternalName();
  }

  protected void convertAttribute(final Object value, final JPAPath jpaPath,
      final Map<String, ComplexValue> complexValueBuffer, final List<Property> properties, final Tuple parentRow,
      final String prefix, final String rootURI) throws ODataJPAModelException, ODataApplicationException {

    if (jpaPath != null) {
      final JPAAttribute attribute = (JPAAttribute) jpaPath.getPath().get(0);
      if (attribute != null && !attribute.isKey() && attribute.isComplex()) {
        convertComplexAttribute(value, jpaPath.getAlias(), complexValueBuffer, properties, attribute, parentRow,
            prefix, rootURI);
      } else if (attribute != null) {
        convertPrimitiveAttribute(value, properties, jpaPath, attribute);
      }
    }
  }

  protected void createComplexValue(final Map<String, ComplexValue> complexValueBuffer, final List<Property> properties,
      final JPAAttribute attribute, final Tuple parentRow, final String bufferKey, final String rootURI)
      throws ODataJPAModelException, ODataApplicationException {

    final ComplexValue complexValue = new ComplexValue();
    complexValueBuffer.put(bufferKey, complexValue);
    properties.add(new Property(
        attribute.getStructuredType().getExternalFQN().getFullQualifiedNameAsString(),
        attribute.getExternalName(),
        ValueType.COMPLEX,
        complexValue));
    complexValue.getNavigationLinks().addAll(createExpand(attribute.getStructuredType(), parentRow, bufferKey,
        rootURI));

  }

  protected Collection<Link> createExpand(final JPAStructuredType jpaStructuredType, final Tuple row,
      final String prefix, final String rootURI) throws ODataApplicationException {
    final List<Link> entityExpandLinks = new ArrayList<>();

    JPAAssociationPath path = null;
    try {
      for (final JPAAssociationAttribute a : jpaStructuredType.getDeclaredAssociations()) {
        path = jpaConversionTargetEntity.getAssociationPath(buildPath(prefix, a));
        final JPAExpandResult child = jpaQueryResult.getChild(path);
        final String linkURI = rootURI + JPAPath.PATH_SEPERATOR + path.getAlias();
        if (child != null) {
          // TODO Check how to convert Organizations('3')/AdministrativeInformation?$expand=Created/User
          entityExpandLinks.add(getLink(path, row, child, linkURI));
        } else
          entityExpandLinks.add(getLink(path, linkURI));

      }
    } catch (ODataJPAModelException e) {
      throw new ODataJPAQueryException(ODataJPAQueryException.MessageKeys.QUERY_RESULT_NAVI_PROPERTY_ERROR,
          HttpStatusCode.INTERNAL_SERVER_ERROR, path != null ? path.getAlias() : EMPTY_PREFIX);
    }
    return entityExpandLinks;
  }

  protected final String determineAlias(final String alias, final String prefix) {
    if (EMPTY_PREFIX.equals(prefix))
      return alias;
    return alias.substring(alias.indexOf(prefix) + prefix.length() + 1);
  }

  protected final JPAStructuredType determineCollectionRoot(final JPAEntityType et, final List<JPAElement> pathList)
      throws ODataJPAModelException {
    if (pathList.size() > 1)
      return ((JPAAttribute) pathList.get(pathList.size() - 2)).getStructuredType();
    else
      return et;
  }

  protected final String determinePrefix(String alias) {
    final String prefix = alias;
    final int index = prefix.lastIndexOf(JPAPath.PATH_SEPERATOR);
    if (index < 0)
      return EMPTY_PREFIX;
    else
      return prefix.substring(0, index);
  }

  String buildPath(final JPAAttribute attribute, final String prefix) {
    return EMPTY_PREFIX.equals(prefix) ? attribute.getExternalName() : prefix + JPAPath.PATH_SEPERATOR + attribute
        .getExternalName();
  }

  void convertComplexAttribute(final Object value, final String externalName,
      final Map<String, ComplexValue> complexValueBuffer, final List<Property> properties, final JPAAttribute attribute,
      final Tuple parentRow, final String prefix, final String rootURI) throws ODataJPAModelException,
      ODataApplicationException {

    final String bufferKey = buildPath(attribute, prefix);

    if (!complexValueBuffer.containsKey(bufferKey)) {
      createComplexValue(complexValueBuffer, properties, attribute, parentRow, bufferKey, rootURI);
    }

    final List<Property> values = complexValueBuffer.get(bufferKey).getValue();
    final int splitIndex = attribute.getExternalName().length() + JPAPath.PATH_SEPERATOR.length();
    final String attributeName = splitIndex < externalName.length() ? externalName.substring(splitIndex) : externalName;
    convertAttribute(value, attribute.getStructuredType().getPath(attributeName), complexValueBuffer, values,
        parentRow, buildPath(attribute, prefix), rootURI);
  }

  @SuppressWarnings("unchecked")
  <T extends Object, S extends Object> void convertPrimitiveAttribute(final Object value,
      final List<Property> properties, final JPAPath jpaPath,
      final JPAAttribute attribute) {

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

  Integer determineCount(final JPAAssociationPath assoziation, final Tuple parentRow, final JPAExpandResult child)
      throws ODataJPAQueryException {
    try {
      Long count = child.getCount(buildConcatenatedKey(parentRow, assoziation.getLeftColumnsList()));
      return count != null ? Integer.valueOf(count.intValue()) : null;
    } catch (ODataJPAModelException e) {
      throw new ODataJPAQueryException(ODataJPAQueryException.MessageKeys.QUERY_RESULT_CONV_ERROR,
          HttpStatusCode.INTERNAL_SERVER_ERROR, e);
    }
  }

  private Link getLink(final JPAAssociationPath assoziation, final Tuple parentRow, final JPAExpandResult child,
      final String linkURI) throws ODataApplicationException {
    final Link link = new Link();
    link.setTitle(assoziation.getLeaf().getExternalName());
    link.setRel(Constants.NS_NAVIGATION_LINK_REL + link.getTitle());
    link.setType(Constants.ENTITY_NAVIGATION_LINK_TYPE);
    try {
      final EntityCollection expandCollection = ((JPAConvertableResult) child).getEntityCollection(
          buildConcatenatedKey(parentRow, assoziation.getLeftColumnsList()));

      expandCollection.setCount(determineCount(assoziation, parentRow, child));
      if (assoziation.getLeaf().isCollection()) {
        link.setInlineEntitySet(expandCollection);
        link.setHref(linkURI);
      } else {
        if (expandCollection.getEntities() != null && !expandCollection.getEntities().isEmpty()) {
          final Entity expandEntity = expandCollection.getEntities().get(0);
          link.setInlineEntity(expandEntity);
          link.setHref(linkURI);
        }
      }
    } catch (ODataJPAModelException e) {
      throw new ODataJPAQueryException(ODataJPAQueryException.MessageKeys.QUERY_RESULT_CONV_ERROR,
          HttpStatusCode.INTERNAL_SERVER_ERROR, e);
    }
    return link;
  }

  private Link getLink(final JPAAssociationPath assoziation, final String linkURI) {
    final Link link = new Link();
    link.setTitle(assoziation.getLeaf().getExternalName());
    link.setRel(Constants.NS_NAVIGATION_LINK_REL + link.getTitle());
    link.setType(Constants.ENTITY_NAVIGATION_LINK_TYPE);
    link.setHref(linkURI);
    return link;
  }

}