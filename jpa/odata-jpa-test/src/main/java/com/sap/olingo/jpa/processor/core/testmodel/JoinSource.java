package com.sap.olingo.jpa.processor.core.testmodel;

import java.util.List;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity(name = "JoinSource")
@Table(schema = "\"OLINGO\"", name = "\"JoinSource\"")
public class JoinSource {

  @Id
  @Column(name = "\"SourceKey\"")
  private Integer sourceID;

  @OneToMany
  @JoinTable(name = "\"JoinRelation\"", schema = "\"OLINGO\"",
      joinColumns = @JoinColumn(name = "\"SourceID\""),
      inverseJoinColumns = @JoinColumn(name = "\"TargetID\""))
  private List<JoinTarget> oneToMany;

  @OneToMany
  @JoinTable(name = "\"JoinHiddenRelation\"", schema = "\"OLINGO\"",
      joinColumns = @JoinColumn(name = "\"SourceID\""),
      inverseJoinColumns = @JoinColumn(name = "\"TargetID\""))
  private List<JoinTarget> oneToManyHidden;

  @Embedded
  private JoinComplex complex;

  public JoinSource(final Integer sourceID, final JoinComplex complex, final List<JoinTarget> oneToMany) {
    super();
    this.sourceID = sourceID;
    this.oneToMany = oneToMany;
    this.complex = complex;
  }

  public JoinSource() {
    super();
  }

  public Integer getSourceID() {
    return sourceID;
  }

  public void setSourceID(final Integer sourceID) {
    this.sourceID = sourceID;
  }

  public List<JoinTarget> getOneToMany() {
    return oneToMany;
  }

  public void setOneToMany(final List<JoinTarget> oneToMany) {
    this.oneToMany = oneToMany;
  }

  public JoinComplex getComplex() {
    return complex;
  }

  public void setComplex(final JoinComplex complex) {
    this.complex = complex;
  }

  @Override
  public int hashCode() {
    return Objects.hash(sourceID);
  }

  @Override
  public boolean equals(final Object object) {
    return object instanceof final JoinSource other
        && Objects.equals(sourceID, other.sourceID);
  }
}
