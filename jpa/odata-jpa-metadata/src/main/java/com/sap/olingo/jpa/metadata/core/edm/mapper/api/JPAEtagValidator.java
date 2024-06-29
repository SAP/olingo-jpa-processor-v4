package com.sap.olingo.jpa.metadata.core.edm.mapper.api;

/**
 * Strength of the entity tag (ETag) validator according to
 * <a href="https://datatracker.ietf.org/doc/html/rfc7232#section-2.1">RFC 7232 Section-2.1</a><br>
 *
 * @author Oliver Grande
 * @since 25.06.2024
 * @version 2.1.3
 */
public enum JPAEtagValidator {
  WEAK,
  STRONG
}
