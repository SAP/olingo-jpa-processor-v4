package org.apache.olingo.jpa.processor.core.testmodel;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Embeddable
public class ChangeInformation {

  @Column
  private String by;
  @Column
  private Timestamp at;

  @ManyToOne
  @JoinColumn(name = "by", referencedColumnName = "\"ID\"", insertable = false, updatable = false)
  Person user;

  public ChangeInformation() {}

  public ChangeInformation(String by, Timestamp at) {
    super();
    this.by = by;
    this.at = at;
  }

  public Timestamp getAt() {
    return at;
  }

  public String getBy() {
    return by;
  }

}
