/**
 *
 */
package com.sap.olingo.jpa.metadata.api;

/**
 * @author Oliver Grande
 * Created: 02.02.2020
 *
 */
public interface JPAJoinColumn {
  /**
   * Returns the name of the column of the target table. This can be different from the name given in the @JoinColumn
   * annotation.
   * @return
   */
  String getReferencedColumnName();

  /**
   * Returns the name of the column of the source table. This can be different from the name given in the @JoinColumn
   * annotation.
   * @return
   */
  String getName();

}
