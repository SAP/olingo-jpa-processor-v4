/**
 *
 */
package com.sap.olingo.jpa.metadata.core.edm.extension.vocabularies;

/**
 * @author Oliver Grande
 * @since 1.1.1
 * 13.02.2023
 */
public interface ReferenceAccess {

  /**
   * See: <a
   * href=
   * "http://docs.oasis-open.org/odata/odata/v4.0/errata03/os/complete/part3-csdl/odata-v4.0-errata03-os-part3-csdl-complete.html#_Toc453752506">3.4
   * Element edmx:Include</a>
   * @param namespace Namespace of a schema defined in the referenced CSDL document to be included. The same namespace
   * MUST NOT be included more than once.
   * @param alias Alias for the given namespace. The alias must be unique.
   */
  void addInclude(String namespace, String alias);

}
