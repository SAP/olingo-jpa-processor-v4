package com.sap.olingo.jpa.processor.core.testmodel;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmIgnore;

@EdmIgnore
@Entity
@Table(schema = "\"OLINGO\"", name = "\"Pages\"")
public class Pages {

  @Id
  @Column(name = "\"token\"")
  private Integer token;

  @Column(name = "\"skip\"")
  private Integer skip;

  @Column(name = "\"top\"")
  private Integer top;

  @Column(name = "\"last\"")
  private Integer last;

  @Column(name = "\"baseUri\"")
  private String baseUri;

  @Column(name = "\"oDataPath\"")
  private String oDataPath;

  @Column(name = "\"queryPath\"")
  private String queryPath;

  @Column(name = "\"fragments\"")
  private String fragments;

  public Pages() {
    // Needed for JPA
  }

  public Pages(final Integer token, final Integer skip, final Integer top, final Integer last, final String baseUri,
      final String oDataPath, final String queryPath, final String fragments) {
    super();
    this.token = token;
    this.skip = skip;
    this.top = top;
    this.last = last;
    this.baseUri = baseUri;
    this.oDataPath = oDataPath;
    this.queryPath = queryPath;
    this.fragments = fragments;
  }

  public Pages(final Pages previousPage, final int skip, final Integer token) {
    super();
    this.token = token;
    this.skip = skip;
    this.top = previousPage.top;
    this.last = previousPage.last;
    this.baseUri = previousPage.baseUri;
    this.oDataPath = previousPage.oDataPath;
    this.queryPath = previousPage.queryPath;
    this.fragments = previousPage.fragments;
  }

  public Integer getToken() {
    return token;
  }

  public void setToken(final Integer token) {
    this.token = token;
  }

  public Integer getSkip() {
    return skip;
  }

  public void setSkip(final Integer skip) {
    this.skip = skip;
  }

  public Integer getTop() {
    return top;
  }

  public void setTop(final Integer top) {
    this.top = top;
  }

  public String getBaseUri() {
    return baseUri;
  }

  public void setBaseUri(final String baseUri) {
    this.baseUri = baseUri;
  }

  public String getODataPath() {
    return oDataPath;
  }

  public void setODataPath(final String oDataPath) {
    this.oDataPath = oDataPath;
  }

  public String getQueryPath() {
    return queryPath;
  }

  public void setQueryPath(final String queryPath) {
    this.queryPath = queryPath;
  }

  public String getFragments() {
    return fragments;
  }

  public void setFragments(final String fragments) {
    this.fragments = fragments;
  }

  public Integer getLast() {
    return last;
  }

  public void setLast(final Integer last) {
    this.last = last;
  }
}
