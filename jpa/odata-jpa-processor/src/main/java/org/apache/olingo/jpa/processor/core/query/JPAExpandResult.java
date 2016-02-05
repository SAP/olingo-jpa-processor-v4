package org.apache.olingo.jpa.processor.core.query;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.persistence.Tuple;

import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.jpa.metadata.core.edm.mapper.impl.JPAAssociationPath;
import org.apache.olingo.server.api.ODataApplicationException;

public final class JPAExpandResult {

  private final HashMap<JPAAssociationPath, JPAExpandResult> childrenResult;
  private final Map<String, List<Tuple>> result;

  public JPAExpandResult(Map<String, List<Tuple>> result) {
    super();
    childrenResult = new HashMap<JPAAssociationPath, JPAExpandResult>();
    this.result = result;
  }

  public boolean hasChildren() {
    return !childrenResult.isEmpty();
  }

  public Map<String, List<Tuple>> getResult() {
    return result;
  }

  public void putChild(JPAAssociationPath assosiation, JPAExpandResult childResult) throws ODataApplicationException {
    if (childrenResult.get(assosiation) != null)
      throw new ODataApplicationException("Double execution of $expand", HttpStatusCode.INTERNAL_SERVER_ERROR.ordinal(),
          Locale.ENGLISH);
    childrenResult.put(assosiation, childResult);
  }

  public void putChildren(Map<JPAAssociationPath, JPAExpandResult> childResults) throws ODataApplicationException {
    for (JPAAssociationPath child : childResults.keySet()) {
      if (childrenResult.get(child) != null)
        throw new ODataApplicationException("Double execution of $expand", HttpStatusCode.INTERNAL_SERVER_ERROR
            .ordinal(),
            Locale.ENGLISH);
    }
    childrenResult.putAll(childResults);
  }

  public List<Tuple> get(String key) {
    return result.get(key);
  }

  public HashMap<JPAAssociationPath, JPAExpandResult> getChildren() {
    return childrenResult;
  }
}
