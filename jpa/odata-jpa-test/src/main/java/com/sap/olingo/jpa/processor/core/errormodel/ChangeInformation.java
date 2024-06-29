package com.sap.olingo.jpa.processor.core.errormodel;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

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
