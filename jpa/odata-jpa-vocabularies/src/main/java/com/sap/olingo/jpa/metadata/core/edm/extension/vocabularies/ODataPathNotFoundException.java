/**
 *
 */
package com.sap.olingo.jpa.metadata.core.edm.extension.vocabularies;

import org.apache.olingo.commons.api.ex.ODataException;

/**
 * @author Oliver Grande
 * @since 1.1.1
 * 13.02.2023
 */
public class ODataPathNotFoundException extends ODataException {

  /**
   *
   */
  private static final long serialVersionUID = 5003835145918311048L;
  private static final String MESSAGE = "Path ''0'' could not completly converted. Unknown part: ''1''.";
  private static final String EXCEPTION = "An exception occured during converation of path ''0''.";

  /**
   * @param pathElementNotFound
   * @param pathItem
   * @param internalPath
   */
  public ODataPathNotFoundException(final String pathItem, final String path) {
    super(MESSAGE.replaceFirst("'0'", path).replaceFirst("'1'", pathItem));
  }

  /**
   * @param internalPath
   * @param e
   */
  public ODataPathNotFoundException(final String path, final Exception e) {
    super(EXCEPTION.replaceFirst("'0'", path), e);
  }

}
