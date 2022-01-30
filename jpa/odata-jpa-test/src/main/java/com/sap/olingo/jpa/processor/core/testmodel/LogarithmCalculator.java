/**
 * 
 */
package com.sap.olingo.jpa.processor.core.testmodel;

import java.util.Optional;

import javax.persistence.Tuple;
import javax.persistence.TupleElement;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmTransientPropertyCalculator;

/**
 * @author Oliver Grande
 * Created: 17.03.2020
 *
 */
public class LogarithmCalculator implements EdmTransientPropertyCalculator<Double> {

  @Override
  public Double calculateProperty(final Tuple row) {
    final Optional<TupleElement<?>> element = row.getElements()
        .stream()
        .filter(e -> e.getAlias().contains("Number"))
        .findFirst();
    if (element.isPresent()) {
      final Long number = (Long) row.get(element.get());
      return Math.log(number);
    }
    return null;
  }
}
