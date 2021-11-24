package com.sap.olingo.jpa.processor.cb.impl;

import static com.sap.olingo.jpa.processor.cb.impl.TypeConverter.convert;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.persistence.Tuple;
import javax.persistence.TupleElement;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAttribute;

/**
 *
 * @author Oliver Grande
 * @since 1.0.0
 */
class TupleImpl implements Tuple {
  private final Object[] values;
  private final List<Entry<String, JPAAttribute>> selection;
  private final Map<String, Integer> selectionIndex;
  private Optional<List<TupleElement<?>>> tupleElements;

  TupleImpl(final Object value, final List<Entry<String, JPAAttribute>> selection,
      final Map<String, Integer> selectionIndex) {
    this(new Object[] { value }, selection, selectionIndex);
  }

  TupleImpl(final Object[] values, final List<Entry<String, JPAAttribute>> selPath,
      final Map<String, Integer> selectionIndex) {
    super();
    this.values = values;
    this.selection = selPath;
    this.selectionIndex = selectionIndex;
    this.tupleElements = Optional.empty();
  }

  /**
   * Get the value of the element at the specified
   * position in the result tuple. The first position is 0.<p>
   * <b>Please note:</b> As of now <b>no</b> conversions are made.
   * @param i position in result tuple
   * @return value of the tuple element
   * @throws IllegalArgumentException if i exceeds
   * length of result tuple
   */
  @Override
  public Object get(final int index) {
    if (index >= values.length || index < 0)
      throw new IllegalArgumentException("Index out of bound");
    return values[index];
  }

  /**
   * Get the value of the element at the specified
   * position in the result tuple. The first position is 0.<p>
   * <b>Please note:</b> As of now <b>no</b> conversions are made.
   * @param i position in result tuple
   * @param type type of the tuple element
   * @return value of the tuple element
   * @throws IllegalArgumentException if i exceeds
   * length of result tuple or element cannot be
   * assigned to the specified type
   */
  @SuppressWarnings("unchecked")
  @Override
  public <X> X get(final int i, final Class<X> type) {
    return (X) convert(get(i), type);
  }

  /**
   * Get the value of the tuple element to which the
   * specified alias has been assigned.
   * @param alias alias assigned to tuple element
   * @return value of the tuple element
   * @throws IllegalArgumentException if alias
   * does not correspond to an element in the
   * query result tuple
   */
  @Override
  public Object get(final String alias) {

    if (selectionIndex.containsKey(alias)) {
      final int index = selectionIndex.get(alias);
      final JPAAttribute attribute = selection.get(index).getValue();
      if (values[index] == null)
        return null;
      if (attribute.isEnum() && attribute.getConverter() == null && values[index] != null)
        return attribute.getType().getEnumConstants()[(int) values[index]];
      final Object value = convert(values[index], attribute.getDbType());
      if (attribute.getRawConverter() != null)
        return attribute.getRawConverter().convertToEntityAttribute(value);
      return value;
    } else {
      throw new IllegalArgumentException("Unknown alias: " + alias);
    }
  }

  /**
   * Get the value of the tuple element to which the
   * specified alias has been assigned.
   * @param alias alias assigned to tuple element
   * @param type of the tuple element
   * @return value of the tuple element
   * @throws IllegalArgumentException if alias
   * does not correspond to an element in the
   * query result tuple or element cannot be
   * assigned to the specified type
   */
  @SuppressWarnings("unchecked")
  @Override
  public <X> X get(final String alias, final Class<X> type) {
    return (X) convert(get(alias), type);
  }

  /**
   * Get the value of the specified tuple element.
   * @param tupleElement tuple element
   * @return value of tuple element
   * @throws IllegalArgumentException if tuple element
   * does not correspond to an element in the
   * query result tuple
   */
  @Override
  public <X> X get(final TupleElement<X> tupleElement) {
    return get(tupleElement.getAlias(), tupleElement.getJavaType());
  }

  /**
   * Return the tuple elements.
   * @return tuple elements
   */
  @Override
  public List<TupleElement<?>> getElements() {
    return tupleElements.orElseGet(this::asTupleElements);
  }

  /**
   * Return the values of the result tuple elements as an array.
   * @return tuple element values
   */
  @Override
  public Object[] toArray() {
    return Arrays.copyOf(values, values.length);
  }

  private List<TupleElement<?>> asTupleElements() {
    tupleElements = Optional.of(
        selectionIndex.entrySet()
            .stream()
            .map(e -> new TupleElementImpl<>(e.getValue()))
            .collect(Collectors.toList()));
    return tupleElements.orElseThrow(IllegalStateException::new);
  }

  private class TupleElementImpl<X> implements TupleElement<X> {
    private final int index;

    private TupleElementImpl(final int index) {
      super();
      this.index = index;
    }

    @Override
    public String getAlias() {
      return selection.get(index).getKey();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<? extends X> getJavaType() {
      return (Class<? extends X>) selection.get(index).getValue().getType();
    }
  }
}
