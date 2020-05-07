package com.sap.olingo.jpa.metadata.core.edm.mapper.annotation;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.olingo.commons.api.edm.provider.CsdlTerm;

import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;

public class TermReader extends AbstractVocabularyReader {

  public TermReader() {
    super();
  }

  public Map<String, Map<String, CsdlTerm>> getTerms(String path) throws IOException, ODataJPAModelException {
    return convertEDMX(readFromResource(path));
  }

  public Map<String, Map<String, CsdlTerm>> getTerms(URI uri) throws IOException {
    return convertEDMX(readFromURI(uri));
  }

  @SuppressWarnings("unchecked")
  @Override
  protected Map<String, Map<String, CsdlTerm>> convertEDMX(Edmx edmx) {
    if (edmx != null && edmx.getDataService() != null) {
      List<Schema> schemas = edmx.getDataService().getSchemas();
      Map<String, Map<String, CsdlTerm>> edmSchemas = new HashMap<>(schemas.size());

      for (Schema schema : schemas) {
        String namespace = schema.getNamespace();
        Map<String, CsdlTerm> terms = new HashMap<>();
        for (CsdlTerm t : schema.getTerms()) {
          terms.put(t.getName(), t);
        }
        edmSchemas.put(namespace, terms);
      }
      return edmSchemas;
    }
    return null;
  }
}
