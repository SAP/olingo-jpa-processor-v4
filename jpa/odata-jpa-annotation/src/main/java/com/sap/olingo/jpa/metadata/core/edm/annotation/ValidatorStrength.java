package com.sap.olingo.jpa.metadata.core.edm.annotation;

/**
 * Strength of the entity tag (ETag) validator according to
 * <a href="https://datatracker.ietf.org/doc/html/rfc7232#section-2.1">RFC 7232 Section-2.1</a><br>
 * In case DEFAULT is chosen, in case the attribute marked with {@link jakarta.persistence.Version} is seen as WEAK in
 * case it is of type {@link java.sql.Timestamp},
 * otherwise as STRONG.
 *
 * @author Oliver Grande
 * @since 24.06.2024
 * @version 2.1.3
 */
public enum ValidatorStrength {

  DEFAULT,
  WEAK,
  STRONG;
}
