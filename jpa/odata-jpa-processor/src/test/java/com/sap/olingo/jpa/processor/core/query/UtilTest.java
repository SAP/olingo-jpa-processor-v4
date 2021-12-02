package com.sap.olingo.jpa.processor.core.query;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.edm.EdmType;
import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriInfoResource;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.apache.olingo.server.api.uri.UriResourceKind;
import org.apache.olingo.server.api.uri.UriResourceNavigation;
import org.apache.olingo.server.api.uri.UriResourceSingleton;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.processor.core.testmodel.CurrentUser;
import com.sap.olingo.jpa.processor.core.testmodel.Singleton;
import com.sap.olingo.jpa.processor.core.util.TestBase;

class UtilTest extends TestBase {
  private UriInfoResource uriInfo;
  private List<UriResource> resourceParts;

  @BeforeEach
  void setUp() throws ODataException {
    getHelper();
    uriInfo = mock(UriInfoResource.class);
    resourceParts = new ArrayList<>();
  }

  @Test
  void testDetermineNavigationPathReturnsSingleton() throws ODataApplicationException {
    final UriResourceSingleton resourcePart = mock(UriResourceSingleton.class);
    final EdmType edmType = mock(EdmType.class);
    when(resourcePart.getKind()).thenReturn(UriResourceKind.singleton);
    when(resourcePart.getType()).thenReturn(edmType);
    when(edmType.getNamespace()).thenReturn(PUNIT_NAME);
    when(edmType.getName()).thenReturn(CurrentUser.class.getSimpleName());

    resourceParts.add(resourcePart);
    final List<JPANavigationPropertyInfo> act = Util.determineNavigationPath(helper.sd, resourceParts, uriInfo);
    assertNotNull(act);
    assertEquals(1, act.size());
  }

  @Test
  void testDetermineAssociationPathCreatesPathForSingleton() throws ODataApplicationException {
    final StringBuilder associationName = new StringBuilder("Details");
    final UriResourceSingleton source = mock(UriResourceSingleton.class);
    final EdmEntityType singletonType = mock(EdmEntityType.class);
    when(singletonType.getNamespace()).thenReturn(PUNIT_NAME);
    when(singletonType.getName()).thenReturn(Singleton.class.getSimpleName());
    when(source.getType()).thenReturn(singletonType);

    final JPAAssociationPath act = Util.determineAssociationPath(helper.sd, source, associationName);
    assertNotNull(act);
  }

  @Test
  void testDetermineKeyPredicatesEntitySet() throws ODataApplicationException {

    final UriResourceEntitySet es = mock(UriResourceEntitySet.class);
    final UriParameter keyElement = mock(UriParameter.class);
    final List<UriParameter> keys = Collections.singletonList(keyElement);
    when(es.getKeyPredicates()).thenReturn(keys);
    final List<UriParameter> act = Util.determineKeyPredicates(es);
    assertEquals(keys, act);
  }

  @Test
  void testDetermineKeyPredicatesNavigation() throws ODataApplicationException {

    final UriResourceNavigation navigation = mock(UriResourceNavigation.class);
    final UriParameter keyElement = mock(UriParameter.class);
    final List<UriParameter> keys = Collections.singletonList(keyElement);
    when(navigation.getKeyPredicates()).thenReturn(keys);
    final List<UriParameter> act = Util.determineKeyPredicates(navigation);
    assertEquals(keys, act);
  }

  @Test
  void testDetermineKeySingleton() throws ODataApplicationException {

    final UriResourceSingleton singleton = mock(UriResourceSingleton.class);
    final List<UriParameter> act = Util.determineKeyPredicates(singleton);
    assertTrue(act.isEmpty());
  }
}
