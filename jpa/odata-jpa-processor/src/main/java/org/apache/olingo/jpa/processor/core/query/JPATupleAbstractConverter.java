package org.apache.olingo.jpa.processor.core.query;

import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.persistence.Tuple;
import javax.persistence.TupleElement;

import org.apache.olingo.commons.api.data.ComplexValue;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.Link;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.data.ValueType;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.ex.ODataRuntimeException;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAAttribute;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAOnConditionItem;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAStructuredType;
import org.apache.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import org.apache.olingo.jpa.metadata.core.edm.mapper.impl.JPAAssociationPath;
import org.apache.olingo.jpa.metadata.core.edm.mapper.impl.ServicDocument;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.serializer.SerializerException;
import org.apache.olingo.server.api.uri.UriHelper;

public abstract class JPATupleAbstractConverter {

  public static final String ACCESS_MODIFIER_GET = "get";
  public static final String ACCESS_MODIFIER_SET = "set";
  public static final String ACCESS_MODIFIER_IS = "is";
  private static final Map<String, HashMap<String, Method>> methodsBuffer =
      new HashMap<String, HashMap<String, Method>>();
  protected final JPAEntityType jpaConversionTargetEntity;
  protected final JPAExpandResult jpaQueryResult;
  protected final UriHelper uriHelper;
  protected final String setName;
  protected final ServicDocument sd;
  protected final ServiceMetadata serviceMetadata;

  public JPATupleAbstractConverter(final JPAExpandResult jpaQueryResult,
      final UriHelper uriHelper, final ServicDocument sd, final ServiceMetadata serviceMetadata)
      throws ODataApplicationException {
    super();

    this.jpaConversionTargetEntity = jpaQueryResult.getEntityType();
    this.jpaQueryResult = jpaQueryResult;
    this.uriHelper = uriHelper;
    this.sd = sd;
    this.serviceMetadata = serviceMetadata;
    try {
      this.setName = sd.getEntitySet(jpaQueryResult.getEntityType()).getExternalName();
    } catch (ODataJPAModelException e) {
      throw new ODataApplicationException("Entity Set not found for " +
          jpaQueryResult.getEntityType().getExternalFQN().getFullQualifiedNameAsString(),
          HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), Locale.ENGLISH, e);
    }
  }

  protected String buildConcatenatedKey(final Tuple row, final List<JPAOnConditionItem> joinColumns) {
    final StringBuffer buffer = new StringBuffer();
    for (final JPAOnConditionItem item : joinColumns) {
      buffer.append(JPAPath.PATH_SEPERATOR);
      buffer.append(row.get(item.getLeftPath().getAlias()));
    }
    buffer.deleteCharAt(0);
    return buffer.toString();
  }

  protected Entity convertRow(final JPAEntityType rowEntity, final Tuple row) throws ODataApplicationException {
    final Map<String, ComplexValue> complexValueBuffer = new HashMap<String, ComplexValue>();
    final Entity odataEntity = new Entity();
    odataEntity.setType(rowEntity.getExternalFQN().getFullQualifiedNameAsString());
    final List<Property> properties = odataEntity.getProperties();
    for (final TupleElement<?> element : row.getElements()) {
      try {
        convertAttribute(row.get(element.getAlias()), element.getAlias(), "", rowEntity, complexValueBuffer,
            properties);
      } catch (ODataJPAModelException e) {
        throw new ODataApplicationException("Mapping Error", 500, Locale.ENGLISH, e);
      }
    }
    try {
      odataEntity.setId(createId(jpaConversionTargetEntity.getKey(), odataEntity));
    } catch (ODataJPAModelException e) {
      throw new ODataApplicationException("Property not found", HttpStatusCode.BAD_REQUEST.getStatusCode(),
          Locale.ENGLISH, e);
    }
    for (final String attribute : complexValueBuffer.keySet()) {
      final ComplexValue complexValue = complexValueBuffer.get(attribute);
      complexValue.getNavigationLinks().addAll(createExpand(row, odataEntity.getId(), attribute));
    }
    odataEntity.getNavigationLinks().addAll(createExpand(row, odataEntity.getId(), ""));
    return odataEntity;
  }

  protected Collection<? extends Link> createExpand(final Tuple row, final URI uri, final String attributeName)
      throws ODataApplicationException {
    final List<Link> entityExpandLinks = new ArrayList<Link>();
    // jpaConversionTargetEntity.
    final Map<JPAAssociationPath, JPAExpandResult> children = jpaQueryResult.getChildren();
    if (children != null) {
      for (final JPAAssociationPath associationPath : children.keySet()) {
        try {
          JPAStructuredType type;
          if (attributeName != null && !attributeName.isEmpty()) {
            type = ((JPAAttribute) jpaConversionTargetEntity.getPath(attributeName).getPath().get(0))
                .getStructuredType();
          } else
            type = jpaConversionTargetEntity;
          if (type.getDeclaredAssociation(associationPath.getLeaf().getExternalName()) != null) {
            final Link expand = new JPATupleExpandResultConverter(children.get(associationPath), row,
                associationPath, uriHelper, sd, serviceMetadata).getResult();
            entityExpandLinks.add(expand);
          }
        } catch (ODataJPAModelException e) {
          throw new ODataApplicationException("Navigation property not found", HttpStatusCode.INTERNAL_SERVER_ERROR
              .getStatusCode(), Locale.ENGLISH, e);
        }
      }
    }
    return entityExpandLinks;
  }

  protected URI createId(List<? extends JPAAttribute> keyAttributes, Entity entity)
      throws ODataApplicationException, ODataRuntimeException {

    EdmEntityType edmType = serviceMetadata.getEdm().getEntityType(jpaQueryResult.getEntityType().getExternalFQN());
    try {
      // TODO Clarify host-name and port as part of ID see
      // http://docs.oasis-open.org/odata/odata-atom-format/v4.0/cs02/odata-atom-format-v4.0-cs02.html#_Toc372792702

      StringBuffer uriString = new StringBuffer(setName);
      uriString.append("(");
      uriString.append(uriHelper.buildKeyPredicate(edmType, entity));
      uriString.append(")");
      return new URI(uriString.toString());
    } catch (URISyntaxException e) {
      throw new ODataRuntimeException("Unable to create id for entity: " + edmType.getName(), e);
    } catch (IllegalArgumentException e) {
      return null;
    } catch (SerializerException e) {
      throw new ODataRuntimeException("Unable to create id for entity: " + edmType.getName(), e);
    }
  }

  protected Map<String, Method> getGetter(final JPAAttribute structuredAttribute) {
    HashMap<String, Method> pojoMethods = methodsBuffer.get(structuredAttribute.getInternalName());
    if (pojoMethods == null) {
      pojoMethods = new HashMap<String, Method>();
      final Method[] allMethods = structuredAttribute.getStructuredType().getTypeClass().getMethods();
      for (final Method m : allMethods) {
        pojoMethods.put(m.getName(), m);
      }
      methodsBuffer.put(structuredAttribute.getInternalName(), pojoMethods);
    }
    return pojoMethods;
  }

  private void convertAttribute(final Object value, final String externalName, final String prefix,
      final JPAStructuredType jpaStructuredType, final Map<String, ComplexValue> complexValueBuffer,
      final List<Property> properties) throws ODataJPAModelException {

    ComplexValue compexValue = null;
    if (jpaStructuredType.getPath(externalName) != null) {
      final JPAAttribute attribute = (JPAAttribute) jpaStructuredType.getPath(externalName).getPath().get(0);// getLeaf();
      if (attribute != null && !attribute.isKey() && attribute.isComplex()) {
        String bufferKey;
        if (prefix.isEmpty())
          bufferKey = attribute.getExternalName();
        else
          bufferKey = prefix + JPAPath.PATH_SEPERATOR + attribute.getExternalName();
        compexValue = complexValueBuffer.get(bufferKey);
        if (compexValue == null) {
          compexValue = new ComplexValue();
          complexValueBuffer.put(bufferKey, compexValue);
          properties.add(new Property(
              attribute.getStructuredType().getExternalFQN().getFullQualifiedNameAsString(),
              attribute.getExternalName(),
              ValueType.COMPLEX,
              compexValue));
        }
        final List<Property> values = compexValue.getValue();
        final int splitIndex = attribute.getExternalName().length() + JPAPath.PATH_SEPERATOR.length();
        final String attributeName = externalName.substring(splitIndex);
        convertAttribute(value, attributeName, bufferKey, attribute.getStructuredType(), complexValueBuffer, values);
      } else if (attribute.isKey() && attribute.isComplex()) {
        properties.add(new Property(
            null,
            jpaStructuredType.getPath(externalName).getLeaf().getExternalName(),
            ValueType.PRIMITIVE,
            value));
      } else {
        // ...$select=Name1,Address/Region
        properties.add(new Property(
            null,
            externalName,
            ValueType.PRIMITIVE,
            value));
      }
    }
  }

}