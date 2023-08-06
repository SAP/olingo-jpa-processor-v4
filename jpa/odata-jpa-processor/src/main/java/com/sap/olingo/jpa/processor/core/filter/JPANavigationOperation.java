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
import org.apache.olingo.server.api.uri.queryoption.expression.MethodKind;
import org.apache.olingo.server.api.uri.queryoption.expression.VisitableExpression;

import com.sap.olingo.jpa.processor.core.exception.ODataJPAFilterException;
import com.sap.olingo.jpa.processor.core.query.JPAAbstractQuery;
import com.sap.olingo.jpa.processor.core.query.JPAAbstractSubQuery;
import com.sap.olingo.jpa.processor.core.query.JPACollectionFilterQuery;
import com.sap.olingo.jpa.processor.core.query.JPANavigationFilterQuery;
import com.sap.olingo.jpa.processor.core.query.JPANavigationFilterQueryBuilder;
import com.sap.olingo.jpa.processor.core.query.JPANavigationPropertyInfo;

/**
 * In case the query result shall be filtered on an attribute of navigation target a sub-select will be generated.
 * <p>
 * E.g.<br>
 * - Organizations?$select=ID&$filter=Roles/$count eq 2<br>
 * - CollectionDeeps?$filter=FirstLevel/SecondLevel/Comment/$count eq 2&$select=ID<br>
 * - Organizations?$filter=AdministrativeInformation/Created/User/LastName eq 'Mustermann'<br>
 * - AdministrativeDivisions?$filter=Parent/Parent/CodeID eq 'NUTS1' and DivisionCode eq 'BE212'
 * - AssociationOneToOneSources?$filter=ColumnTarget eq null
 * @author Oliver Grande
 *
 */
final class JPANavigationOperation extends JPAExistsOperation {

  final BinaryOperatorKind operator;
  final JPAMemberOperator jpaMember;
  final JPALiteralOperator operand;
  final MethodKind methodCall;
  private VisitableExpression expression;

  public JPANavigationOperation(final BinaryOperatorKind operator,
      final JPANavigationOperation jpaNavigationOperation, final JPALiteralOperator operand,
      final JPAFilterComplierAccess jpaComplier) {
    super(jpaComplier);
    this.operator = operator;
    this.methodCall = jpaNavigationOperation.methodCall;
    this.jpaMember = jpaNavigationOperation.jpaMember;
    this.operand = operand;
  }

  JPANavigationOperation(final JPAFilterComplierAccess jpaComplier, final BinaryOperatorKind operator,
      final JPAOperator left, final JPAOperator right) {

    super(jpaComplier);
    this.operator = operator;
    this.methodCall = null;
    if (left instanceof JPAMemberOperator) {
      jpaMember = (JPAMemberOperator) left;
      operand = (JPALiteralOperator) right;
    } else {
      jpaMember = (JPAMemberOperator) right;
      operand = (JPALiteralOperator) left;
    }
  }

  public JPANavigationOperation(final JPAFilterComplierAccess jpaComplier, final MethodKind methodCall,
      final List<JPAOperator> parameters) {
    super(jpaComplier);
    this.operator = null;
    this.methodCall = methodCall;
    if (parameters.get(0) instanceof JPAMemberOperator) {
      jpaMember = (JPAMemberOperator) parameters.get(0);
      operand = parameters.size() > 1 ? (JPALiteralOperator) parameters.get(1) : null;
    } else {
      jpaMember = (JPAMemberOperator) parameters.get(1);
      operand = (JPALiteralOperator) parameters.get(0);
    }
  }

  @Override
  public Expression<Boolean> get() throws ODataApplicationException {
    // return converter.cb.greaterThan(getExistsQuery().as("a"), converter.cb.literal('5')); //NOSONAR

    final Subquery<Object> existQuery = getExistsQuery();
    if (expression instanceof JPAInvertibleVisitableExpression
        && ((JPAInvertibleVisitableExpression) expression).isInversionRequired()) {
      ((JPAInvertibleVisitableExpression) expression).inversionPerformed();
      return converter.cb.not(converter.cb.exists(existQuery));
    }
    return converter.cb.exists(existQuery);
  }

  @SuppressWarnings("unchecked")
  @Override
  public Enum<?> getOperator() {
    return null;
  }

  @Override
  public String getName() {
    return operator != null ? operator.name() : methodCall.name();
  }

  @SuppressWarnings("unchecked")
  @Override
  <T> Subquery<T> getExistsQuery() throws ODataApplicationException {
    final List<UriResource> allUriResourceParts = new ArrayList<>(uriResourceParts);
    allUriResourceParts.addAll(jpaMember.getMember().getResourcePath().getUriResourceParts());

    // 1. Determine all relevant associations
    final List<JPANavigationPropertyInfo> navigationPathList = determineAssociations(sd, allUriResourceParts);
    JPAAbstractQuery parent = root;
    final List<JPAAbstractSubQuery> queryList = new ArrayList<>();

    // 2. Create the queries and roots
    for (int i = navigationPathList.size() - 1; i >= 0; i--) {
      final JPANavigationPropertyInfo navigationInfo = navigationPathList.get(i);
      if (i == 0) {
        expression = createExpression();
        if (navigationInfo.getUriResource() instanceof UriResourceProperty) {
          queryList.add(new JPACollectionFilterQuery(odata, sd, em, parent, navigationInfo.getAssociationPath(),
              expression, determineFrom(i, navigationPathList.size(), parent), groups));
        } else {
          queryList.add(new JPANavigationFilterQueryBuilder()
              .setOdata(odata)
              .setServiceDocument(sd)
              .setUriResourceItem(navigationInfo.getUriResource())
              .setParent(parent)
              .setEntityManager(em)
              .setAssociation(navigationInfo.getAssociationPath())
              .setExpression(expression)
              .setFrom(determineFrom(i, navigationPathList.size(), parent))
              .setParent(parent)
              .setClaimsProvider(claimsProvider)
              .setGroups(groups)
              .build());
        }
      } else {
        queryList.add(new JPANavigationFilterQuery(odata, sd, navigationInfo.getUriResource(), parent, em,
            navigationInfo.getAssociationPath(), determineFrom(i, navigationPathList.size(), parent), claimsProvider));
      }
      parent = queryList.get(queryList.size() - 1);
    }
    // 3. Create select statements
    Subquery<?> childQuery = null;
    for (int i = queryList.size() - 1; i >= 0; i--) {
      childQuery = queryList.get(i).getSubQuery(childQuery, expression);
    }
    return (Subquery<T>) childQuery;
  }

  Member getMember() {
    return new SubMember(jpaMember);
  }

  private VisitableExpression createExpression() throws ODataJPAFilterException {
    if (operator != null && methodCall == null) {
      final List<UriResource> parts = jpaMember.getMember().getResourcePath().getUriResourceParts();
      if (UriResourceKind.count == parts.get(parts.size() - 1).getKind())
        return new JPACountExpression(new SubMember(jpaMember), operand.getLiteral(),
            operator);
      if ("null".equals(operand.getLiteral().getText()))
        return new JPANullExpression(new SubMember(jpaMember), operand.getLiteral(),
            operator);
      return new JPAFilterExpression(new SubMember(jpaMember), operand.getLiteral(),
          operator);
    }
    if (operator == null && methodCall != null) {
      return new JPAMethodExpression(new SubMember(jpaMember), operand, this.methodCall);
    } else {
      final JPAVisitableExpression methodExpression = new JPAMethodExpression(new SubMember(jpaMember),
          operand, this.methodCall);
      return new JPABinaryExpression(methodExpression, operand.getLiteral(), operator);
    }
  }

  private From<?, ?> determineFrom(final int i, final int size, final JPAAbstractQuery parent) {
    return i == size - 1 ? from : parent.getRoot();
  }

  private static class SubMember implements Member {
    private final JPAMemberOperator parentMember;

    SubMember(final JPAMemberOperator parentMember) {
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

  private static class SubResource implements UriInfoResource {
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
      return new ArrayList<>(0);
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
        + ", methodCall=" + methodCall + "]";
  }
}
