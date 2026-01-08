package com.sap.olingo.jpa.processor.core.query;

import static java.util.Collections.emptyList;
import static java.util.Optional.empty;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import jakarta.persistence.Tuple;

import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriInfoResource;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.apache.olingo.server.api.uri.queryoption.ExpandOption;
import org.apache.olingo.server.api.uri.queryoption.SkipOption;
import org.apache.olingo.server.api.uri.queryoption.TopOption;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.api.JPAODataQueryDirectives;
import com.sap.olingo.jpa.processor.core.api.JPAODataQueryDirectives.UuidSortOrder;
import com.sap.olingo.jpa.processor.core.api.JPAODataRequestContextAccess;
import com.sap.olingo.jpa.processor.core.converter.JPAExpandResult;
import com.sap.olingo.jpa.processor.core.converter.JPAResultConverter;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAQueryException;
import com.sap.olingo.jpa.processor.core.testmodel.AdministrativeDivision;
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
  private JPAODataQueryDirectives directives;

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
    directives = new JPAODataQueryDirectives.JPAODataQueryDirectivesImpl(0, UuidSortOrder.AS_JAVA_UUID);
    requestContext = mock(JPAODataRequestContextAccess.class);
    top = mock(TopOption.class);
    skip = mock(SkipOption.class);
    expand = mock(ExpandOption.class);
    when(requestContext.getUriInfo()).thenReturn(uriInfo);
    when(requestContext.getQueryDirectives()).thenReturn(directives);
    queryResult.put("root", tuples);
  }

  @Test
  void checkGetKeyBoundaryEmptyBoundaryNoTopSkipPageEmpty() throws ODataJPAModelException, ODataJPAProcessException {

    cut = new JPAExpandQueryResult(queryResult, null, helper.getJPAEntityType("Organizations"),
        emptyList(), empty());
    final Optional<JPAKeyBoundary> act = cut.getKeyBoundary(requestContext, hops);
    assertFalse(act.isPresent());
  }

  @Test
  void checkGetKeyBoundaryEmptyBoundaryExpandWithoutTopSkip() throws ODataJPAModelException,
      ODataJPAProcessException {

    cut = new JPAExpandQueryResult(queryResult, null, helper.getJPAEntityType("AdministrativeDivisionDescriptions"),
        emptyList(), empty());
    when(uriInfo.getExpandOption()).thenReturn(expand);
    final Optional<JPAKeyBoundary> act = cut.getKeyBoundary(requestContext, hops);
    assertFalse(act.isPresent());
  }

  @Test
  void checkGetKeyBoundaryEmptyBoundaryNoExpand() throws ODataJPAModelException, ODataJPAProcessException {

    final Map<String, Object> key = new HashMap<>(1);
    final TupleDouble tuple = new TupleDouble(key);
    tuples.add(tuple);
    key.put("ID", Integer.valueOf(10));
    cut = new JPAExpandQueryResult(queryResult, null, helper.getJPAEntityType("AdministrativeDivisionDescriptions"),
        emptyList(), empty());
    when(uriInfo.getTopOption()).thenReturn(top);
    when(top.getValue()).thenReturn(2);
    final Optional<JPAKeyBoundary> act = cut.getKeyBoundary(requestContext, hops);
    assertFalse(act.isPresent());
  }

  @Test
  void checkGetKeyBoundaryEmptyBoundaryNotComparable() throws ODataJPAModelException,
      NumberFormatException, ODataApplicationException {

    cut = new JPAExpandQueryResult(queryResult, null, helper.getJPAEntityType("AdministrativeDivisionDescriptions"),
        emptyList(), empty());
    final Optional<JPAKeyBoundary> act = cut.getKeyBoundary(requestContext, hops);
    assertFalse(act.isPresent());
  }

  @Test
  void checkGetKeyBoundaryEmptyBoundaryNoResult() throws ODataJPAModelException, ODataJPAProcessException {

    queryResult.put("root", emptyList());

    cut = new JPAExpandQueryResult(queryResult, null, helper.getJPAEntityType("Organizations"),
        emptyList(), empty());
    when(uriInfo.getTopOption()).thenReturn(top);
    when(uriInfo.getExpandOption()).thenReturn(expand);
    when(top.getValue()).thenReturn(2);
    final Optional<JPAKeyBoundary> act = cut.getKeyBoundary(requestContext, hops);
    assertFalse(act.isPresent());
  }

  @Test
  void checkGetKeyBoundaryOneResultWithTop() throws ODataJPAModelException, ODataJPAProcessException {

    final Map<String, Object> key = new HashMap<>(1);
    final TupleDouble tuple = new TupleDouble(key);
    tuples.add(tuple);
    key.put("ID", Integer.valueOf(10));
    cut = new JPAExpandQueryResult(queryResult, null, et, emptyList(), empty());
    when(uriInfo.getTopOption()).thenReturn(top);
    when(uriInfo.getExpandOption()).thenReturn(expand);
    when(top.getValue()).thenReturn(2);
    final Optional<JPAKeyBoundary> act = cut.getKeyBoundary(requestContext, hops);
    assertTrue(act.isPresent());
    assertEquals(10, act.get().getKeyBoundary().getMin().get(et.getKey().get(0)));
  }

  @Test
  void checkGetKeyBoundaryOneResultWithSkip() throws ODataJPAModelException, ODataJPAProcessException {

    addTuple(12);
    cut = new JPAExpandQueryResult(queryResult, null, et, emptyList(), empty());
    when(uriInfo.getSkipOption()).thenReturn(skip);
    when(uriInfo.getExpandOption()).thenReturn(expand);
    when(skip.getValue()).thenReturn(2);
    final Optional<JPAKeyBoundary> act = cut.getKeyBoundary(requestContext, hops);
    assertTrue(act.isPresent());
    assertEquals(12, act.get().getKeyBoundary().getMin().get(et.getKey().get(0)));
  }

  @Test
  void checkGetKeyBoundaryContainsNoHops() throws ODataJPAProcessException {

    addTuple(12);
    cut = new JPAExpandQueryResult(queryResult, null, et, emptyList(), empty());
    when(uriInfo.getSkipOption()).thenReturn(skip);
    when(uriInfo.getExpandOption()).thenReturn(expand);
    when(skip.getValue()).thenReturn(2);
    final Optional<JPAKeyBoundary> act = cut.getKeyBoundary(requestContext, hops);
    assertTrue(act.isPresent());
    assertEquals(2, act.get().getNoHops());
  }

  @Test
  void checkGetKeyBoundaryTwoResultWithSkip() throws ODataJPAModelException, ODataJPAProcessException {

    addTuple(12);
    addTuple(15);
    cut = new JPAExpandQueryResult(queryResult, null, et, emptyList(), empty());
    when(uriInfo.getSkipOption()).thenReturn(skip);
    when(uriInfo.getExpandOption()).thenReturn(expand);
    when(skip.getValue()).thenReturn(2);
    final Optional<JPAKeyBoundary> act = cut.getKeyBoundary(requestContext, hops);
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
        emptyList(), empty());
    when(uriInfo.getTopOption()).thenReturn(top);
    when(uriInfo.getExpandOption()).thenReturn(expand);
    when(top.getValue()).thenReturn(2);
    final Optional<JPAKeyBoundary> act = cut.getKeyBoundary(requestContext, hops);
    assertTrue(act.isPresent());
    assertNotNull(act.get().getKeyBoundary().getMin());
    assertNull(act.get().getKeyBoundary().getMax());
  }

  @Test
  void checkGetKeyBoundaryEmptyBoundaryNoTopSkipPageSkip() throws ODataJPAModelException, ODataJPAProcessException {

    addTuple(12);
    when(uriInfo.getExpandOption()).thenReturn(expand);
    cut = new JPAExpandQueryResult(queryResult, null, helper.getJPAEntityType("Organizations"),
        emptyList(), empty());
    final Optional<JPAKeyBoundary> act = cut.getKeyBoundary(requestContext, hops);
    assertFalse(act.isPresent());
  }

  @Test
  void checkConstructor() {
    cut = new JPAExpandQueryResult(et, List.of());
    assertNotNull(cut);
  }

  @Test
  void checkRemoveResult() throws ODataJPAModelException {
    final Map<String, Object> key = new HashMap<>(1);
    final TupleDouble tuple = new TupleDouble(key);
    tuples.add(tuple);
    cut = new JPAExpandQueryResult(queryResult, null, helper.getJPAEntityType("AdministrativeDivisionDescriptions"),
        emptyList(), empty());
    assertNotNull(cut.removeResult("root"));
    assertNull(cut.removeResult("root"));
  }

  @Test
  void checkHasCountFalse() throws ODataJPAModelException {
    final Map<String, Object> key = new HashMap<>(1);
    final TupleDouble tuple = new TupleDouble(key);
    tuples.add(tuple);
    cut = new JPAExpandQueryResult(queryResult, null, helper.getJPAEntityType("AdministrativeDivisionDescriptions"),
        emptyList(), empty());
    assertFalse(cut.hasCount());
  }

  @Test
  void checkHasCountTrue() throws ODataJPAModelException {
    final Map<String, Object> key = new HashMap<>(1);
    final TupleDouble tuple = new TupleDouble(key);
    tuples.add(tuple);
    final Map<String, Long> counts = new HashMap<>(1);
    cut = new JPAExpandQueryResult(queryResult, counts, helper.getJPAEntityType("AdministrativeDivisionDescriptions"),
        emptyList(), empty());
    assertTrue(cut.hasCount());
  }

  @Test
  void checkPutChildrenThrowsExceptionOnIdenticalKey() throws ODataJPAModelException, ODataApplicationException {
    final Map<String, Object> key = new HashMap<>(1);
    final TupleDouble tuple = new TupleDouble(key);
    tuples.add(tuple);
    et = helper.getJPAEntityType(AdministrativeDivision.class);
    final Map<JPAAssociationPath, JPAExpandResult> childResults = Map.of(et.getAssociationPath("Parent"),
        new JPAExpandQueryResult(et, List.of()));
    cut = new JPAExpandQueryResult(queryResult, null, helper.getJPAEntityType(AdministrativeDivision.class),
        emptyList(), empty());
    cut.putChildren(childResults);
    assertThrows(ODataJPAQueryException.class, () -> cut.putChildren(childResults));
  }

  @Test
  void checkConvert() throws ODataJPAModelException {
    final JPAResultConverter converter = mock(JPAResultConverter.class);
    cut = new JPAExpandQueryResult(queryResult, null, helper.getJPAEntityType(AdministrativeDivision.class),
        emptyList(), empty());
    assertThrows(IllegalAccessError.class, () -> cut.convert(converter));
  }

  private void addTuple(final Integer value) {
    final Map<String, Object> key = new HashMap<>(1);
    final TupleDouble tuple = new TupleDouble(key);
    tuples.add(tuple);
    key.put("ID", value);
  }

}
