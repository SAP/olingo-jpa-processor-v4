package com.sap.olingo.jpa.metadata.core.edm.mapper.annotation;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.olingo.commons.api.edm.provider.CsdlTerm;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

public class TermReader {
  final private JacksonXmlModule module;
  final private XmlMapper xmlMapper;

  public TermReader() {
    super();
    module = new JacksonXmlModule();
    module.setDefaultUseWrapper(false);
    xmlMapper = new XmlMapper(module);

  }

  public Map<String, Map<String, CsdlTerm>> getTerms(String path) throws JsonParseException, JsonMappingException,
      IOException {
    return convertEDMX(readFromResource(path));
  }

  public Map<String, Map<String, CsdlTerm>> getTerms(URI uri) throws JsonParseException, JsonMappingException,
      MalformedURLException, IOException {
    return convertEDMX(readFromURI(uri));
  }

  public Edmx readFromResource(final String path) throws JsonParseException, JsonMappingException, IOException {
    byte[] b = loadXML(path);
    return xmlMapper.readValue(new String(b), Edmx.class);
  }

  public Edmx readFromURI(final URI uri) throws JsonParseException, JsonMappingException, MalformedURLException,
      IOException {
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

  private Map<String, Map<String, CsdlTerm>> convertEDMX(Edmx edmx) {
    if (edmx != null && edmx.getDataService() != null) {
      Schema[] schemas = edmx.getDataService().getSchemas();
      Map<String, Map<String, CsdlTerm>> edmSchemas = new HashMap<String, Map<String, CsdlTerm>>(schemas.length);

      for (Schema schema : schemas) {
        String namespace = schema.getNamespace();
        Map<String, CsdlTerm> terms = new HashMap<String, CsdlTerm>();
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
