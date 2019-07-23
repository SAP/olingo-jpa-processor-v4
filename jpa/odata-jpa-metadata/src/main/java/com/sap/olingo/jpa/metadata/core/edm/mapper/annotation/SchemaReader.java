package com.sap.olingo.jpa.metadata.core.edm.mapper.annotation;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.olingo.commons.api.edm.provider.CsdlSchema;

import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException.MessageKeys;

public class SchemaReader {
  private final JacksonXmlModule module;
  private final XmlMapper xmlMapper;

  public SchemaReader() {
    super();
    module = new JacksonXmlModule();
    module.setDefaultUseWrapper(false);
    xmlMapper = new XmlMapper(module);

  }

  public Map<String, CsdlSchema> getSchemas(String path) throws IOException, ODataJPAModelException {
    return convertEDMX(readFromResource(path));
  }

  public Map<String, CsdlSchema> getSchemas(URI uri) throws IOException {
    return convertEDMX(readFromURI(uri));
  }

  public Edmx readFromResource(final String path) throws IOException, ODataJPAModelException {

    if (path == null || path.isEmpty()) {
      return null;
    }
    byte[] b = loadXML(path);
    return xmlMapper.readValue(new String(b), Edmx.class);
  }

  public Edmx readFromURI(final URI uri) throws IOException {
    if (uri == null) {
      return null;
    }
    return xmlMapper.readValue(uri.toURL(), Edmx.class);
  }

  private byte[] loadXML(String path) throws IOException, ODataJPAModelException {

    InputStream i = null;
    byte[] image = null;
    URL u = this.getClass().getClassLoader().getResource(path);
    if (u == null)
      throw new ODataJPAModelException(MessageKeys.FILE_NOT_FOUND, path);
    try {
      i = u.openStream();
      image = new byte[i.available()];
      i.read(image); // NOSONAR
    } finally {
      if (i != null)
        i.close();
    }
    return image;
  }

  private Map<String, CsdlSchema> convertEDMX(Edmx edmx) {

    if (edmx != null && edmx.getDataService() != null) {

      Schema[] schemas = edmx.getDataService().getSchemas();
      Map<String, CsdlSchema> edmSchemas = new HashMap<>(schemas.length);
      for (Schema schema : schemas) {
        String namespace = schema.getNamespace();
        edmSchemas.put(namespace, schema.asCsdlSchema());
      }
      return edmSchemas;
    }
    return null;
  }
}
