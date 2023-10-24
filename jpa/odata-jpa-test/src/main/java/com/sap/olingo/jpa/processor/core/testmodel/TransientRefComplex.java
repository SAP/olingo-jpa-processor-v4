/**
 *
 */
package com.sap.olingo.jpa.processor.core.testmodel;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmTransient;

/**
 * @author Oliver Grande
 * Created: 08.12.2019
 *
 */
@Entity(name = "TransientRefComplex")
@Table(schema = "\"OLINGO\"", name = "\"BusinessPartner\"")
public class TransientRefComplex {
  @Id
  @Column(name = "\"ID\"")
  protected String iD;

  @EdmTransient(requiredAttributes = { "addr/building" }, calculator = FullNameCalculator.class)
  @Transient
  private String concatenatedName;

  @Embedded
  private InhouseAddressWithGroup addr;
}
