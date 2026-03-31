package com.sap.olingo.jpa.processor.core.testmodel;

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity(name = "JoinTarget")
@Table(schema = "\"OLINGO\"", name = "\"JoinTarget\"")
public class JoinTarget {
  @Id
  @Column(name = "\"TargetKey\"")
  private Integer targetID;

  @ManyToOne()
  @JoinTable(name = "\"JoinRelation\"", schema = "\"OLINGO\"",
      joinColumns = @JoinColumn(name = "\"TargetID\""),
      inverseJoinColumns = @JoinColumn(name = "\"SourceID\""))
  private JoinSource manyToOne;

  public JoinTarget() {
    super();
  }

  public JoinTarget(final Integer targetID) {
    super();
    this.targetID = targetID;
  }

  public Integer getTargetID() {
    return targetID;
  }

  public void setTargetID(final Integer targetID) {
    this.targetID = targetID;
  }

  public JoinSource getManyToOne() {
    return manyToOne;
  }

  public void setManyToOne(final JoinSource manyToOne) {
    this.manyToOne = manyToOne;
  }

  @Override
  public int hashCode() {
    return Objects.hash(targetID);
  }

  @Override
  public boolean equals(final Object object) {
    return object instanceof final JoinTarget other
        && Objects.equals(targetID, other.targetID);
  }
}
