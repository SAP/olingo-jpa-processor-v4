package com.sap.olingo.jpa.processor.core.testmodel;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Embeddable
public class ChangeInformation {

  @Column
  private String by;
  @Column(precision = 9)
  @Temporal(TemporalType.TIMESTAMP)
  private Date at;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "\"by\"", referencedColumnName = "\"ID\"", insertable = false, updatable = false)
  Person user;

  public ChangeInformation() {}

  public ChangeInformation(String by, Date at) {
    super();
    this.by = by;
    this.at = at;
  }

  public Date getAt() {
    return at;
  }

  public String getBy() {
    return by;
  }

  public void setBy(String by) {
    if (by == null) {
      throw new NullPointerException();
    }
    this.by = by;
  }

  public void setAt(Date at) {
    this.at = at;
  }

  public Person getUser() {
    return user;
  }

  public void setUser(Person user) {
    this.user = user;
    this.by = user.getID();
  }

}
