/**
 * 
 */
package com.sap.olingo.jpa.processor.core.testmodel;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Transient;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmTransient;
import com.sap.olingo.jpa.processor.core.errormodel.DummyPropertyCalculator;

/**
 * @author Oliver Grande
 * Created: 24.03.2020
 *
 */
@Embeddable
public class ComplexWithTransientComplexCollction {

  @Column(name = "\"LevelID\"")
  private Integer levelID;

  @Transient
  @EdmTransient(calculator = DummyPropertyCalculator.class)
  private List<InhouseAddress> transientCollection = new ArrayList<>();
}
