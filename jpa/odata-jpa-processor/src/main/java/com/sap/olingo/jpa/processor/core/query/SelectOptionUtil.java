package com.sap.olingo.jpa.processor.core.query;

import java.util.stream.Collectors;

import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.uri.queryoption.SelectItem;
import org.apache.olingo.server.api.uri.queryoption.SelectOption;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAStructuredType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAQueryException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAQueryException.MessageKeys;

/**
 * 
 * 
 * @author Oliver Grande
 * Created: 01.11.2019
 *
 */
class SelectOptionUtil {

  private SelectOptionUtil() {
    super();
  }

  public static JPAPath selectItemAsPath(final JPAStructuredType jpaEntity, final String pathPrefix,
      final SelectItem sItem) throws ODataJPAQueryException {

    try {
      final String pathItem = sItem.getResourcePath().getUriResourceParts().stream().map(path -> (path
          .getSegmentValue())).collect(Collectors.joining(JPAPath.PATH_SEPARATOR));
      JPAPath selectItemPath;

      selectItemPath = jpaEntity.getPath(pathPrefix.isEmpty() ? pathItem : pathPrefix
          + JPAPath.PATH_SEPARATOR + pathItem);
      if (selectItemPath == null)
        throw new ODataJPAQueryException(MessageKeys.QUERY_PREPARATION_INVALID_SELECTION_PATH,
            HttpStatusCode.BAD_REQUEST);
      return selectItemPath;
    } catch (ODataJPAModelException e) {
      throw new ODataJPAQueryException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
    }
  }

  public static boolean selectAll(final SelectOption select) {
    return select == null || select.getSelectItems() == null || select.getSelectItems().isEmpty() || select
        .getSelectItems().get(0).isStar();
  }
}