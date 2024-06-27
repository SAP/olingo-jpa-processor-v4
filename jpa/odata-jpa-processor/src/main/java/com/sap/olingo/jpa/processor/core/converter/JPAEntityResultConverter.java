package com.sap.olingo.jpa.processor.core.converter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import javax.annotation.CheckForNull;

import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.serializer.SerializerException;
import org.apache.olingo.server.api.uri.UriHelper;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAElement;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAServiceDocument;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAStructuredType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.api.JPAODataEtagHelper;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAQueryException;

public class JPAEntityResultConverter extends JPAStructuredResultConverter {
  private final EdmEntityType edmEntityType;
  private final UriHelper odataUriHelper;
  private final JPAODataEtagHelper etagHelper;

  public JPAEntityResultConverter(final UriHelper uriHelper, final JPAServiceDocument sd, final List<?> jpaQueryResult,
      final EdmEntityType returnType, final JPAODataEtagHelper etagHelper) throws ODataJPAModelException {
    super(jpaQueryResult, sd.getEntity(returnType));
    this.edmEntityType = returnType;
    this.odataUriHelper = uriHelper;
    this.etagHelper = etagHelper;
  }

  @Override
  public EntityCollection getResult() throws ODataApplicationException, SerializerException, URISyntaxException {
    final EntityCollection odataEntityCollection = new EntityCollection();
    final List<Entity> odataResults = odataEntityCollection.getEntities();

    for (final Object row : jpaQueryResult) {
      final Entity odataEntity = new Entity();
      odataEntity.setType(this.jpaTopLevelType.getExternalFQN().getFullQualifiedNameAsString());
      final List<Property> properties = odataEntity.getProperties();
      convertProperties(row, properties, jpaTopLevelType);
      odataEntity.setETag(createEtag(row, jpaTopLevelType));
      odataEntity.setId(new URI(odataUriHelper.buildKeyPredicate(edmEntityType, odataEntity)));
      odataResults.add(odataEntity);
    }
    return odataEntityCollection;
  }

  @CheckForNull
  private String createEtag(final Object row, final JPAStructuredType jpaType) throws ODataJPAQueryException {
    if (jpaType instanceof final JPAEntityType et) {
      try {
        if (et.hasEtag()) {
          Object value = row;
          for (final JPAElement part : et.getEtagPath().getPath()) {
            final Map<String, Method> methodMap = getMethods(value.getClass());
            final Method getMethod = getGetter(part.getInternalName(), methodMap);
            value = getMethod.invoke(value);
          }
          return etagHelper.asEtag(et, value);
        }
      } catch (final ODataJPAModelException | IllegalAccessException | IllegalArgumentException
          | InvocationTargetException e) {
        throw new ODataJPAQueryException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
      }
    }
    return null;

  }
}
