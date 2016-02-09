package org.apache.olingo.jpa.processor.core.filter;

import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;

import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPADescriptionAttribute;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAElement;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import org.apache.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import org.apache.olingo.jpa.processor.core.query.Util;
import org.apache.olingo.server.api.uri.UriInfoResource;

public class JPAMemberOperator implements JPAOperator {
  private final UriInfoResource member;
  private final JPAEntityType jpaEntityType;
  private final Root<?> root;

  public JPAMemberOperator(JPAEntityType jpaEntityType, Root<?> root, UriInfoResource member) {
    super();
    this.member = member;
    this.jpaEntityType = jpaEntityType;
    this.root = root;
  }

  @Override
  public Path<?> get() {
    JPAPath selectItemPath = determineAttributePath();
    return determineCriteriaPath(selectItemPath);
  }

  private Path<?> determineCriteriaPath(JPAPath selectItemPath) {
    Path<?> p = root;
    for (JPAElement jpaPathElement : selectItemPath.getPath()) {
      if (jpaPathElement instanceof JPADescriptionAttribute) {
        // TODO handle description fields
//        Join<?, ?> join = (Join<?, ?>) joinTables.get(jpaPathElement.getInternalName());
//        p = join.get(((JPADescriptionAttribute) jpaPathElement).getDescriptionAttribute().getInternalName());
      } else
        p = p.get(jpaPathElement.getInternalName());
    }
    return p;
  }

  public JPAPath determineAttributePath() {
    String path = Util.determineProptertyNavigationPath(member.getUriResourceParts());
    JPAPath selectItemPath = null;
    try {
      selectItemPath = jpaEntityType.getPath(path);
    } catch (ODataJPAModelException e) {
      // TODO error handling
      e.printStackTrace();
    }
    return selectItemPath;
  }
}
