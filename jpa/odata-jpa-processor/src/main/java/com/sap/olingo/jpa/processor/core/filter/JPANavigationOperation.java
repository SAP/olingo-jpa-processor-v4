package com.sap.olingo.jpa.processor.core.filter;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Subquery;

import org.apache.olingo.commons.api.edm.EdmType;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriInfoResource;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceKind;
import org.apache.olingo.server.api.uri.UriResourceNavigation;
import org.apache.olingo.server.api.uri.UriResourceProperty;
import org.apache.olingo.server.api.uri.queryoption.ApplyOption;
import org.apache.olingo.server.api.uri.queryoption.CountOption;
import org.apache.olingo.server.api.uri.queryoption.CustomQueryOption;
import org.apache.olingo.server.api.uri.queryoption.DeltaTokenOption;
import org.apache.olingo.server.api.uri.queryoption.ExpandOption;
import org.apache.olingo.server.api.uri.queryoption.FilterOption;
import org.apache.olingo.server.api.uri.queryoption.FormatOption;
import org.apache.olingo.server.api.uri.queryoption.IdOption;
import org.apache.olingo.server.api.uri.queryoption.OrderByOption;
import org.apache.olingo.server.api.uri.queryoption.SearchOption;
import org.apache.olingo.server.api.uri.queryoption.SelectOption;
import org.apache.olingo.server.api.uri.queryoption.SkipOption;
import org.apache.olingo.server.api.uri.queryoption.SkipTokenOption;
import org.apache.olingo.server.api.uri.queryoption.TopOption;
import org.apache.olingo.server.api.uri.queryoption.expression.BinaryOperatorKind;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitor;
import org.apache.olingo.server.api.uri.queryoption.expression.Member;

import com.sap.olingo.jpa.processor.core.query.JPAAbstractQuery;
import com.sap.olingo.jpa.processor.core.query.JPACollectionFilterQuery;
import com.sap.olingo.jpa.processor.core.query.JPANavigationFilterQuery;
import com.sap.olingo.jpa.processor.core.query.JPANavigationProptertyInfo;
import com.sap.olingo.jpa.processor.core.query.JPANavigationQuery;

/**
 * In case the query result shall be filtered on an attribute of navigation target a sub-select will be generated.<p>
 * E.g.<br>
 * - Organizations?$select=ID&$filter=Roles/$count eq 2<br>
 * - CollectionDeeps?$filter=FirstLevel/SecondLevel/Comment/$count eq 2&$select=ID<br>
 * - Organizations?$filter=AdministrativeInformation/Created/User/LastName eq 'Mustermann'<br>
 * - AdministrativeDivisions?$filter=Parent/Parent/CodeID eq 'NUTS1' and DivisionCode eq 'BE212'
 * @author Oliver Grande
 *
 */
final class JPANavigationOperation extends JPAExistsOperation implements JPAExpressionOperator {

  public static boolean hasNavigation(final List<UriResource> uriResourceParts) {
    if (uriResourceParts != null) {
      for (int i = uriResourceParts.size() - 1; i >= 0; i--) {
        if (uriResourceParts.get(i) instanceof UriResourceNavigation)
          return true;
      }
    }
    return false;
  }

  final BinaryOperatorKind operator;
  final JPAMemberOperator jpaMember;
  final JPALiteralOperator operand;

  private final UriResourceKind aggregationType;

  JPANavigationOperation(final JPAFilterComplierAccess jpaComplier, final BinaryOperatorKind operator,
      final JPAOperator left, final JPAOperator right) {

    super(jpaComplier);
    this.aggregationType = null;
    this.operator = operator;
    if (left instanceof JPAMemberOperator) {
      jpaMember = (JPAMemberOperator) left;
      operand = (JPALiteralOperator) right;
    } else {
      jpaMember = (JPAMemberOperator) right;
      operand = (JPALiteralOperator) left;
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public Expression<Boolean> get() throws ODataApplicationException {
    // return converter.cb.greaterThan(getExistsQuery().as("a"), converter.cb.literal('5')); //NOSONAR
    if (aggregationType != null)
      return (Expression<Boolean>) getExistsQuery().getRoots().toArray()[0];
    return converter.cb.exists(getExistsQuery());
  }

  @SuppressWarnings("unchecked")
  @Override
  public Enum<?> getOperator() {
    return null;
  }

  @Override
  Subquery<?> getExistsQuery() throws ODataApplicationException {
    final List<UriResource> allUriResourceParts = new ArrayList<>(uriResourceParts);
    allUriResourceParts.addAll(jpaMember.getMember().getResourcePath().getUriResourceParts());

    // 1. Determine all relevant associations
    final List<JPANavigationProptertyInfo> naviPathList = determineAssoziations(sd, allUriResourceParts);
    JPAAbstractQuery parent = root;
    final List<JPANavigationQuery> queryList = new ArrayList<>();

    // 2. Create the queries and roots
    for (int i = naviPathList.size() - 1; i >= 0; i--) {
      final JPANavigationProptertyInfo naviInfo = naviPathList.get(i);
      if (i == 0 && aggregationType == null) {
        final JPAFilterExpression expression = new JPAFilterExpression(new SubMember(jpaMember), operand.getLiteral(),
            operator);
        if (naviInfo.getUriResiource() instanceof UriResourceProperty)
          queryList.add(new JPACollectionFilterQuery(odata, sd, em, parent, naviInfo.getAssociationPath(), expression,
              determineFrom(i, naviPathList.size(), parent)));
        else
          queryList.add(new JPANavigationFilterQuery(odata, sd, naviInfo.getUriResiource(), parent, em, naviInfo
              .getAssociationPath(), expression, determineFrom(i, naviPathList.size(), parent)));
      } else
        queryList.add(new JPANavigationFilterQuery(odata, sd, naviInfo.getUriResiource(), parent, em, naviInfo
            .getAssociationPath(), determineFrom(i, naviPathList.size(), parent)));
      parent = queryList.get(queryList.size() - 1);
    }
    // 3. Create select statements
    Subquery<?> childQuery = null;
    for (int i = queryList.size() - 1; i >= 0; i--) {
      childQuery = queryList.get(i).getSubQueryExists(childQuery);
    }
    return childQuery;
  }

  private From<?, ?> determineFrom(int i, int size, JPAAbstractQuery parent) {
    return i == size - 1 ? from : parent.getRoot();
  }

  private class SubMember implements Member {
    private final JPAMemberOperator parentMember;

    public SubMember(final JPAMemberOperator parentMember) {
      super();
      this.parentMember = parentMember;
    }

    @Override
    public <T> T accept(final ExpressionVisitor<T> visitor) throws ODataApplicationException {
      return null;
    }

    @Override
    public UriInfoResource getResourcePath() {
      return new SubResource(parentMember);
    }

    @Override
    public EdmType getStartTypeFilter() {
      return null;
    }

    @Override
    public EdmType getType() {
      return null;
    }

    @Override
    public boolean isCollection() {
      return false;
    }

  }

  private class SubResource implements UriInfoResource {
    private final JPAMemberOperator parentMember;

    public SubResource(final JPAMemberOperator member) {
      super();
      this.parentMember = member;
    }

    @Override
    public ApplyOption getApplyOption() {
      return null;
    }

    @Override
    public CountOption getCountOption() {
      return null;
    }

    @Override
    public List<CustomQueryOption> getCustomQueryOptions() {
      return null;
    }

    @Override
    public DeltaTokenOption getDeltaTokenOption() {
      return null;
    }

    @Override
    public ExpandOption getExpandOption() {
      return null;
    }

    @Override
    public FilterOption getFilterOption() {
      return null;
    }

    @Override
    public FormatOption getFormatOption() {
      return null;
    }

    @Override
    public IdOption getIdOption() {
      return null;
    }

    @Override
    public OrderByOption getOrderByOption() {
      return null;
    }

    @Override
    public SearchOption getSearchOption() {
      return null;
    }

    @Override
    public SelectOption getSelectOption() {
      return null;
    }

    @Override
    public SkipOption getSkipOption() {
      return null;
    }

    @Override
    public SkipTokenOption getSkipTokenOption() {
      return null;
    }

    @Override
    public TopOption getTopOption() {
      return null;
    }

    @Override
    public List<UriResource> getUriResourceParts() {
      final List<UriResource> result = new ArrayList<>();
      final List<UriResource> source = parentMember.getMember().getResourcePath().getUriResourceParts();
      for (int i = source.size() - 1; i > 0; i--) {
        if (source.get(i).getKind() == UriResourceKind.navigationProperty
            || source.get(i).getKind() == UriResourceKind.entitySet
            || (source.get(i) instanceof UriResourceProperty && ((UriResourceProperty) source.get(i)).isCollection())) {
          break;
        }
        result.add(0, source.get(i));
      }
      return result;
    }

    @Override
    public String getValueForAlias(final String alias) {
      return null;
    }

  }

  @Override
  public String toString() {
    return "JPANavigationOperation [operator=" + operator + ", jpaMember=" + jpaMember + ", operand=" + operand
        + ", aggregationType=" + aggregationType + "]";
  }
}
