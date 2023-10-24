/**
 *
 */
package com.sap.olingo.jpa.processor.core.testmodel;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Transient;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmTransient;
import com.sap.olingo.jpa.processor.core.errormodel.DummyPropertyCalculator;

/**
 * @author Oliver Grande
 * Created: 24.03.2020
 *
 */
@Embeddable
public class ComplexWithTransientComplexCollection {

  @Column(name = "\"LevelID\"")
  private Integer levelID;

  @Transient
  @EdmTransient(calculator = DummyPropertyCalculator.class)
  private final List<InhouseAddress> transientCollection = new ArrayList<>();
}
