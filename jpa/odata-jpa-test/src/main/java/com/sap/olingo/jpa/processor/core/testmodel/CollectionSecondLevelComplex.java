package com.sap.olingo.jpa.processor.core.testmodel;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OrderColumn;

@Embeddable
public class CollectionSecondLevelComplex {

  @Column(name = "\"Number\"")
  private Long number;

  @ElementCollection(fetch = FetchType.LAZY)
  @CollectionTable(schema = "\"OLINGO\"", name = "\"InhouseAddress\"",
      joinColumns = @JoinColumn(name = "\"ID\"", referencedColumnName = "\"ID\""))
  private List<InhouseAddress> address = new ArrayList<>();

  @ElementCollection(fetch = FetchType.LAZY)
  @OrderColumn(name = "\"Order\"")
  @CollectionTable(schema = "\"OLINGO\"", name = "\"Comment\"",
      joinColumns = @JoinColumn(name = "\"BusinessPartnerID\"", referencedColumnName = "\"ID\""))
  @Column(name = "\"Text\"")
  private List<String> comment = new ArrayList<>();;

  public void setNumber(Long number) {
    this.number = number;
  }

  public void setAddress(List<InhouseAddress> address) {
    this.address = address;
  }

  public void setComment(List<String> comment) {
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

  public void addInhouseAddress(InhouseAddress address) {
    this.address.add(address);
  }
}
