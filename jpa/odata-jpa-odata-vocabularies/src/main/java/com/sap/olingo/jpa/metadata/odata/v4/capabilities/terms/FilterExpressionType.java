/**
 *
 */
package com.sap.olingo.jpa.metadata.odata.v4.capabilities.terms;

/**
 * @author Oliver Grande
 * Created: 26.04.2021
 *
 */
public enum FilterExpressionType {

  /**
   * Property can be used in a single `eq` clause.
   */
  SINGLE_VALUE("SingleValue"),
  /**
   * Property can be used in multiple `eq` and `in` clauses, combined by `or` (which is logically equivalent to a
   * single `in` clause)
   */
  MULTI_VALUE("MultiValue"),
  /**
   * Property can be used in at most one `ge` and/or one `le` clause, separated by `and`.
   */
  SINGLE_RANGE("SingleRange"),
  /**
   * The filter expression for this property consists of one or more interval expressions combined by `or`. A single
   * interval expression is either a single comparison of the property and a literal value with `eq`, `le`, `lt`, `ge`,
   * or `gt`, or pair of boundaries combined by `and` and enclosed in parentheses. The lower boundary is either `ge` or
   * `gt`, the upper boundary either `le` or `lt`.
   */
  MULTI_RANGE("MultiRange"),
  /**
   * String property can be used as first operand in `startswith`, `endswith`, and `contains` clauses.
   */
  SEARCH_EXPRESSION("SearchExpression"),
  /**
   * The filter expression for this property consists of one or more interval expressions or string comparison functions
   * combined by `or`. See {@linkplain #MULTI_RANGE} for a definition of an interval expression. See
   * {@linkplain #SEARCH_EXPRESSION} for the
   * allowed string comparison functions.
   */
  MULTI_RANGE_SEARCH("MultiRangeOrSearchExpression");

  private final String value;

  /**
   * @param value
   */
  FilterExpressionType(final String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  @Override
  public String toString() {
    return value;

  }
}
