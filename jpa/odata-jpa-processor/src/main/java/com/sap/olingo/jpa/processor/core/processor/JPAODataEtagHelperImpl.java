package com.sap.olingo.jpa.processor.core.processor;

import java.sql.Timestamp;
import java.util.Collection;

import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.etag.ETagHelper;
import org.apache.olingo.server.api.etag.PreconditionException;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEtagValidator;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.api.JPAODataEtagHelper;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAQueryException;

final class JPAODataEtagHelperImpl implements JPAODataEtagHelper {

  private final ETagHelper olingoHelper;

  JPAODataEtagHelperImpl(final OData odata) {
    this.olingoHelper = odata.createETagHelper();
  }

  @Override
  public boolean checkReadPreconditions(final String etag, final Collection<String> ifMatchHeaders,
      final Collection<String> ifNoneMatchHeaders) throws PreconditionException {
    return olingoHelper.checkReadPreconditions(etag, ifMatchHeaders, ifNoneMatchHeaders);
  }

  @Override
  public void checkChangePreconditions(final String etag, final Collection<String> ifMatchHeaders,
      final Collection<String> ifNoneMatchHeaders) throws PreconditionException {
    olingoHelper.checkChangePreconditions(etag, ifMatchHeaders, ifNoneMatchHeaders);
  }

  @Override
  public String asEtag(final JPAEntityType entityType, final Object value) throws ODataJPAQueryException {

    try {
      if (entityType.hasEtag()) {
        final var etag = new StringBuilder();
        if (value != null) {
          if (entityType.getEtagValidator() == JPAEtagValidator.WEAK)
            etag.append("W/");
          etag.append("\"")
              .append(value instanceof final Timestamp t ? t.toInstant().toString() : value.toString())
              .append("\"");
        }
        return etag.toString();
      }
      return null;
    } catch (final ODataJPAModelException e) {
      throw new ODataJPAQueryException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
    }
  }

}
