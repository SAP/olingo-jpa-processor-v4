package org.apache.olingo.jpa.processor.core.query;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.persistence.Tuple;

import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAAttribute;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import org.apache.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import org.apache.olingo.jpa.metadata.core.edm.mapper.impl.JPAEdmNameBuilder;
import org.apache.olingo.jpa.metadata.core.edm.mapper.impl.ServicDocument;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriHelper;

public class JPATupleResultConverter extends JPATupleAbstractConverter {

  public JPATupleResultConverter(final ServicDocument sd, final JPAExpandResult jpaQueryResult,
      final UriHelper uriHelper) throws ODataJPAModelException, ODataApplicationException {
    super(jpaQueryResult, uriHelper, sd);
  }

  public EntityCollection getResult() throws ODataApplicationException {
    final EntityCollection odataEntityCollection = new EntityCollection();
    final List<Entity> odataResults = odataEntityCollection.getEntities();

    for (final Tuple row : jpaQueryResult.getResult("root")) {
      final Entity odataEntity = convertRow(jpaConversionTargetEntity, row);
      odataResults.add(odataEntity);
    }
    return odataEntityCollection;
  }

  private void createIdCompundEmbedded(final JPAAttribute keyAttribute, final StringBuffer uriString,
      final Object value,
      final String prefix) throws ODataApplicationException {
    final Map<String, Method> methodList = getGetter(keyAttribute);

    try {
      for (final JPAPath keyPath : keyAttribute.getStructuredType().getPathList()) {
        final JPAAttribute keySubAttribute = (JPAAttribute) keyPath.getLeaf();
        uriString.append(prefix);
        uriString.append(JPAPath.PATH_SEPERATOR);
        uriString.append(keySubAttribute.getExternalName());
        uriString.append('=');
        // TODO Nested embedded keys and mixed embedded - not embedded Keys
        final String getter = ACCESS_MODIFIER_GET + JPAEdmNameBuilder.firstToUpper(keySubAttribute.getInternalName());
        final Method get = methodList.get(getter);
        if (get == null)
          throw new ODataApplicationException("Getter not found for " + keySubAttribute.getExternalName(),
              HttpStatusCode.INTERNAL_SERVER_ERROR.ordinal(),
              Locale.ENGLISH);

        createIdSimple(keySubAttribute, uriString, get.invoke(value));
        uriString.append(',');
      }
    } catch (ODataJPAModelException e) {
      throw new ODataApplicationException("Mapping Error", 500, Locale.ENGLISH, e);
    } catch (IllegalAccessException e) {
      throw new ODataApplicationException("Mapping Error", 500, Locale.ENGLISH, e);
    } catch (IllegalArgumentException e) {
      throw new ODataApplicationException("Mapping Error", 500, Locale.ENGLISH, e);
    } catch (InvocationTargetException e) {
      throw new ODataApplicationException("Mapping Error", 500, Locale.ENGLISH, e);
    }
    uriString.delete(uriString.length() - 1, uriString.length());

  }

  private void createIdOneValue(final JPAAttribute keyAttribute, final StringBuffer uriString, final Object value)
      throws ODataJPAModelException {
    if (keyAttribute.getEdmType() == EdmPrimitiveTypeKind.String) {
      // TODO Check which types have to be in "'"
      uriString.append('\'');
      uriString.append(value);
      uriString.append('\'');
    } else
      uriString.append(value);
  }

  private void createIdSimple(final JPAAttribute keyAttribute, final StringBuffer uriString, final Object value)
      throws ODataJPAModelException, ODataApplicationException {

    if (keyAttribute.isComplex()) {
      final List<JPAPath> attributes = keyAttribute.getStructuredType().getPathList();
      if (attributes.size() == 1)
        createIdSimple((JPAAttribute) attributes.get(0).getLeaf(), uriString, value);
      else
        createIdCompundEmbedded(keyAttribute, uriString, value, keyAttribute.getExternalName());
    } else {
      createIdOneValue(keyAttribute, uriString, value);
    }
  }
}
