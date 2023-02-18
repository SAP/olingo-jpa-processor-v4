/**
 *
 */
package com.sap.olingo.jpa.metadata.core.edm.extension.vocabularies;

import java.net.URI;

import org.apache.olingo.commons.api.ex.ODataException;

/**
 * @author Oliver Grande
 * @since 1.1.1
 * 15.02.2023
 */
public class ODataVocabularyReadException extends ODataException {
  /**
   *
   */
  private static final long serialVersionUID = 7101416958465873109L;
  private static final String EXCEPTION = "Could not import vocabulary ''0'' from ''1''.";

  /**
   * @param uri
   * @param e
   */
  public ODataVocabularyReadException(final URI uri, final String path, final Exception e) {
    super(EXCEPTION.replaceFirst("'0'", uri.toString()).replaceFirst("'1'", path), e);
  }

  /**
   * @param capabilitiesAlias
   * @param capabilitiesPath
   * @param e
   */
  public ODataVocabularyReadException(final String alias, final String path, final Exception e) {
    super(EXCEPTION.replaceFirst("'0'", alias).replaceFirst("'1'", path), e);
  }
}
