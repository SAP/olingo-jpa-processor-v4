package com.sap.olingo.jpa.processor.core.query;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.persistence.Tuple;

import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriInfoResource;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.apache.olingo.server.api.uri.queryoption.ExpandOption;
import org.apache.olingo.server.api.uri.queryoption.SkipOption;
import org.apache.olingo.server.api.uri.queryoption.TopOption;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.api.JPAODataPage;
import com.sap.olingo.jpa.processor.core.api.JPAODataRequestContextAccess;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessException;
import com.sap.olingo.jpa.processor.core.util.TestBase;
import com.sap.olingo.jpa.processor.core.util.TestHelper;
import com.sap.olingo.jpa.processor.core.util.TupleDouble;

class JPAExpandQueryResultTest extends TestBase {
  private JPAExpandQueryResult cut;
  private UriInfoResource uriInfo;
  private TopOption top;
  private SkipOption skip;
  private ExpandOption expand;
  private JPAODataRequestContextAccess requestContext;
  private TestHelper helper;
  private final HashMap<String, List<Tuple>> queryResult = new HashMap<>(1);
  private final List<Tuple> tuples = new ArrayList<>();
  private JPAEntityType et;
  private List<JPANavigationPropertyInfo> hops;
  private JPAODataPage page;

  @BeforeEach
  void setup() throws ODataException {
    helper = new TestHelper(emf, PUNIT_NAME);
    final UriResourceEntitySet uriEts = mock(UriResourceEntitySet.class);
    final JPANavigationPropertyInfo hop0 = new JPANavigationPropertyInfo(helper.sd, uriEts, null, null);
    final JPANavigationPropertyInfo hop1 = new JPANavigationPropertyInfo(helper.sd, uriEts, helper.getJPAEntityType(
        "Organizations").getAssociationPath("Roles"), null);

    hops = Arrays.asList(hop0, hop1);
    et = helper.getJPAEntityType("Organizations");
    uriInfo = mock(UriInfoResource.class);
    requestContext = mock(JPAODataRequestContextAccess.class);
    top = mock(TopOption.class);
    skip = mock(SkipOption.class);
    expand = mock(ExpandOption.class);
    page = new JPAODataPage(null, 0, Integer.MAX_VALUE, hop1);
    when(requestContext.getUriInfo()).thenReturn(uriInfo);
    queryResult.put("root", tuples);
  }

  @Test
  void checkGetKeyBoundaryEmptyBoundaryNoTopSkipPageEmpty() throws ODataJPAModelException, ODataJPAProcessException {

    cut = new JPAExpandQueryResult(queryResult, null, helper.getJPAEntityType("Organizations"),
        Collections.emptyList());
    final Optional<JPAKeyBoundary> act = cut.getKeyBoundary(requestContext, hops, page);
    assertFalse(act.isPresent());
  }

  @Test
  void checkGetKeyBoundaryEmptyBoundaryExpandWithoutTopSkip() throws ODataJPAModelException,
      ODataJPAProcessException {

    cut = new JPAExpandQueryResult(queryResult, null, helper.getJPAEntityType("AdministrativeDivisionDescriptions"),
        Collections.emptyList());
    when(uriInfo.getExpandOption()).thenReturn(expand);
    final Optional<JPAKeyBoundary> act = cut.getKeyBoundary(requestContext, hops, page);
    assertFalse(act.isPresent());
  }

  @Test
  void checkGetKeyBoundaryEmptyBoundaryNoExpand() throws ODataJPAModelException, ODataJPAProcessException {

    final Map<String, Object> key = new HashMap<>(1);
    final TupleDouble tuple = new TupleDouble(key);
    tuples.add(tuple);
    key.put("ID", Integer.valueOf(10));
    cut = new JPAExpandQueryResult(queryResult, null, helper.getJPAEntityType("AdministrativeDivisionDescriptions"),
        Collections.emptyList());
    when(uriInfo.getTopOption()).thenReturn(top);
    when(top.getValue()).thenReturn(2);
    final Optional<JPAKeyBoundary> act = cut.getKeyBoundary(requestContext, hops, page);
    assertFalse(act.isPresent());
  }

  @Test
  void checkGetKeyBoundaryEmptyBoundaryNotComparable() throws ODataJPAModelException,
      NumberFormatException, ODataApplicationException {

    cut = new JPAExpandQueryResult(queryResult, null, helper.getJPAEntityType("AdministrativeDivisionDescriptions"),
        Collections.emptyList());
    final Optional<JPAKeyBoundary> act = cut.getKeyBoundary(requestContext, hops, page);
    assertFalse(act.isPresent());
  }

  @Test
  void checkGetKeyBoundaryEmptyBoundaryNoResult() throws ODataJPAModelException, ODataJPAProcessException {

    queryResult.put("root", Collections.emptyList());

    cut = new JPAExpandQueryResult(queryResult, null, helper.getJPAEntityType("Organizations"),
        Collections.emptyList());
    when(uriInfo.getTopOption()).thenReturn(top);
    when(uriInfo.getExpandOption()).thenReturn(expand);
    when(top.getValue()).thenReturn(2);
    final Optional<JPAKeyBoundary> act = cut.getKeyBoundary(requestContext, hops, page);
    assertFalse(act.isPresent());
  }

  @Test
  void checkGetKeyBoundaryOneResultWithTop() throws ODataJPAModelException, ODataJPAProcessException {

    final Map<String, Object> key = new HashMap<>(1);
    final TupleDouble tuple = new TupleDouble(key);
    tuples.add(tuple);
    key.put("ID", Integer.valueOf(10));
    cut = new JPAExpandQueryResult(queryResult, null, et, Collections.emptyList());
    when(uriInfo.getTopOption()).thenReturn(top);
    when(uriInfo.getExpandOption()).thenReturn(expand);
    when(top.getValue()).thenReturn(2);
    final Optional<JPAKeyBoundary> act = cut.getKeyBoundary(requestContext, hops, page);
    assertTrue(act.isPresent());
    assertEquals(10, act.get().getKeyBoundary().getMin().get(et.getKey().get(0)));
  }

  @Test
  void checkGetKeyBoundaryOneResultWithSkip() throws ODataJPAModelException, ODataJPAProcessException {

    addTuple(12);
    cut = new JPAExpandQueryResult(queryResult, null, et, Collections.emptyList());
    when(uriInfo.getSkipOption()).thenReturn(skip);
    when(uriInfo.getExpandOption()).thenReturn(expand);
    when(skip.getValue()).thenReturn(2);
    final Optional<JPAKeyBoundary> act = cut.getKeyBoundary(requestContext, hops, page);
    assertTrue(act.isPresent());
    assertEquals(12, act.get().getKeyBoundary().getMin().get(et.getKey().get(0)));
  }

  @Test
  void checkGetKeyBoundaryContainsNoHops() throws ODataJPAModelException, ODataJPAProcessException {

    addTuple(12);
    cut = new JPAExpandQueryResult(queryResult, null, et, Collections.emptyList());
    when(uriInfo.getSkipOption()).thenReturn(skip);
    when(uriInfo.getExpandOption()).thenReturn(expand);
    when(skip.getValue()).thenReturn(2);
    final Optional<JPAKeyBoundary> act = cut.getKeyBoundary(requestContext, hops, page);
    assertTrue(act.isPresent());
    assertEquals(2, act.get().getNoHops());
  }

  @Test
  void checkGetKeyBoundaryTwoResultWithSkip() throws ODataJPAModelException, ODataJPAProcessException {

    addTuple(12);
    addTuple(15);
    cut = new JPAExpandQueryResult(queryResult, null, et, Collections.emptyList());
    when(uriInfo.getSkipOption()).thenReturn(skip);
    when(uriInfo.getExpandOption()).thenReturn(expand);
    when(skip.getValue()).thenReturn(2);
    final Optional<JPAKeyBoundary> act = cut.getKeyBoundary(requestContext, hops, page);
    assertTrue(act.isPresent());
    assertEquals(12, act.get().getKeyBoundary().getMin().get(et.getKey().get(0)));
    assertEquals(15, act.get().getKeyBoundary().getMax().get(et.getKey().get(0)));
  }

  @Test
  void checkGetKeyBoundaryOneCompoundResultWithTop() throws ODataJPAModelException, ODataJPAProcessException {

    final Map<String, Object> key = new HashMap<>(1);
    final TupleDouble tuple = new TupleDouble(key);
    tuples.add(tuple);
    key.put("codePublisher", "ISO");
    key.put("codeID", "3166-1");
    key.put("divisionCode", "BEL");
    cut = new JPAExpandQueryResult(queryResult, null, helper.getJPAEntityType("AdministrativeDivisionDescriptions"),
        Collections.emptyList());
    when(uriInfo.getTopOption()).thenReturn(top);
    when(uriInfo.getExpandOption()).thenReturn(expand);
    when(top.getValue()).thenReturn(2);
    final Optional<JPAKeyBoundary> act = cut.getKeyBoundary(requestContext, hops, page);
    assertTrue(act.isPresent());
    assertNotNull(act.get().getKeyBoundary().getMin());
    assertNull(act.get().getKeyBoundary().getMax());
  }

  @Test
  void checkGetKeyBoundaryCollectionRequested() throws ODataJPAModelException, ODataJPAProcessException {

    addTuple(12);
    addTuple(15);
    cut = new JPAExpandQueryResult(queryResult, null, et, Collections.emptyList());
    when(uriInfo.getSkipOption()).thenReturn(skip);
    when(uriInfo.getExpandOption()).thenReturn(expand);
    when(skip.getValue()).thenReturn(2);
    final Optional<JPAKeyBoundary> act = cut.getKeyBoundary(requestContext, hops, page);
    assertTrue(act.isPresent());
    assertEquals(12, act.get().getKeyBoundary().getMin().get(et.getKey().get(0)));
    assertEquals(15, act.get().getKeyBoundary().getMax().get(et.getKey().get(0)));
  }

  @Test
  void checkGetKeyBoundaryEmptyBoundaryNoTopSkipPageSkip() throws ODataJPAModelException, ODataJPAProcessException {

    addTuple(12);
    when(uriInfo.getExpandOption()).thenReturn(expand);
    page = new JPAODataPage(null, 10, Integer.MAX_VALUE, null);
    cut = new JPAExpandQueryResult(queryResult, null, helper.getJPAEntityType("Organizations"),
        Collections.emptyList());
    final Optional<JPAKeyBoundary> act = cut.getKeyBoundary(requestContext, hops, page);
    assertTrue(act.isPresent());
  }

  @Test
  void checkGetKeyBoundaryEmptyBoundaryNoTopSkipPageTop() throws ODataJPAModelException, ODataJPAProcessException {

    addTuple(12);
    when(uriInfo.getExpandOption()).thenReturn(expand);
    page = new JPAODataPage(null, 0, 10, null);
    cut = new JPAExpandQueryResult(queryResult, null, helper.getJPAEntityType("Organizations"),
        Collections.emptyList());
    final Optional<JPAKeyBoundary> act = cut.getKeyBoundary(requestContext, hops, page);
    assertTrue(act.isPresent());
  }

  private void addTuple(final Integer value) {
    final Map<String, Object> key = new HashMap<>(1);
    final TupleDouble tuple = new TupleDouble(key);
    tuples.add(tuple);
    key.put("ID", value);
  }

}
