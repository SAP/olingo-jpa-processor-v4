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

public class SchemaReader {
  private final JacksonXmlModule module;
  private final XmlMapper xmlMapper;

  public SchemaReader() {
    super();
    module = new JacksonXmlModule();
    module.setDefaultUseWrapper(false);
    xmlMapper = new XmlMapper(module);

  }

  public Map<String, ? extends CsdlSchema> getSchemas(String path) throws IOException {
    return convertEDMX(readFromResource(path));
  }

  public Map<String, ? extends CsdlSchema> getSchemas(URI uri) throws IOException {
    return convertEDMX(readFromURI(uri));
  }

  public Edmx readFromResource(final String path) throws IOException {

    byte[] b = loadXML(path);
    return xmlMapper.readValue(new String(b), Edmx.class);
  }

  public Edmx readFromURI(final URI uri) throws IOException {

    return xmlMapper.readValue(uri.toURL(), Edmx.class);
  }

  private byte[] loadXML(String path) {

    InputStream i = null;
    byte[] image = null;
    URL u = this.getClass().getClassLoader().getResource(path);
    try {
      i = u.openStream();
      image = new byte[i.available()];
      i.read(image);
    } catch (IOException e1) {
      e1.printStackTrace();
    } finally {
      try {
        i.close();
        return image;
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return null;
  }

  private Map<String, ? extends CsdlSchema> convertEDMX(Edmx edmx) {

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
