/**
 * 
 */
package com.sap.olingo.jpa.metadata.core.edm.mapper.annotation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;

import org.apache.olingo.commons.api.edm.provider.CsdlAbstractEdmItem;

import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException.MessageKeys;

/**
 * @author Oliver Grande
 * Created: 15.12.2019
 *
 */
abstract class AbstractVocabularyReader {
  final JacksonXmlModule module;
  final XmlMapper xmlMapper;

  /**
   * 
   */
  AbstractVocabularyReader() {
    super();
    module = new JacksonXmlModule();
    module.setDefaultUseWrapper(false);
    xmlMapper = new XmlMapper(module);
  }

  protected String loadXML(@Nonnull final String path) throws IOException, ODataJPAModelException {
    final Optional<URL> url = Optional.ofNullable(this.getClass().getClassLoader().getResource(path));
    final File file = new File(url.orElseThrow(
        () -> new ODataJPAModelException(MessageKeys.FILE_NOT_FOUND, path)).getFile());
    final StringBuilder content = new StringBuilder();
    try (final FileReader reader = new FileReader(file);
        final BufferedReader br = new BufferedReader(reader)) {
      String line;
      while ((line = br.readLine()) != null) {
        content.append(line);
      }
    }
    return content.toString();
  }

  protected abstract <T extends CsdlAbstractEdmItem> Map<String, T> convertEDMX(Edmx edmx);

  public Edmx readFromResource(@Nonnull final String path) throws IOException, ODataJPAModelException {
    return xmlMapper.readValue(loadXML(Objects.requireNonNull(path)), Edmx.class);
  }

  public Edmx readFromURI(@Nonnull final URI uri) throws IOException {
    return xmlMapper.readValue(uri.toURL(), Edmx.class);

  }

}