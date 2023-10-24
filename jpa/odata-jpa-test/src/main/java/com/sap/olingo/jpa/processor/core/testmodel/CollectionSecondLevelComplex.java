package com.sap.olingo.jpa.processor.core.testmodel;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embeddable;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;

@Embeddable
public class CollectionSecondLevelComplex {

  @Column(name = "\"Number\"")
  private Long number;

  @ElementCollection(fetch = FetchType.LAZY)
  @CollectionTable(schema = "\"OLINGO\"", name = "\"InhouseAddress\"",
      joinColumns = @JoinColumn(name = "\"ParentID\"", referencedColumnName = "\"ID\""))
  private List<InhouseAddress> address = new ArrayList<>();

  @ElementCollection(fetch = FetchType.LAZY)
  @OrderColumn(name = "\"Order\"")
  @CollectionTable(schema = "\"OLINGO\"", name = "\"Comment\"",
      joinColumns = @JoinColumn(name = "\"BusinessPartnerID\"", referencedColumnName = "\"ID\""))
  @Column(name = "\"Text\"")
  private List<String> comment = new ArrayList<>();

  public void setNumber(final Long number) {
    this.number = number;
  }

  public void setAddress(final List<InhouseAddress> address) {
    this.address = address;
  }

  public void setComment(final List<String> comment) {
    this.comment = comment;
  }

  public Long getNumber() {
    return number;
  }

  public List<InhouseAddress> getAddress() {
    return address;
  }

  public List<String> getComment() {
    return comment;
  }

  public void addInhouseAddress(final InhouseAddress address) {
    this.address.add(address);
  }
}
