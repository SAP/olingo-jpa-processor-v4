/**
 *
 */
package com.sap.olingo.jpa.processor.core.testmodel;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmIgnore;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmTransient;

/**
 * @author Oliver Grande
 * Created: 08.12.2019
 *
 */
@Entity(name = "TransientRefIgnore")
@Table(schema = "\"OLINGO\"", name = "\"BusinessPartner\"")
public class TransientRefIgnore {
  @Id
  @Column(name = "\"ID\"")
  protected String iD;

  @EdmTransient(requiredAttributes = { "name1", "name2" }, calculator = FullNameCalculator.class)
  @Transient
  private String concatenatedAddr;

  @EdmIgnore
  @Column(name = "\"NameLine1\"")
  private String name1;

  @EdmIgnore
  @Column(name = "\"NameLine2\"")
  private String name2;
}
