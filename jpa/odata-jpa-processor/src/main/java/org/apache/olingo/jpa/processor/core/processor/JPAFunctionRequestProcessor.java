package org.apache.olingo.jpa.processor.core.processor;

import java.net.URISyntaxException;
import java.util.List;
import java.util.Locale;

import javax.persistence.EntityManager;

import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAFunction;
import org.apache.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import org.apache.olingo.jpa.processor.core.api.JPAODataContextAccess;
import org.apache.olingo.jpa.processor.core.api.JPAODataDatabaseProcessor;
import org.apache.olingo.jpa.processor.core.query.JPAInstanceResultConverter;
import org.apache.olingo.jpa.processor.core.serializer.JPASerializer;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ODataLibraryException;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.serializer.SerializerResult;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriResourceFunction;

/**
 * Functions as User Defined Functions, Native Query, as Criteria Builder does not provide the option to used UDFs in
 * the From clause.
 * @author Oliver Grande
 *
 */
public class JPAFunctionRequestProcessor extends JPAAbstractRequestProcessor {

  private final JPAODataDatabaseProcessor dbProcessor;

  public JPAFunctionRequestProcessor(final OData odata, final JPAODataContextAccess context, final EntityManager em,
      final UriInfo uriInfo, final JPASerializer serializer) {
    super(odata, context, em, uriInfo, serializer);
    this.dbProcessor = context.getDatabaseProcessor();
  }

  @Override
  public void retrieveData(final ODataRequest request, final ODataResponse response, final ContentType responseFormat)
      throws ODataApplicationException, ODataLibraryException {

    final UriResourceFunction uriResourceFunction = (UriResourceFunction) uriInfo.getUriResourceParts().get(0);
    final JPAFunction jpaFunction = sd.getFunction(uriResourceFunction.getFunction());
    final JPAEntityType returnType = sd.getEntity(jpaFunction.getReturnType());

    // dbProcessor.query

    final List<?> nr = dbProcessor.executeFunctionQuery(uriResourceFunction, jpaFunction, returnType, em);

    EntityCollection entityCollection;
    final EdmEntitySet returnEntitySet = uriResourceFunction.getFunctionImport().getReturnedEntitySet();
    try {
      entityCollection = new JPAInstanceResultConverter(odata.createUriHelper(), sd, nr, returnEntitySet, returnType
          .getTypeClass()).getResult();
    } catch (ODataJPAModelException e) {
      throw new ODataApplicationException("Result could not be created", HttpStatusCode.INTERNAL_SERVER_ERROR
          .getStatusCode(), Locale.ENGLISH, e);
    } catch (URISyntaxException e) {
      throw new ODataApplicationException("Result could not be created", HttpStatusCode.INTERNAL_SERVER_ERROR
          .getStatusCode(), Locale.ENGLISH, e);
    }

    if (entityCollection.getEntities() != null && entityCollection.getEntities().size() > 0) {
      final SerializerResult serializerResult = serializer.serialize(request, entityCollection);
      createSuccessResonce(response, responseFormat, serializerResult);
    } else
      response.setStatusCode(HttpStatusCode.NO_CONTENT.getStatusCode());

  }
}
