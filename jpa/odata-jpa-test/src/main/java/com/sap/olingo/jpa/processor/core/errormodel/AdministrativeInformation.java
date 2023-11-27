package com.sap.olingo.jpa.processor.core.errormodel;

import java.sql.Timestamp;
import java.util.Date;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

@Embeddable
public class AdministrativeInformation {

  @Embedded
  @AttributeOverride(name = "by", column = @Column(name = "\"CreatedBy\""))
  @AttributeOverride(name = "at", column = @Column(name = "\"CreatedAt\""))
  private ChangeInformation created;

  @Embedded
  @AttributeOverride(name = "by", column = @Column(name = "\"UpdatedBy\""))
  @AttributeOverride(name = "at", column = @Column(name = "\"UpdatedAt\""))
  private ChangeInformation updated;

  public ChangeInformation getCreated() {
    return created;
  }

  public ChangeInformation getUpdated() {
    return updated;
  }

  public void setCreated(final ChangeInformation created) {
    this.created = created;
  }

  public void setUpdated(final ChangeInformation updated) {
    this.updated = updated;
  }

  @PrePersist
  void onCreate() {
    created = new ChangeInformation("99", new Timestamp(new Date().getTime()));
  }

  @PreUpdate
  void onUpdate() {
    updated = new ChangeInformation("99", new Timestamp(new Date().getTime()));
  }
}
