package com.sap.olingo.jpa.processor.core.testmodel;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Embeddable;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Embeddable
public class RestrictedEntityComplex {

  // @Column()

  @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.REFRESH)
  @JoinColumn(name = "\"MemberId\"", referencedColumnName = "\"ParentId\"", insertable = false, updatable = false,
      nullable = true)
  private RestrictedEntity restricted;

}
