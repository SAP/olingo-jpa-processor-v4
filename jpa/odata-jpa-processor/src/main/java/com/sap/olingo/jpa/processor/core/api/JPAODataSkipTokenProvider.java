package com.sap.olingo.jpa.processor.core.api;

import java.util.List;

public interface JPAODataSkipTokenProvider {

  String get(final List<JPAODataPageExpandInfo> foreignKeyStack);
}
