package org.apache.olingo.jpa.processor.core.filter;

import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;

import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAAttribute;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPADescriptionAttribute;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAElement;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import org.apache.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import org.apache.olingo.jpa.processor.core.exception.ODataJPAFilterException;
import org.apache.olingo.jpa.processor.core.query.JPAAbstractQuery;
import org.apache.olingo.jpa.processor.core.query.Util;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.queryoption.expression.Member;

public class JPAMemberOperator implements JPAOperator {
  private final Member member;
  private final JPAEntityType jpaEntityType;
  private final Root<?> root;

  JPAMemberOperator(final JPAEntityType jpaEntityType, final JPAAbstractQuery parent,
      final Member member) {
    super();
    this.member = member;
    this.jpaEntityType = jpaEntityType;
    this.root = parent.getRoot();
  }

  public JPAAttribute determineAttribute() throws ODataApplicationException {
    return determineAttributePath().getLeaf();
  }

  @Override
  public Path<?> get() throws ODataApplicationException {
    final JPAPath selectItemPath = determineAttributePath();
    return determineCriteriaPath(selectItemPath);
  }

  public Member getMember() {// UriInfoResource getMember() {
    return member; // .getResourcePath();
  }

  private JPAPath determineAttributePath() throws ODataApplicationException {
    final String path = Util.determineProptertyNavigationPath(member.getResourcePath().getUriResourceParts());
    JPAPath selectItemPath = null;
    try {
      selectItemPath = jpaEntityType.getPath(path);
    } catch (ODataJPAModelException e) {
      throw new ODataJPAFilterException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
    }
    return selectItemPath;
  }

  private Path<?> determineCriteriaPath(final JPAPath selectItemPath) {
    Path<?> p = root;
    for (final JPAElement jpaPathElement : selectItemPath.getPath()) {
      if (jpaPathElement instanceof JPADescriptionAttribute) {
        // TODO handle description fields
//        Join<?, ?> join = (Join<?, ?>) joinTables.get(jpaPathElement.getInternalName());
//        p = join.get(((JPADescriptionAttribute) jpaPathElement).getDescriptionAttribute().getInternalName());
      } else
        p = p.get(jpaPathElement.getInternalName());
    }
    return p;
  }
}
