package com.sap.olingo.jpa.processor.core.api;

import java.util.Collection;

import javax.annotation.Nonnull;

import org.apache.olingo.server.api.etag.PreconditionException;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAQueryException;

public interface JPAODataEtagHelper {

  /**
   * <p>
   * Checks the preconditions of a read request with a given ETag value
   * against the If-Match and If-None-Match HTTP headers.
   * </p>
   * <p>
   * If the given ETag value is not matched by the ETag information in the If-Match headers,
   * and there are ETags in the headers to be matched, a "Precondition Failed" exception is
   * thrown.
   * </p>
   * <p>
   * If the given ETag value is matched by the ETag information in the If-None-Match headers,
   * <code>true</code> is returned, and applications are supposed to return an empty response
   * with a "Not Modified" status code and the ETag header, <code>false</code> otherwise.
   * </p>
   * <p>
   * All matching uses weak comparison as described in
   * <a href="https://www.ietf.org/rfc/rfc7232.txt">RFC 7232</a>, section 2.3.2.
   * </p>
   * <p>
   * This method does not nothing and returns <code>false</code> if the ETag value is
   * <code>null</code>.
   * </p>
   * @param eTag the ETag value to match
   * @param ifMatchHeaders the If-Match header values
   * @param ifNoneMatchHeaders the If-None-Match header values
   * @return whether a "Not Modified" response should be used
   */
  public boolean checkReadPreconditions(String etag,
      Collection<String> ifMatchHeaders, Collection<String> ifNoneMatchHeaders)
      throws PreconditionException;

  /**
   * <p>
   * Checks the preconditions of a change request (with HTTP methods PUT, PATCH, or DELETE)
   * with a given ETag value against the If-Match and If-None-Match HTTP headers.
   * </p>
   * <p>
   * If the given ETag value is not matched by the ETag information in the If-Match headers,
   * and there are ETags in the headers to be matched, or
   * if the given ETag value is matched by the ETag information in the If-None-Match headers,
   * a "Precondition Failed" exception is thrown.
   * </p>
   * <p>
   * All matching uses weak comparison as described in
   * <a href="https://www.ietf.org/rfc/rfc7232.txt">RFC 7232</a>, section 2.3.2.
   * </p>
   * <p>
   * This method does not nothing if the ETag value is <code>null</code>.
   * </p>
   * @param eTag the ETag value to match
   * @param ifMatchHeaders the If-Match header values
   * @param ifNoneMatchHeaders the If-None-Match header values
   */
  public void checkChangePreconditions(String etag,
      Collection<String> ifMatchHeaders, Collection<String> ifNoneMatchHeaders)
      throws PreconditionException;

  /**
   * Converts the value of an ETag into a the corresponding string. <br>
   * A value is converted by calling the toString method with one exception. In case
   * the value is an instance of type {@link java.sql.Timestamp}, the time stamp is first converted into
   * a {@link java.time.Instant} to get a <a href="https://www.ietf.org/rfc/rfc7232.txt">RFC 7232</a> compliant string.
   *
   * @param entityType the ETag belongs to
   * @param value raw value of the ETag
   * @return ETag string. It is empty if the value is null and null if the entity type has no ETag property
   * @throws ODataJPAQueryException
   */
  public String asEtag(@Nonnull final JPAEntityType entityType, final Object value) throws ODataJPAQueryException;
}
