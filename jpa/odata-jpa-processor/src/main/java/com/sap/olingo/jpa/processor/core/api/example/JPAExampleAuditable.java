package com.sap.olingo.jpa.processor.core.api.example;

import java.time.LocalDateTime;

public interface JPAExampleAuditable {

  void setCreatedBy(final String user);

  void setCreatedAt(final LocalDateTime dateTime);

  void setUpdatedBy(final String user);

  void setUpdatedAt(final LocalDateTime dateTime);
}
