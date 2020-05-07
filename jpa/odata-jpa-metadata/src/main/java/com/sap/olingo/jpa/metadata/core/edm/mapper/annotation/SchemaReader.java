package com.sap.olingo.jpa.metadata.core.edm.mapper.annotation;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.olingo.commons.api.edm.provider.CsdlSchema;

import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;

public class SchemaReader extends AbstractVocabularyReader {

  public SchemaReader() {
    super();
  }

  public Map<String, CsdlSchema> getSchemas(final String path) throws IOException, ODataJPAModelException {
    return path == null || path.isEmpty() ? Collections.emptyMap() : convertEDMX(readFromResource(path));
  }

  public Map<String, CsdlSchema> getSchemas(final URI uri) throws IOException {
    return uri == null ? Collections.emptyMap() : convertEDMX(readFromURI(uri));
  }

  @SuppressWarnings("unchecked")
  @Override
  protected Map<String, CsdlSchema> convertEDMX(Edmx edmx) {

    if (edmx != null && edmx.getDataService() != null) {

      List<Schema> schemas = edmx.getDataService().getSchemas();
      Map<String, CsdlSchema> edmSchemas = new HashMap<>(schemas.size());
      for (Schema schema : schemas) {
        String namespace = schema.getNamespace();
        edmSchemas.put(namespace, schema.asCsdlSchema());
      }
      return edmSchemas;
    }
    return null;
  }
}
