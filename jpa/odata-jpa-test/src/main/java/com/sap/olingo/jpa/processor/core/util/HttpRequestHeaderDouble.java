package com.sap.olingo.jpa.processor.core.util;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class HttpRequestHeaderDouble {
  private final HashMap<String, List<String>> headers;

  public HttpRequestHeaderDouble() {
    super();
    headers = new HashMap<>();
    List<String> headerValue;
    headerValue = new ArrayList<>();
    headerValue.add("localhost:8090");
    headers.put("host", headerValue);

    headerValue = new ArrayList<>();
    headerValue.add("keep-alive");
    headers.put("connection", headerValue);

    headerValue = new ArrayList<>();
    headerValue.add("max-age=0");
    headers.put("cache-control", headerValue);

    headerValue = new ArrayList<>();
    headerValue.add("text/html,application/json,application/xml;q=0.9,image/webp,*/*;q=0.8");
    headers.put("accept", headerValue);

    headerValue = new ArrayList<>();
    headerValue.add("gzip, deflate, sdch");
    headers.put("accept-encoding", headerValue);

    headerValue = new ArrayList<>();
    headerValue.add("de-DE,de;q=0.8,en-US;q=0.6,en;q=0.4");
    headers.put("accept-language", headerValue);

  }

  public Enumeration<String> get(final String headerName) {
    return new HeaderItem(headers.get(headerName));
  }

  public Enumeration<String> getEnumerator() {
    return new HeaderEnumerator(headers.keySet());
  }

  public void setBatchRequest() {
    final List<String> headerValue = new ArrayList<>();
    headerValue.add("multipart/mixed;boundary=abc123");
    headers.put("content-type", headerValue);
  }

  public void setHeaders(final Map<String, List<String>> additionalHeaders) {
    if (additionalHeaders != null) {
      for (final Entry<String, List<String>> header : additionalHeaders.entrySet())
        headers.put(header.getKey(), header.getValue());
    }
  }

  static class HeaderEnumerator implements Enumeration<String> { // NOSONAR

    private final Iterator<String> keys;

    HeaderEnumerator(final Set<String> keySet) {
      keys = keySet.iterator();
    }

    @Override
    public boolean hasMoreElements() {
      return keys.hasNext();
    }

    @Override
    public String nextElement() {
      return keys.next();
    }
  }

  static class HeaderItem implements Enumeration<String> {// NOSONAR
    private final Iterator<String> keys;

    public HeaderItem(final List<String> header) {
      keys = header.iterator();
    }

    @Override
    public boolean hasMoreElements() {
      return keys.hasNext();
    }

    @Override
    public String nextElement() {
      return keys.next();
    }

  }
}
