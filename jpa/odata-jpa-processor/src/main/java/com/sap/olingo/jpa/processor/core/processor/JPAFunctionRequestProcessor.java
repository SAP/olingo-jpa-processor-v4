package com.sap.olingo.jpa.processor.core.processor;

import static com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessorException.MessageKeys.FUNCTION_UNKNOWN;

import org.apache.olingo.commons.api.data.Annotatable;
import org.apache.olingo.commons.api.edm.EdmType;
import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ODataLibraryException;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.uri.UriResourceFunction;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmFunctionType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPADataBaseFunction;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAFunction;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAJavaFunction;
import com.sap.olingo.jpa.processor.core.api.JPAODataDatabaseProcessor;
import com.sap.olingo.jpa.processor.core.api.JPAODataRequestContextAccess;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessorException;

/**
 * Functions as User Defined Functions, Native Query, as Criteria Builder does not provide the option to used UDFs in
 * the From clause.
 * @author Oliver Grande
 *
 */
public final class JPAFunctionRequestProcessor extends JPAOperationRequestProcessor implements JPARequestProcessor { // NOSONAR

  private final JPAODataDatabaseProcessor dbProcessor;

  public JPAFunctionRequestProcessor(final OData odata, final JPAODataRequestContextAccess requestContext)
      throws ODataException {
    super(odata, requestContext);
    this.dbProcessor = requestContext.getDatabaseProcessor();
  }

  @Override
  public void retrieveData(final ODataRequest request, final ODataResponse response, final ContentType responseFormat)
      throws ODataApplicationException, ODataLibraryException {

    final UriResourceFunction uriResourceFunction =
        (UriResourceFunction) uriInfo.getUriResourceParts().get(uriInfo.getUriResourceParts().size() - 1);
    final JPAFunction jpaFunction = sd.getFunction(uriResourceFunction.getFunction());
    if (jpaFunction == null)
      throw new ODataJPAProcessorException(FUNCTION_UNKNOWN, HttpStatusCode.BAD_REQUEST, uriResourceFunction
          .getFunction().getName());
    Object result = null;
    if (jpaFunction.getFunctionType() == EdmFunctionType.JavaClass) {
      result = new JPAJavaFunctionProcessor(sd, uriResourceFunction, (JPAJavaFunction) jpaFunction, em).process();
    } else if (jpaFunction.getFunctionType() == EdmFunctionType.UserDefinedFunction) {
      result = dbProcessor.executeFunctionQuery(uriInfo.getUriResourceParts(), (JPADataBaseFunction) jpaFunction, em);
    }
    final EdmType returnType = uriResourceFunction.getFunction().getReturnType().getType();
    final Annotatable annotatable = convertResult(result, returnType, jpaFunction);
    serializeResult(returnType, response, responseFormat, annotatable, request);
  }
}
