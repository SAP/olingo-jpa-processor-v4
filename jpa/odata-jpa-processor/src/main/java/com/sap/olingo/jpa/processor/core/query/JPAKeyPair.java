package com.sap.olingo.jpa.processor.core.query;

import java.util.List;
import java.util.Map;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAttribute;

/**
 * A pair of comparable entity keys<br>
 * Such a pair is used to forward the lowest and highest key value from a query to the dependent $expand query in case
 * the original query was restricted by <code>$top</code> and/or <code>$skip</code>.<br>
 * The pair is seen as closed interval, that is min and max are seen as part of the result.
 * 
 * @author Oliver Grande
 * Created: 13.10.2019
 * @since 0.3.6
 * @param <T>
 */
@SuppressWarnings("rawtypes")
public class JPAKeyPair {
  private Map<JPAAttribute, Comparable> min;
  private Map<JPAAttribute, Comparable> max;
  private final List<JPAAttribute> keyDefinition;

  public JPAKeyPair(final List<JPAAttribute> keyDef) {
    super();
    this.keyDefinition = keyDef;
  }

  public Map<JPAAttribute, Comparable> getMin() {
    return min;
  }

  @SuppressWarnings("unchecked")
  public <Y extends Comparable<? super Y>> Y getMinElement(final JPAAttribute keyElement) {
    return (Y) min.get(keyElement);
  }

  public Map<JPAAttribute, Comparable> getMax() {
    return max;
  }

  @SuppressWarnings("unchecked")
  public <Y extends Comparable<? super Y>> Y getMaxElement(final JPAAttribute keyElement) {
    return (Y) max.get(keyElement);
  }

  public boolean hasUpperBoundary() {
    return max != null && !min.equals(max);
  }

  @SuppressWarnings("unchecked")
  public void setValue(final Map<JPAAttribute, Comparable> value) {

    for (final JPAAttribute keyElement : keyDefinition) {
      if (min == null || min.get(keyElement) == null
          || (value.get(keyElement) != null
              && value.get(keyElement).compareTo(min.get(keyElement)) < 0)) {
        if (max == null)
          max = min;
        min = value;
        return;
      } else if (max == null || value.get(keyElement).compareTo(max.get(keyElement)) > 0) {
        max = value;
        return;
      } else if (value.get(keyElement).compareTo(max.get(keyElement)) != 0) {
        return;
      }
    }
  }

  @Override
  public String toString() {
    return "JPAKeyPair [min=" + min + ", max=" + max + ", hasUpperBoundary=" + hasUpperBoundary() + "]";
  }
}
