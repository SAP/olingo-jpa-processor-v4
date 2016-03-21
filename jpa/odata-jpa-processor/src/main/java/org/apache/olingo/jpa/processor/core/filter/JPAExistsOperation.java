package org.apache.olingo.jpa.processor.core.filter;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Subquery;

import org.apache.olingo.jpa.metadata.core.edm.mapper.impl.ServicDocument;
import org.apache.olingo.jpa.processor.core.query.JPAAbstractQuery;
import org.apache.olingo.jpa.processor.core.query.JPANavigationFilterQuery;
import org.apache.olingo.jpa.processor.core.query.JPANavigationProptertyInfo;
import org.apache.olingo.jpa.processor.core.query.JPANavigationQuery;
import org.apache.olingo.jpa.processor.core.query.Util;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriInfoResource;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceKind;
import org.apache.olingo.server.api.uri.queryoption.CountOption;
import org.apache.olingo.server.api.uri.queryoption.CustomQueryOption;
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

/**
 * In case the query result shall be filtered on an attribute of navigation target a sub-select will be generated.
 * @author Oliver Grande
 *
 */
class JPAExistsOperation implements JPAExpressionOperator {

  private final JPAOperationConverter converter;
  private final BinaryOperatorKind operator;
  private final JPAMemberOperator member;
  private final JPALiteralOperator operand;
  private final List<UriResource> uriResourceParts;
  private final JPAAbstractQuery root;
  private final ServicDocument sd;
  private final EntityManager em;
  private final OData odata;

  public JPAExistsOperation(final OData odata, ServicDocument sd, EntityManager em, List<UriResource> uriResourceParts,
      final JPAOperationConverter converter, BinaryOperatorKind operator, JPAOperator left,
      JPAOperator right, final JPAAbstractQuery root) {
    super();
    this.operator = operator;
    if (left instanceof JPAMemberOperator) {
      member = (JPAMemberOperator) left;
      operand = (JPALiteralOperator) right;
    } else {
      member = (JPAMemberOperator) right;
      operand = (JPALiteralOperator) left;
    }
    this.uriResourceParts = uriResourceParts;
    this.root = root;
    this.sd = sd;
    this.em = em;
    this.converter = converter;
    this.odata = odata;
  }

  @Override
  public Expression<Boolean> get() throws ODataApplicationException {
    return converter.convert(this);
  }

  public Subquery<?> getSubQuery() throws ODataApplicationException {
    final List<UriResource> allUriResourceParts = new ArrayList<UriResource>(uriResourceParts);
    allUriResourceParts.addAll(member.getMember().getUriResourceParts());

    // 1. Determine all relevant associations
    final List<JPANavigationProptertyInfo> naviPathList = Util.determineAssoziations(sd, allUriResourceParts);
    JPAAbstractQuery parent = root;
    final List<JPANavigationQuery> queryList = new ArrayList<JPANavigationQuery>();

    // 2. Create the queries and roots
    JPAFilterExpression expression = new JPAFilterExpression(new SubMember(member), operand.getLiteral(), operator);
    for (int i = 0; i < naviPathList.size(); i++) {
      final JPANavigationProptertyInfo naviInfo = naviPathList.get(i);
      if (i == naviPathList.size() - 1)
        queryList.add(new JPANavigationFilterQuery(odata, sd, naviInfo.getUriResiource(), parent, em, naviInfo
            .getAssociationPath(), expression));
      else
        queryList.add(new JPANavigationFilterQuery(odata, sd, naviInfo.getUriResiource(), parent, em, naviInfo
            .getAssociationPath()));
      parent = queryList.get(queryList.size() - 1);
    }
    // 3. Create select statements
    Subquery<?> childQuery = null;
    for (int i = queryList.size() - 1; i >= 0; i--) {
      childQuery = queryList.get(i).getSubQueryExists(childQuery);
    }
    return childQuery;
  }

  private class SubMember implements UriInfoResource {
    final private JPAMemberOperator parentMember;

    public SubMember(JPAMemberOperator member) {
      super();
      this.parentMember = member;
    }

    @Override
    public List<CustomQueryOption> getCustomQueryOptions() {
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
    public CountOption getCountOption() {
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
      List<UriResource> result = new ArrayList<UriResource>();
      List<UriResource> source = parentMember.getMember().getUriResourceParts();
      for (int i = source.size() - 1; i > 0; i--) {
        if (source.get(i).getKind() == UriResourceKind.navigationProperty || source.get(i)
            .getKind() == UriResourceKind.entitySet) {
          break;
        }
        result.add(0, source.get(i));
      }
      return result;
    }

    @Override
    public String getValueForAlias(String alias) {
      return null;
    }

  }

  @Override
  public Enum<?> getOperator() {
    return null;
  }
}
