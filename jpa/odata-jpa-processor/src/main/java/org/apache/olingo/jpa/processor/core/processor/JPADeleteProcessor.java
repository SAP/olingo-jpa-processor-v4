package org.apache.olingo.jpa.processor.core.processor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmPrimitiveType;
import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeException;
import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAAttribute;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import org.apache.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import org.apache.olingo.jpa.metadata.core.edm.mapper.impl.JPATypeConvertor;
import org.apache.olingo.jpa.processor.core.api.JPAODataRequestContextAccess;
import org.apache.olingo.jpa.processor.core.api.JPAODataSessionContextAccess;
import org.apache.olingo.jpa.processor.core.exception.ODataJPAProcessException;
import org.apache.olingo.jpa.processor.core.exception.ODataJPAProcessorException;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;

public class JPADeleteProcessor {
  final private OData odata;
  final private JPAODataSessionContextAccess sessionContext;
  final private JPAODataRequestContextAccess requestContext;

  public JPADeleteProcessor(OData odata, JPAODataSessionContextAccess sessionContext,
      JPAODataRequestContextAccess requestContext) {
    this.odata = odata;
    this.sessionContext = sessionContext;
    this.requestContext = requestContext;
  }

  /*
   * 4.4 Addressing References between Entities
   * DELETE http://host/service/Categories(1)/Products/$ref?$id=../../Products(0)
   * DELETE http://host/service/Products(0)/Category/$ref
   */
  public void deleteEntity(final ODataResponse response) throws ODataJPAProcessException {
    final JPACUDRequestHandler handler = sessionContext.getCUDRequestHandler();
    final EntityManager em = requestContext.getEntityManager();
    final JPAEntityType et;
    final Map<String, Object> jpaKeyPredicates = new HashMap<String, Object>();

    // 1. Retrieve the entity set which belongs to the requested entity
    List<UriResource> resourcePaths = requestContext.getUriInfo().getUriResourceParts();
    // Note: only in our example we can assume that the first segment is the EntitySet
    UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourcePaths.get(0);
    EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();
    // 2. Convert Key from URL to JPA
    try {
      et = sessionContext.getEdmProvider().getServiceDocument().getEntity(edmEntitySet.getName());
    } catch (ODataJPAModelException e) {
      throw new ODataJPAProcessorException(e, HttpStatusCode.BAD_REQUEST);
    } catch (ODataException e) {
      throw new ODataJPAProcessorException(e, HttpStatusCode.BAD_REQUEST);
    }
    List<UriParameter> uriKeyPredicates = uriResourceEntitySet.getKeyPredicates();
    for (UriParameter uriParam : uriKeyPredicates) {
      JPAAttribute attribute;
      try {
        attribute = et.getPath(uriParam.getName()).getLeaf();
        final CsdlProperty edmProperty = (CsdlProperty) attribute.getProperty();
        final EdmPrimitiveTypeKind edmTypeKind = JPATypeConvertor.convertToEdmSimpleType(attribute.getType());
        final EdmPrimitiveType edmType = odata.createPrimitiveTypeInstance(edmTypeKind);
        final String key = edmType.fromUriLiteral(uriParam.getText());
        final Object k = edmType.valueOfString(key, edmProperty.isNullable(), edmProperty.getMaxLength(),
            edmProperty.getPrecision(), edmProperty.getScale(), true, attribute.getType());
        jpaKeyPredicates.put(attribute.getInternalName(), k);
      } catch (ODataJPAModelException e) {
        throw new ODataJPAProcessorException(e, HttpStatusCode.BAD_REQUEST);
      } catch (EdmPrimitiveTypeException e) {
        throw new ODataJPAProcessorException(e, HttpStatusCode.BAD_REQUEST);
      }
    }
    // 3. Perform Delete
    em.getTransaction().begin();
    try {
      handler.deleteEntity(et, jpaKeyPredicates, em);
    } catch (ODataJPAProcessException e) {
      throw e;
    } catch (Throwable e) {
      throw new ODataJPAProcessorException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
    }
    em.getTransaction().commit();
    // 4. configure the response object
    response.setStatusCode(HttpStatusCode.NO_CONTENT.getStatusCode());

  }

}
