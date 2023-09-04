package com.sap.olingo.jpa.processor.core.filter;

import static com.sap.olingo.jpa.processor.core.exception.ODataJPAFilterException.MessageKeys.NOT_ALLOWED_MEMBER;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Path;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.queryoption.expression.Member;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPACollectionAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPADescriptionAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAElement;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAFilterException;

public class JPAMemberOperator implements JPAOperator {

  private static final Log LOGGER = LogFactory.getLog(JPAMemberOperator.class);
  private final Member member;
  private final From<?, ?> root;
  private final JPAAssociationPath association;
  private final JPAPath attributePath;

  JPAMemberOperator(final From<?, ?> parent, final Member member, final JPAAssociationPath association,
      final List<String> list, final JPAPath attributePath) throws ODataApplicationException {

    super();
    this.member = member;
    this.root = parent;
    this.association = association;
    this.attributePath = attributePath;
    checkGroup(list);
  }

  public JPAAttribute determineAttribute() {
    return attributePath == null ? null : attributePath.getLeaf();
  }

  @Override
  public Path<?> get() throws ODataApplicationException {
    return attributePath == null ? null : determineCriteriaPath(attributePath);
  }

  public Member getMember() {
    return member;
  }

  @Override
  public String getName() {
    return member.toString();
  }

  private Path<?> determineCriteriaPath(final JPAPath selectItemPath) throws ODataJPAFilterException {
    Path<?> path = root;
    for (final JPAElement jpaPathElement : selectItemPath.getPath()) {
      if (jpaPathElement instanceof JPADescriptionAttribute) {
        path = determineDescriptionCriteriaPath(selectItemPath, path, jpaPathElement);
      } else if (jpaPathElement instanceof JPACollectionAttribute) {
        if (!((JPACollectionAttribute) jpaPathElement).isComplex()) try {
          path = path.get(((JPACollectionAttribute) jpaPathElement).getTargetAttribute().getInternalName());
        } catch (final ODataJPAModelException e) {
          throw new ODataJPAFilterException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
        }
      } else {
        path = path.get(jpaPathElement.getInternalName());
      }
    }
    return path;
  }

  private Path<?> determineDescriptionCriteriaPath(final JPAPath selectItemPath, Path<?> path,
      final JPAElement jpaPathElement) {

    final Set<?> allJoins = root.getJoins();
    final Iterator<?> iterator = allJoins.iterator();
    while (iterator.hasNext()) {
      Join<?, ?> join = (Join<?, ?>) iterator.next();
      if (join.getAlias() != null && join.getAlias().equals(selectItemPath.getAlias())) {
        final Set<?> subJoins = join.getJoins();
        for (final Object sub : subJoins) {
          // e.g. "Organizations?$filter=Address/RegionName eq 'Kalifornien'
          // see createFromClause in JPAExecutableQuery
          if (((Join<?, ?>) sub).getAlias() != null &&
              ((Join<?, ?>) sub).getAlias().equals(jpaPathElement.getExternalName())) {
            join = (Join<?, ?>) sub;
          }
        }
        path = join.get(((JPADescriptionAttribute) jpaPathElement).getDescriptionAttribute().getInternalName());
        break;
      }
    }
    return path;
  }

  private void checkGroup(final List<String> groups) throws ODataJPAFilterException {
    JPAPath orgPath = attributePath;
    if (association != null && association.getPathAsString() != null && attributePath != null) {
      final JPAAttribute st = ((JPAAttribute) this.association.getPath().get(0));
      if (st.isComplex()) {
        try {
          orgPath = st.getStructuredType().getPath(attributePath.getLeaf().getExternalName());
        } catch (final ODataJPAModelException e) {
          // Ignore exception and use path
          LOGGER.debug("Exception occurred -> use path: " + e.getMessage());
        }
      }
    }
    if (orgPath != null && !orgPath.isPartOfGroups(groups))
      throw new ODataJPAFilterException(NOT_ALLOWED_MEMBER, HttpStatusCode.FORBIDDEN, orgPath.getAlias());
  }
}
