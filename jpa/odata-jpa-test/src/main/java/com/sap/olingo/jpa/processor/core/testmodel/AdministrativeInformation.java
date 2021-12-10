package com.sap.olingo.jpa.processor.core.testmodel;

import java.sql.Date;
import java.time.LocalDate;

import javax.persistence.AssociationOverride;
import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.persistence.JoinColumn;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

@Embeddable
public class AdministrativeInformation {

  @Embedded
  @AttributeOverride(name = "by", column = @Column(name = "\"CreatedBy\""))
  @AttributeOverride(name = "at", column = @Column(name = "\"CreatedAt\""))
  @AssociationOverride(name = "user",
      joinColumns = @JoinColumn(referencedColumnName = "\"ID\"", name = "\"CreatedBy\"", insertable = false,
          updatable = false))
  private ChangeInformation created;

  @Embedded
  @AttributeOverride(name = "by", column = @Column(name = "\"UpdatedBy\""))
  @AttributeOverride(name = "at", column = @Column(name = "\"UpdatedAt\""))
  @AssociationOverride(name = "user",
      joinColumns = @JoinColumn(referencedColumnName = "\"ID\"", name = "\"UpdatedBy\"", insertable = false,
          updatable = false))
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
    created = new ChangeInformation("99", Date.valueOf(LocalDate.now()));
  }

  @PreUpdate
  void onUpdate() {
    updated = new ChangeInformation("99", Date.valueOf(LocalDate.now()));
  }
}
