package com.sap.olingo.jpa.processor.core.testmodel;

import java.io.Serializable;
import java.sql.Clob;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

/**
 * Entity implementation class for Entity: DummyToBeIgnored
 *
 */
@Entity
@Table(schema = "\"OLINGO\"", name = "\"Comment\"")
public class Comment implements Serializable {
  private static final long serialVersionUID = 1L;

  @Id
  private Integer ID;

  @Lob
  @Column(name = "\"Text\"")
  @Basic(fetch = FetchType.LAZY)
  private Clob text;

  public Comment() {
    super();
  }

  public Integer getID() {
    return this.ID;
  }

  public void setID(Integer ID) {
    this.ID = ID;
  }

}
