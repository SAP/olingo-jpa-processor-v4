package org.apache.olingo.jpa.processor.core.testmodel;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.apache.olingo.jpa.metadata.core.edm.annotation.EdmIgnore;

/**
 * Entity implementation class for Entity: DummyToBeIgnored
 *
 */
@Entity
@Table(schema = "\"OLINGO\"", name = "\"org.apache.olingo.jpa::DummyToBeIgnored\"")
@EdmIgnore
public class DummyToBeIgnored implements Serializable {

  @Id
  private String ID;
  private static final long serialVersionUID = 1L;

  public DummyToBeIgnored() {
    super();
  }

  public String getID() {
    return this.ID;
  }

  public void setID(String ID) {
    this.ID = ID;
  }

}
