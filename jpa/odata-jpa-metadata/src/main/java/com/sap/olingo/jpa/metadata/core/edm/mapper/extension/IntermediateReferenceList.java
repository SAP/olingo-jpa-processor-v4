package com.sap.olingo.jpa.metadata.core.edm.mapper.extension;

import java.net.URI;
import java.nio.charset.Charset;

import javax.annotation.Nonnull;

import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;

/**
 * A IntermediateReferenceList collects all references to external CSDL documents. The document must be an XML
 * document.<br>
 * See also: <a
 * href="https://docs.oasis-open.org/odata/odata/v4.0/errata03/os/complete/part3-csdl/odata-v4.0-errata03-os-part3-csdl-complete.html#_Toc453752504">Common
 * Schema Definition Language (CSDL) 3.3 Element edmx:Reference</a>
 * @author Oliver Grande
 */
public interface IntermediateReferenceList {
  /**
   * Loads a referenced external CSDL document from the provided uri. The document has to be a XML document.<br>
   * The method is mainly for prototyping and testing, move of content as it is usually not wanted in productive code
   * that the used
   * content changes during runtime or on a restart.
   * @param uri A uniquely identifier of a referenced document, so two references MUST NOT specify the same URI. E.g.
   * "http://docs.oasis-open.org/odata/odata/v4.0/os/vocabularies/Org.OData.Core.V1.xml"
   * @return
   * @throws ODataJPAModelException
   */
  public IntermediateReferenceAccess addReference(@Nonnull final String uri) throws ODataJPAModelException;

  /**
   * Loads a referenced external CSDL document from the file system. The document has to be a XML document. The document
   * is loaded assuming it has been stored using the system default character set.
   * @param uri A uniquely identifier of a referenced document, so two references MUST NOT specify the same URI.
   * @param path Path to the CSDL document
   * @return
   * @throws ODataJPAModelException
   */
  public IntermediateReferenceAccess addReference(@Nonnull final String uri, @Nonnull final String path)
      throws ODataJPAModelException;

  /**
   * Loads a referenced external CSDL document from the file system. The document has to be a XML document.
   * @param uri A uniquely identifier of a referenced document, so two references MUST NOT specify the same URI.
   * @param path Path to the CSDL document
   * @param charset Character set to be used when loading the document.
   * @return
   * @throws ODataJPAModelException
   */
  public IntermediateReferenceAccess addReference(@Nonnull final String uri, @Nonnull final String path,
      @Nonnull final Charset charset) throws ODataJPAModelException;

  public interface IntermediateReferenceAccess {
    public URI getURI();

    public String getPath();

    /**
     * See: <a
     * href="http://docs.oasis-open.org/odata/odata/v4.0/errata03/os/complete/part3-csdl/odata-v4.0-errata03-os-part3-csdl-complete.html#_Toc453752506">3.4
     * Element edmx:Include</a>
     * @param namespace Namespace of a schema defined in the referenced CSDL document to be included. The same namespace
     * MUST NOT be included more than once.
     * @param alias Alias for the given namespace. The alias must be unique.
     */
    public void addInclude(@Nonnull final String namespace, final String alias);

    /**
     * See: <a
     * href="http://docs.oasis-open.org/odata/odata/v4.0/errata03/os/complete/part3-csdl/odata-v4.0-errata03-os-part3-csdl-complete.html#_Toc453752506">3.4
     * Element edmx:Include</a>
     * @param namespace Namespace of a schema defined in the referenced CSDL document to be included. The same namespace
     * MUST NOT be included more than once.
     */
    public void addInclude(@Nonnull final String namespace);

    /**
     * See: <a
     * href="http://docs.oasis-open.org/odata/odata/v4.0/errata03/os/complete/part3-csdl/odata-v4.0-errata03-os-part3-csdl-complete.html#_Toc453752509">3.5
     * Element edmx:IncludeAnnotations</a>
     * @param termNamespace
     * @throws ODataJPAModelException
     */
    public void addIncludeAnnotation(@Nonnull final String termNamespace) throws ODataJPAModelException;

    /**
     * See: <a
     * href="http://docs.oasis-open.org/odata/odata/v4.0/errata03/os/complete/part3-csdl/odata-v4.0-errata03-os-part3-csdl-complete.html#_Toc453752509">3.5
     * Element edmx:IncludeAnnotations</a>
     * @param termNamespace
     * @param qualifier
     * @param targetNamespace
     * @throws ODataJPAModelException
     */
    public void addIncludeAnnotation(@Nonnull final String termNamespace, final String qualifier,
        final String targetNamespace) throws ODataJPAModelException;
  }
}