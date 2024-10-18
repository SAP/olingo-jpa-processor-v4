package com.sap.olingo.jpa.processor.core.query;

import static org.mockito.Mockito.mock;

import org.apache.olingo.server.api.uri.queryoption.ExpandItem;
import org.junit.jupiter.api.BeforeEach;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;

class JPAExpandItemWrapperTest extends JPAExpandItemPageableTest {
  private JPAExpandItemWrapper cut;

  @BeforeEach
  void setup() {
    et = mock(JPAEntityType.class);
    expandItem = mock(ExpandItem.class);
    cut = new JPAExpandItemWrapper(expandItem, et);
  }

  @Override
  JPAExpandItemPageable getCut() {
    return cut;
  }
}
