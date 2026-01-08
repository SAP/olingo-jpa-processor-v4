package com.sap.olingo.jpa.processor.core.converter;

import static com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessorException.MessageKeys.ODATA_MAXPAGESIZE_NOT_A_NUMBER;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Tuple;
import jakarta.persistence.TupleElement;

import org.apache.commons.logging.Log;
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
import org.apache.olingo.server.api.uri.queryoption.SystemQueryOptionKind;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmTransientPropertyCalculator;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAElement;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAServiceDocument;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAStructuredType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.api.JPAODataPageExpandInfo;
import com.sap.olingo.jpa.processor.core.api.JPAODataRequestContextAccess;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessorException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAQueryException;
import com.sap.olingo.jpa.processor.core.query.JPAConvertibleResult;
import com.sap.olingo.jpa.processor.core.query.Utility;
import com.sap.olingo.jpa.processor.core.serializer.JPAEntityCollectionExtension;

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
  protected final JPAODataRequestContextAccess requestContext;

  protected JPATupleResultConverter(final JPAServiceDocument sd, final UriHelper uriHelper,
      final ServiceMetadata serviceMetadata, final JPAODataRequestContextAccess requestContext) {
    this.uriHelper = uriHelper;
    this.sd = sd;
    this.serviceMetadata = serviceMetadata;
    this.requestContext = requestContext;
  }

  protected String buildConcatenatedKey(final Tuple row, final List<JPAPath> leftColumns) {
    final StringBuilder buffer = new StringBuilder();
    for (final JPAPath item : leftColumns) {
      buffer.append(JPAPath.PATH_SEPARATOR);
      // TODO Tuple returns the converted value in case a @Convert(converter = annotation is given
      buffer.append(row.get(item.getAlias()));
    }
    buffer.deleteCharAt(0);
    return buffer.toString();
  }

  protected String buildPath(final String prefix, final JPAAssociationAttribute association) {
    return EMPTY_PREFIX.equals(prefix) ? association.getExternalName() : prefix + JPAPath.PATH_SEPARATOR + association
        .getExternalName();
  }

  protected void convertAttribute(final Object value, final JPAPath jpaPath,
      final Map<String, ComplexValue> complexValueBuffer, final List<Property> properties, final Tuple parentRow,
      final String prefix, @Nullable final Entity odataEntity, final List<JPAODataPageExpandInfo> expandInfo)
      throws ODataJPAModelException, ODataApplicationException {

    if (jpaPath != null) {
      final JPAAttribute attribute = (JPAAttribute) jpaPath.getPath().get(0);
      if (attribute != null && !attribute.isKey() && attribute.isComplex()) {
        convertComplexAttribute(value, jpaPath.getAlias(), complexValueBuffer, properties, attribute, parentRow,
            prefix, odataEntity, expandInfo);
      } else if (attribute != null) {
        convertPrimitiveAttribute(value, properties, jpaPath, attribute, parentRow);
      }
    }
  }

  protected void convertRowWithOutSelection(final JPAEntityType rowEntity, final Tuple row,
      final Map<String, ComplexValue> complexValueBuffer, final Entity odataEntity, final List<Property> properties,
      final List<JPAODataPageExpandInfo> expandInfo) throws ODataApplicationException {

    for (final TupleElement<?> element : row.getElements()) {
      try {
        if (odataEntity.getProperty(element.getAlias()) == null) {
          final JPAPath path = rowEntity.getPath(element.getAlias());
          convertAttribute(row.get(element.getAlias()), path, complexValueBuffer, properties, row, EMPTY_PREFIX,
              odataEntity, expandInfo);
        }
      } catch (final ODataJPAModelException e) {
        throw new ODataJPAQueryException(ODataJPAQueryException.MessageKeys.QUERY_RESULT_CONV_ERROR,
            HttpStatusCode.INTERNAL_SERVER_ERROR, e);
      }
    }
  }

  protected void convertRowWithSelection(final Tuple row, final Collection<JPAPath> requestedSelection,
      final Map<String, ComplexValue> complexValueBuffer, final Entity odataEntity, final List<Property> properties,
      final List<JPAODataPageExpandInfo> expandInfo) throws ODataApplicationException {

    for (final JPAPath p : requestedSelection) {
      try {
        final Object value = p.isTransient() ? null : row.get(p.getAlias());
        if (odataEntity == null || odataEntity.getProperty(p.getAlias()) == null)
          convertAttribute(value, p, complexValueBuffer, properties, row, EMPTY_PREFIX, odataEntity, expandInfo);

      } catch (final IllegalArgumentException e) {
        // Skipped property; add it to result
        final JPATuple skipped = new JPATuple();
        skipped.addElement(p.getAlias(), p.getLeaf().getType(), null);
        try {
          convertAttribute(null, p, complexValueBuffer, properties, skipped, EMPTY_PREFIX, odataEntity, expandInfo);
        } catch (final ODataJPAModelException e1) {
          throw new ODataJPAQueryException(ODataJPAQueryException.MessageKeys.QUERY_RESULT_CONV_ERROR,
              HttpStatusCode.INTERNAL_SERVER_ERROR, e1);
        }
      } catch (final ODataJPAModelException e) {
        throw new ODataJPAQueryException(ODataJPAQueryException.MessageKeys.QUERY_RESULT_CONV_ERROR,
            HttpStatusCode.INTERNAL_SERVER_ERROR, e);
      }
    }
  }

  protected void createComplexValue(final Map<String, ComplexValue> complexValueBuffer, final List<Property> properties,
      final JPAAttribute attribute, final Tuple parentRow, final String bufferKey, final String rootURI,
      final List<JPAODataPageExpandInfo> expandInfo) throws ODataJPAModelException, ODataApplicationException {

    final ComplexValue complexValue = new ComplexValue();
    complexValueBuffer.put(bufferKey, complexValue);
    properties.add(new Property(
        attribute.getStructuredType().getExternalFQN().getFullQualifiedNameAsString(),
        attribute.getExternalName(),
        ValueType.COMPLEX,
        complexValue));
    complexValue.getNavigationLinks().addAll(createExpand(attribute.getStructuredType(), parentRow, bufferKey,
        rootURI, expandInfo));

  }

  protected Collection<Link> createExpand(final JPAStructuredType jpaStructuredType, final Tuple row,
      final String prefix, final String rootURI, final List<JPAODataPageExpandInfo> expandInfo)
      throws ODataApplicationException {
    final List<Link> entityExpandLinks = new ArrayList<>();

    JPAAssociationPath path = null;
    try {
      for (final JPAAssociationAttribute a : jpaStructuredType.getDeclaredAssociations()) {
        path = jpaConversionTargetEntity.getAssociationPath(buildPath(prefix, a));
        final JPAExpandResult child = jpaQueryResult.getChild(path);
        final String linkURI = rootURI + JPAPath.PATH_SEPARATOR + path.getAlias();
        if (child != null) {
          // TODO Check how to convert Organizations('3')/AdministrativeInformation?$expand=Created/User
          entityExpandLinks.add(getLink(path, row, child, linkURI, expandInfo));
        } else {
          entityExpandLinks.add(getLink(path, linkURI));
        }
      }
    } catch (final ODataJPAModelException e) {
      throw new ODataJPAQueryException(ODataJPAQueryException.MessageKeys.QUERY_RESULT_NAVI_PROPERTY_ERROR,
          HttpStatusCode.INTERNAL_SERVER_ERROR, e, path != null ? path.getAlias() : EMPTY_PREFIX);
    }
    return entityExpandLinks;
  }

  protected final String determineAlias(final String alias, final String prefix) {
    if (EMPTY_PREFIX.equals(prefix))
      return alias;
    final int startPos = alias.indexOf(prefix) + prefix.length() + 1;
    if (startPos > alias.length())
      return null;
    return alias.substring(startPos);
  }

  protected final JPAStructuredType determineCollectionRoot(final JPAEntityType et, final List<JPAElement> pathList)
      throws ODataJPAModelException {
    if (pathList.size() > 1)
      return ((JPAAttribute) pathList.get(pathList.size() - 2)).getStructuredType();
    else
      return et;
  }

  protected final String determinePrefix(final String alias) {
    final String prefix = alias;
    final int index = prefix.lastIndexOf(JPAPath.PATH_SEPARATOR);
    if (index < 0)
      return EMPTY_PREFIX;
    else
      return prefix.substring(0, index);
  }

  String buildPath(final JPAAttribute attribute, final String prefix) {
    return EMPTY_PREFIX.equals(prefix) ? attribute.getExternalName() : prefix + JPAPath.PATH_SEPARATOR + attribute
        .getExternalName();
  }

  void convertComplexAttribute(final Object value, final String externalName,
      final Map<String, ComplexValue> complexValueBuffer, final List<Property> properties, final JPAAttribute attribute,
      final Tuple parentRow, final String prefix, final Entity odataEntity,
      final List<JPAODataPageExpandInfo> expandInfo) throws ODataJPAModelException, ODataApplicationException {

    final String bufferKey = buildPath(attribute, prefix);

    if (!complexValueBuffer.containsKey(bufferKey)) {
      createComplexValue(complexValueBuffer, properties, attribute, parentRow, bufferKey,
          odataEntity == null ? "" : odataEntity.getId().toString(), expandInfo);
    }

    final List<Property> values = complexValueBuffer.get(bufferKey).getValue();
    final int splitIndex = attribute.getExternalName().length() + JPAPath.PATH_SEPARATOR.length();
    final String attributeName = splitIndex < externalName.length() ? externalName.substring(splitIndex) : externalName;
    convertAttribute(value, attribute.getStructuredType().getPath(attributeName), complexValueBuffer, values,
        parentRow, buildPath(attribute, prefix), odataEntity, expandInfo);
  }

  @SuppressWarnings("unchecked")
  <T extends Object, S extends Object> void convertPrimitiveAttribute(final Object value,
      final List<Property> properties, final JPAPath jpaPath, final JPAAttribute attribute, final Tuple parentRow)
      throws ODataJPAProcessorException {

    Object odataValue = null;
    if (attribute != null && attribute.isTransient()) {
      if (attribute.isCollection())
        return;
      final Optional<EdmTransientPropertyCalculator<?>> calculator = requestContext.getCalculator(attribute);
      if (calculator.isPresent()) {
        try {
          odataValue = calculator.get().calculateProperty(parentRow);
        } catch (final IllegalArgumentException e) {
          requestContext.getDebugger().debug(this, "Error in transient field calculator %s: %s",
              calculator.get().getClass().getName(), e.getMessage());
          throw new ODataJPAProcessorException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
        }
      }
    } else if (attribute != null && value != null && attribute.isEnum() && !value.getClass().isArray()) {
      odataValue = ((Enum<?>) value).ordinal();
    } else if (attribute != null && attribute.getConverter() != null) {
      final AttributeConverter<T, S> converter = attribute.getConverter();
      odataValue = converter.convertToDatabaseColumn((T) value);
    } else if (attribute != null && value != null && attribute.isCollection()) {
      return;
    } else if (attribute != null && value != null && attribute.getType() == Duration.class) {
      odataValue = ((Duration) value).getSeconds();
    } else {
      odataValue = value;
    }
    if (attribute != null && attribute.isKey() && attribute.isComplex()) {

      properties.add(new Property(
          null,
          jpaPath.getLeaf().getExternalName(),
          attribute.isEnum() ? ValueType.ENUM : ValueType.PRIMITIVE,
          odataValue));
    } else if (attribute != null) {
      // ...$select=Name1,Address/Region
      properties.add(new Property(
          null,
          attribute.getExternalName(),
          attribute.isEnum() ? ValueType.ENUM : ValueType.PRIMITIVE,
          odataValue));
    }
  }

  Integer determineCount(final JPAExpandResult child, final String foreignKey) {

    final Long count = child.getCount(foreignKey);
    return count != null ? count.intValue() : null;
  }

  protected Link getLink(final JPAAssociationPath association, final String linkURI) {
    final Link link = new Link();
    link.setTitle(association.getLeaf().getExternalName());
    link.setRel(Constants.NS_NAVIGATION_LINK_REL + link.getTitle());
    link.setType(Constants.ENTITY_NAVIGATION_LINK_TYPE);
    link.setHref(linkURI);
    return link;
  }

  protected Link getLink(final JPAAssociationPath association, final Tuple parentRow, final JPAExpandResult child,
      final String linkURI, final List<JPAODataPageExpandInfo> expandInfo) throws ODataApplicationException {
    final Link link = new Link();
    link.setTitle(association.getLeaf().getExternalName());
    link.setRel(Constants.NS_NAVIGATION_LINK_REL + link.getTitle());
    link.setType(Constants.ENTITY_NAVIGATION_LINK_TYPE);
    try {
      final var foreignKey = buildConcatenatedKey(parentRow, association.getLeftColumnsList());
      final List<JPAODataPageExpandInfo> newInfo = new ArrayList<>(expandInfo);
      newInfo.add(new JPAODataPageExpandInfo(association.getAlias(), foreignKey));
      final JPAEntityCollectionExtension expandCollection = ((JPAConvertibleResult) child).getEntityCollection(
          foreignKey, this, association, newInfo);
      expandCollection.setCount(determineCount(child, foreignKey));
      if (association.getLeaf().isCollection()) {
        if (!expandCollection.getEntities().isEmpty()) {
          expandCollection.setNext(createNextLink(child, newInfo));
        }
        link.setInlineEntitySet((EntityCollection) expandCollection);
        link.setHref(linkURI);
      } else {
        if (expandCollection.getEntities() != null && !expandCollection.getEntities().isEmpty()) {
          final Entity expandEntity = expandCollection.getEntities().get(0);
          link.setInlineEntity(expandEntity);
          link.setHref(linkURI);
        }
      }
    } catch (final ODataJPAModelException e) {
      throw new ODataJPAQueryException(ODataJPAQueryException.MessageKeys.QUERY_RESULT_CONV_ERROR,
          HttpStatusCode.INTERNAL_SERVER_ERROR, e);
    }
    return link;
  }

  private URI createNextLink(final JPAExpandResult child, final List<JPAODataPageExpandInfo> newInfo)
      throws ODataJPAProcessorException {
    try {
      final String skipToken = child.getSkipToken(newInfo);
      if (skipToken == null)
        return null;
      return new URI(Utility.determineBindingTarget(requestContext.getUriInfo().getUriResourceParts()).getName() + "?"
          + SystemQueryOptionKind.SKIPTOKEN.toString() + "=" + skipToken);
    } catch (final URISyntaxException e) {
      throw new ODataJPAProcessorException(ODATA_MAXPAGESIZE_NOT_A_NUMBER, HttpStatusCode.INTERNAL_SERVER_ERROR, e);
    }
  }

  protected List<Property> findOrCreateComplexProperty(List<Property> result, final JPAElement pathElement)
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
      getLogger().debug(e.getMessage() + ": No URI created");
      return null;
    } catch (final SerializerException e) {
      throw new ODataRuntimeException(e);
    }
  }

  void createEtag(@Nonnull final JPAEntityType rowEntity, final Tuple row, final Entity odataEntity)
      throws ODataJPAQueryException {

    try {
      if (rowEntity.hasEtag()) {
        final String etagAlias = rowEntity.getEtagPath().getAlias();
        final Object etag = row.get(etagAlias);
        if (etag != null) {
          odataEntity.setETag(requestContext.getEtagHelper().asEtag(rowEntity, etag));
        }
      }

    } catch (final ODataJPAModelException e) {
      throw new ODataJPAQueryException(ODataJPAQueryException.MessageKeys.QUERY_RESULT_CONV_ERROR,
          HttpStatusCode.INTERNAL_SERVER_ERROR, e);
    }
  }

  protected abstract Log getLogger();
}