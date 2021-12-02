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

  String getReferencedColumnName();

  String getName();

}
