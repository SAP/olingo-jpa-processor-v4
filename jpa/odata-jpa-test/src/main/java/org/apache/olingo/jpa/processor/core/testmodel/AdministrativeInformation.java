package org.apache.olingo.jpa.processor.core.testmodel;

import javax.persistence.AssociationOverride;
import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.persistence.JoinColumn;

@Embeddable
public class AdministrativeInformation {
  @Embedded
  @AttributeOverrides({
      @AttributeOverride(name = "by", column = @Column(name = "\"CreatedBy\"") ),
      @AttributeOverride(name = "at", column = @Column(name = "\"CreatedAt\"") )
  })
  @AssociationOverride(name = "user",
      joinColumns = @JoinColumn(referencedColumnName = "\"ID\"", name = "\"CreatedBy\"") )
  private ChangeInformation created;
  @Embedded
  @AttributeOverrides({
      @AttributeOverride(name = "by", column = @Column(name = "\"UpdatedBy\"") ),
      @AttributeOverride(name = "at", column = @Column(name = "\"UpdatedAt\"") )
  })
  @AssociationOverride(name = "user",
      joinColumns = @JoinColumn(referencedColumnName = "\"ID\"", name = "\"UpdatedBy\"") )
  private ChangeInformation updated;

  public ChangeInformation getCreated() {
    return created;
  }

  public ChangeInformation getUpdated() {
    return updated;
  }

  public void setCreated(ChangeInformation created) {
    this.created = created;
  }

  public void setUpdated(ChangeInformation updated) {
    this.updated = updated;
  }

}
