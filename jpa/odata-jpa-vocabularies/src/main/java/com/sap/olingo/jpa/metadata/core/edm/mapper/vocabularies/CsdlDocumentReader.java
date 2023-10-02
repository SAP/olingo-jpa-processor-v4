package com.sap.olingo.jpa.metadata.core.edm.mapper.vocabularies;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;

import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.sap.olingo.jpa.metadata.core.edm.mapper.vocabularies.ODataJPAVocabulariesException.MessageKeys;

/**
 *
 * @author Oliver Grande
 * @since 15.12.2019
 * @version 0.3.8
 *
 */
public class CsdlDocumentReader {
  final JacksonXmlModule module;
  final XmlMapper xmlMapper;

  /**
   *
   */
  public CsdlDocumentReader() {
    super();
    module = new JacksonXmlModule();
    module.setDefaultUseWrapper(false);
    xmlMapper = new XmlMapper(module);
  }

//vocabularies
  String loadXML(@Nonnull final String path, @Nonnull final Charset charset) throws IOException,
      ODataJPAVocabulariesException {

    final Optional<InputStream> reader = Optional.ofNullable(this.getClass().getClassLoader()
        .getResourceAsStream(path));
    final StringBuilder content = new StringBuilder();

    try (
        final InputStreamReader input = new InputStreamReader(reader.orElseThrow(
            () -> new ODataJPAVocabulariesException(MessageKeys.FILE_NOT_FOUND, path)), charset);
        final BufferedReader br = new BufferedReader(input)) {
      String line;
      while ((line = br.readLine()) != null) {
        content.append(line);
      }
      return content.toString();
    }
  }

  /**
   *
   * @param path
   * @param charset
   * @return
   * @throws IOException
   * @throws ODataJPAVocabulariesException
   * @throws ODataJPAModelException
   * @throws NullPointerException
   */
  public CsdlDocument readFromResource(@Nonnull final String path, @Nonnull final Charset charset)
      throws IOException, ODataJPAVocabulariesException {

    if (Objects.requireNonNull(path).isEmpty())
      return null;
    return xmlMapper.readValue(loadXML(path, Objects.requireNonNull(charset)),
        CsdlDocument.class);
  }

  /**
   *
   * @param uri
   * @return
   * @throws IOException
   * @throws NullPointerException
   */
  public CsdlDocument readFromURI(@Nonnull final URI uri) throws IOException {

    return xmlMapper.readValue(Objects.requireNonNull(uri).toURL(), CsdlDocument.class);
  }
}