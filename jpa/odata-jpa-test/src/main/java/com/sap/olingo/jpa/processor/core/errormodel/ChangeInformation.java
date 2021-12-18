package com.sap.olingo.jpa.processor.core.errormodel;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Embeddable
public class ChangeInformation {

  @Column
  private String by;
  @Column(precision = 9)
  @Temporal(TemporalType.TIMESTAMP)
  private Date at;

  String user;

  public ChangeInformation() {}

  public ChangeInformation(final String by, final Date at) {
    super();
    this.by = by;
    this.at = at;
  }
}
