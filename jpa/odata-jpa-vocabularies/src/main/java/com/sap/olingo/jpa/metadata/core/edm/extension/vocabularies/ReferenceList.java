/**
 *
 */
package com.sap.olingo.jpa.metadata.core.edm.extension.vocabularies;

import java.net.URI;

import javax.annotation.Nonnull;

/**
 * @author Oliver Grande
 * @since 1.1.1
 * 13.02.2023
 */
public interface ReferenceList {

  /**
   * @param uri URI of the vocabulary e.g.
   * "http://docs.oasisopen.org/odata/odata/v4.0/os/vocabularies/Org.OData.Capabilities.V1.xml"
   * @param sub-path the vocabulary is stored within a resource folder
   * @return
   */
  ReferenceAccess addReference(@Nonnull final URI uri, @Nonnull final String path)
      throws ODataVocabularyReadException;

}
