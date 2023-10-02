package com.sap.olingo.jpa.processor.core.testmodel;

import java.util.Date;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

@Embeddable
public class ChangeInformation {

  @Column
  private String by;
  @Column(precision = 9)
  @Temporal(TemporalType.TIMESTAMP)
  private Date at;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "\"by\"", referencedColumnName = "\"ID\"", insertable = false, updatable = false)
  private Person user;

  public ChangeInformation() {}

  public ChangeInformation(final String by, final Date at) {
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

  public void setBy(final String by) {
    Objects.nonNull(by);
    this.by = by;
  }

  public void setAt(final Date at) {
    this.at = at;
  }

  public Person getUser() {
    return user;
  }

  public void setUser(final Person user) {
    this.user = user;
    this.by = user.getID();
  }

}
