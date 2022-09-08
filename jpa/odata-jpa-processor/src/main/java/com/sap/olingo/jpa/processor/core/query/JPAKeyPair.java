package com.sap.olingo.jpa.processor.core.query;

import java.util.List;
import java.util.Map;

import javax.persistence.AttributeConverter;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAttribute;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAKeyPairException;

/**
 * A pair of comparable entity keys.<br>
 * Such a pair is used to forward the lowest and highest key value from a query to the dependent $expand query in case
 * the original query was restricted by <code>$top</code> and/or <code>$skip</code>.
 * The pair is seen as closed interval, that is min and max are seen as part of the result. In case an attribute of the
 * key has a conversion, the converted value is used for the comparison.
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

  public void setValue(final Map<JPAAttribute, Comparable> keyValues) throws ODataJPAKeyPairException {

    for (final JPAAttribute keyElement : keyDefinition) {
      final Comparable value = keyValues.get(keyElement);
      if (min == null || min.get(keyElement) == null
          || (value != null
              && compareValues(value, min, keyElement) < 0)) {
        if (max == null)
          max = min;
        min = keyValues;
        return;
      } else if (max == null || compareValues(value, max, keyElement) > 0) {
        max = keyValues;
        return;
      } else if (compareValues(value, max, keyElement) != 0) {
        return;
      }
    }
  }

  @SuppressWarnings("unchecked")
  private int compareValues(final Comparable value, final Map<JPAAttribute, Comparable> comp,
      final JPAAttribute keyElement) throws ODataJPAKeyPairException {

    final Comparable minValue = comp.get(keyElement);
    if (keyElement.getRawConverter() != null) {
      try {
        final AttributeConverter<Object, Object> converter = keyElement.getRawConverter();
        if (keyElement.getDbType() == Byte[].class || keyElement.getDbType() == byte[].class) {
          return new ComparableByteArray(
              ComparableByteArray.unboxedArray(converter.convertToDatabaseColumn(value))).compareTo(
                  ComparableByteArray.unboxedArray(converter.convertToDatabaseColumn(minValue)));
        }
        return ((Comparable) converter.convertToDatabaseColumn(value))
            .compareTo(converter.convertToDatabaseColumn(minValue));
      } catch (final ClassCastException e) {
        throw new ODataJPAKeyPairException(e, keyElement.getDbType() == null ? keyElement.getType().getSimpleName()
            : keyElement.getDbType().getSimpleName());
      }
    }
    return value.compareTo(minValue);
  }

  @Override
  public String toString() {
    return "JPAKeyPair [min=" + min + ", max=" + max + ", hasUpperBoundary=" + hasUpperBoundary() + "]";
  }
}
