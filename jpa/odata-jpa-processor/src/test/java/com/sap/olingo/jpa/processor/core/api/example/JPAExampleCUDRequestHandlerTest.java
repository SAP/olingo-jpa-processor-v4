package com.sap.olingo.jpa.processor.core.api.example;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.persistence.EntityManager;

import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.commons.api.http.HttpMethod;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAProtectionInfo;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.api.JPAClaimsPair;
import com.sap.olingo.jpa.processor.core.api.JPAODataClaimProvider;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessException;
import com.sap.olingo.jpa.processor.core.modify.JPAUpdateResult;
import com.sap.olingo.jpa.processor.core.processor.JPAModifyUtil;
import com.sap.olingo.jpa.processor.core.processor.JPARequestEntity;
import com.sap.olingo.jpa.processor.core.processor.JPARequestLink;
import com.sap.olingo.jpa.processor.core.testmodel.AdministrativeDivision;
import com.sap.olingo.jpa.processor.core.testmodel.AdministrativeDivisionKey;
import com.sap.olingo.jpa.processor.core.testmodel.Collection;
import com.sap.olingo.jpa.processor.core.testmodel.InhouseAddress;
import com.sap.olingo.jpa.processor.core.testmodel.Organization;
import com.sap.olingo.jpa.processor.core.testmodel.Person;
import com.sap.olingo.jpa.processor.core.testmodel.PostalAddressData;
import com.sap.olingo.jpa.processor.core.testobjects.OrganizationWithAudit;
import com.sap.olingo.jpa.processor.core.util.TestBase;
import com.sap.olingo.jpa.processor.core.util.TestHelper;

public class JPAExampleCUDRequestHandlerTest extends TestBase {
  private JPAExampleCUDRequestHandler cut;
  private EntityManager em;
  private JPARequestEntity requestEntity;
  private Map<String, Object> data;
  private Map<String, Object> keys;

  @BeforeEach
  public void setup() throws ODataException {
    helper = new TestHelper(emf, PUNIT_NAME);
    em = mock(EntityManager.class);
    requestEntity = mock(JPARequestEntity.class);
    data = new HashMap<>();
    keys = new HashMap<>();
    doReturn(new JPAModifyUtil()).when(requestEntity).getModifyUtil();
    doReturn(data).when(requestEntity).getData();
    doReturn(keys).when(requestEntity).getKeys();
    cut = new JPAExampleCUDRequestHandler();
  }

  @Test
  public void checkCreateEntity() throws ODataJPAProcessException, ODataJPAModelException {

    final Object act = createAdminDiv();
    assertNotNull(act);
    assertEquals("NUTS2", ((AdministrativeDivision) act).getCodeID());
    verify(em).persist(act);
  }

  @Test
  public void checkCreateEntityWithPrimitiveCollcetion() throws ODataJPAProcessException, ODataJPAModelException {

    doReturn(helper.getJPAEntityType("Organizations")).when(requestEntity).getEntityType();

    final List<String> comments = new ArrayList<>(2);
    comments.add("This is just test");
    comments.add("YAT");

    data.put("iD", "504");
    data.put("comment", comments);

    final Organization act = (Organization) cut.createEntity(requestEntity, em);
    assertNotNull(act);
    assertEquals("504", act.getID());
    assertFalse(act.getComment().isEmpty());
    verify(em).persist(act);
  }

  @Test
  public void checkCreateEntityWithComplexCollcetion() throws ODataJPAProcessException, ODataJPAModelException {

    doReturn(helper.getJPAEntityType("Persons")).when(requestEntity).getEntityType();

    final List<Map<String, Object>> inhouseAddrs = new ArrayList<>(2);
    final Map<String, Object> addr1 = new HashMap<>(4);
    addr1.put("roomNumber", 32);
    addr1.put("floor", 2);
    addr1.put("building", "7");
    addr1.put("taskID", "MAIN");
    inhouseAddrs.add(addr1);

    final Map<String, Object> addr2 = new HashMap<>(4);
    addr2.put("roomNumber", 245);
    addr2.put("floor", -3);
    addr2.put("building", "1");
    addr2.put("taskID", "DEV");
    inhouseAddrs.add(addr2);

    data.put("iD", "707");
    data.put("inhouseAddress", inhouseAddrs);

    final Person act = (Person) cut.createEntity(requestEntity, em);
    assertNotNull(act);
    assertEquals("707", act.getID());
    assertEquals(2, act.getInhouseAddress().size());
    assertNotNull(act.getInhouseAddress().get(0).getTaskID());
    verify(em).persist(act);
  }

  @Test
  public void checkCreateEntityWithComplexCollcetionInitialyNull() throws ODataJPAProcessException,
      ODataJPAModelException {
    assertNull(new Collection().getNested());

    doReturn(helper.getJPAEntityType("Collections")).when(requestEntity).getEntityType();

    final Map<String, Object> complex = new HashMap<>();
    final List<Map<String, Object>> nested = new ArrayList<>();
    final Map<String, Object> nestedItem = new HashMap<>();
    final Map<String, Object> inner = new HashMap<>();
    final List<String> comment = new ArrayList<>();
    inner.put("figure1", 100L);

    nestedItem.put("inner", inner);
    nestedItem.put("number", 50L);
    nested.add(nestedItem);

    comment.add("How about this");

    complex.put("number", 25L);
    complex.put("comment", comment);
    data.put("iD", "707");
    data.put("complex", complex);
    data.put("nested", nested);

    final Collection act = (Collection) cut.createEntity(requestEntity, em);
    assertNotNull(act);

    assertEquals("707", act.getID());
    assertNotNull(act.getNested());
    assertEquals(1, act.getNested().size());
    assertEquals(50L, act.getNested().get(0).getNumber());
    assertEquals(100L, act.getNested().get(0).getInner().getFigure1());
    assertNotNull(act.getComplex());
    assertNotNull(act.getComplex().getComment());
    assertEquals(1, act.getComplex().getComment().size());
  }

  @Test
  public void checkCreateLinkedEntity() throws ODataJPAProcessException, ODataJPAModelException {
    // http://localhost:8080/tutorial/v1/AdministrativeDivisions(DivisionCode='DE5',CodeID='NUTS1',CodePublisher='Eurostat')/Children
    // key = {divisionCode=DE5, codeID=NUTS1, codePublisher=Eurostat}
    // jpaDeepEntities = JPAAssPath ; JPARequestEntity

    final JPAEntityType et = helper.getJPAEntityType("AdministrativeDivisions");
    final JPAAssociationPath path = et.getAssociationPath("Children");
    final Map<JPAAssociationPath, List<JPARequestEntity>> deepEntities = new HashMap<>();
    final JPARequestEntity deepEntity = mock(JPARequestEntity.class);
    final Map<String, Object> deepData = new HashMap<>();
    final AdministrativeDivision parent = new AdministrativeDivision(new AdministrativeDivisionKey("Eurostat", "NUTS1",
        "DE5"));

    doReturn(et).when(requestEntity).getEntityType();
    keys.put("divisionCode", "DE5");
    keys.put("codeID", "NUTS1");
    keys.put("codePublisher", "Eurostat");
    doReturn(deepEntities).when(requestEntity).getRelatedEntities();
    deepEntities.put(path, Arrays.asList(deepEntity));

    doReturn(et).when(deepEntity).getEntityType();
    doReturn(deepData).when(deepEntity).getData();
    doReturn(new JPAModifyUtil()).when(deepEntity).getModifyUtil();

    deepData.put("DivisionCode", "DE52");
    deepData.put("CodeID", "NUTS2");
    deepData.put("CodePublisher", "Eurostat");
    deepData.put("CountryCode", "DEU");

    doReturn(parent).when(em).getReference(eq(et.getTypeClass()), any());

    final AdministrativeDivision act = (AdministrativeDivision) cut.createEntity(requestEntity, em);
    assertNotNull(act.getChildren());
    assertEquals(1, act.getChildren().size());

  }

  @Test
  public void checkUpdateEntityNotFound() throws ODataJPAProcessException, ODataJPAModelException {
    final String id = "1";
    final Organization beforImage = new Organization(id);
    final JPAEntityType et = helper.getJPAEntityType("Organizations");
    beforImage.setName1("Example Ltd");

    doReturn(et).when(requestEntity).getEntityType();
    doReturn(null).when(em).find(eq(et.getTypeClass()), any());

    data.put("name1", "Example SE");
    keys.put("iD", id);

    final ODataJPAProcessException exception = assertThrows(ODataJPAProcessException.class, () -> cut.updateEntity(
        requestEntity, em, HttpMethod.PATCH));

    assertEquals(404, exception.getStatusCode());

  }

  @Test
  public void checkDeleteSimplePrimitiveProperty() throws ODataJPAProcessException, ODataJPAModelException {
    final String id = "1";
    final Organization beforImage = new Organization(id);
    beforImage.setName1("Example Ltd");

    doReturn(helper.getJPAEntityType("Organizations")).when(requestEntity).getEntityType();
    doReturn(beforImage).when(em).find(Organization.class, id);

    data.put("name1", null);
    keys.put("iD", id);

    final JPAUpdateResult act = cut.updateEntity(requestEntity, em, HttpMethod.DELETE);

    assertFalse(act.wasCreate());
    assertNull(((Organization) act.getModifyedEntity()).getName1());
  }

  @Test
  public void checkDeletePrimitiveCollctionProperty() throws ODataJPAProcessException, ODataJPAModelException {
    final String id = "1";
    final Organization beforImage = new Organization(id);
    beforImage.getComment().add("YAC");

    doReturn(helper.getJPAEntityType("Organizations")).when(requestEntity).getEntityType();
    doReturn(beforImage).when(em).find(Organization.class, id);

    data.put("comment", null);
    keys.put("iD", id);

    final JPAUpdateResult act = cut.updateEntity(requestEntity, em, HttpMethod.DELETE);

    assertFalse(act.wasCreate());
    assertNull(((Organization) act.getModifyedEntity()).getComment());
  }

  @Test
  public void checkDeleteSimpleComplexProperty() throws ODataJPAProcessException, ODataJPAModelException {
    final String id = "1";
    final Organization beforImage = new Organization(id);
    beforImage.setAddress(new PostalAddressData());

    doReturn(helper.getJPAEntityType("Organizations")).when(requestEntity).getEntityType();
    doReturn(beforImage).when(em).find(Organization.class, id);

    data.put("address", null);
    keys.put("iD", id);

    final JPAUpdateResult act = cut.updateEntity(requestEntity, em, HttpMethod.DELETE);

    assertFalse(act.wasCreate());
    assertNull(((Organization) act.getModifyedEntity()).getAddress());
  }

  @Test
  public void checkDeleteComplexCollctionProperty() throws ODataJPAProcessException, ODataJPAModelException {

    final String id = "2";
    final Person beforImage = new Person();
    beforImage.setID(id);
    beforImage.getInhouseAddress().add(new InhouseAddress("DEV", "D-2"));

    doReturn(helper.getJPAEntityType("Persons")).when(requestEntity).getEntityType();
    doReturn(beforImage).when(em).find(Person.class, id);

    data.put("inhouseAddress", null);
    keys.put("iD", id);

    final JPAUpdateResult act = cut.updateEntity(requestEntity, em, HttpMethod.DELETE);

    assertFalse(act.wasCreate());
    assertNull(((Person) act.getModifyedEntity()).getInhouseAddress());
  }

  @Test
  public void checkDeleteSimplePrimitivePropertyDeep() throws ODataJPAProcessException, ODataJPAModelException {
    final String id = "1";
    final Organization beforImage = new Organization(id);
    final PostalAddressData addr = new PostalAddressData();
    final Map<String, Object> addrData = new HashMap<>();
    addr.setPOBox("23145-1235");
    addr.setCityName("Hamburg");
    beforImage.setAddress(addr);
    beforImage.setName1("Example Ltd");

    doReturn(helper.getJPAEntityType("Organizations")).when(requestEntity).getEntityType();
    doReturn(beforImage).when(em).find(Organization.class, id);

    data.put("address", addrData);
    addrData.put("pOBox", null);
    keys.put("iD", id);

    final JPAUpdateResult act = cut.updateEntity(requestEntity, em, HttpMethod.DELETE);

    assertFalse(act.wasCreate());
    assertNull(((Organization) act.getModifyedEntity()).getAddress().getPOBox());
    assertEquals("Hamburg", ((Organization) act.getModifyedEntity()).getAddress().getCityName());
  }

  @Test
  public void checkDeleteEntity() throws ODataJPAProcessException, ODataJPAModelException {
    final String id = "1";
    final Organization beforImage = new Organization(id);

    doReturn(beforImage).when(em).find(Organization.class, id);
    doReturn(helper.getJPAEntityType("Organizations")).when(requestEntity).getEntityType();
    keys.put("iD", id);
    cut.deleteEntity(requestEntity, em);
    verify(em).remove(beforImage);
  }

  @Test
  public void checkDeleteNoErrorIfEntityDoesNotExists() throws ODataJPAProcessException, ODataJPAModelException {
    final String id = "1";
    final Organization beforImage = new Organization(id);

    doReturn(null).when(em).find(Organization.class, id);
    doReturn(helper.getJPAEntityType("Organizations")).when(requestEntity).getEntityType();
    keys.put("iD", id);
    cut.deleteEntity(requestEntity, em);
    verify(em, times(0)).remove(beforImage);
  }

  @Test
  public void checkPatchOneSimplePrimitiveValue() throws ODataJPAModelException, ODataJPAProcessException {
    final JPAUpdateResult act = updateSimplePrimitiveValue();

    assertFalse(act.wasCreate());
    assertEquals("Example SE", ((Organization) act.getModifyedEntity()).getName1());
  }

  @Test
  public void checkPatchOneSimpleComplexValue() throws ODataJPAModelException, ODataJPAProcessException {

    final String id = "1";
    final Organization beforImage = new Organization(id);
    final PostalAddressData beforeAddr = new PostalAddressData();
    final Map<String, Object> changedAddr = new HashMap<>();

    beforImage.setAddress(beforeAddr);
    beforeAddr.setHouseNumber("45A");
    beforeAddr.setCityName("Test");
    beforeAddr.setPostalCode("12345");

    doReturn(helper.getJPAEntityType("Organizations")).when(requestEntity).getEntityType();
    doReturn(beforImage).when(em).find(Organization.class, id);

    changedAddr.put("houseNumber", "45");
    changedAddr.put("streetName", "Example Street");

    data.put("address", changedAddr);
    keys.put("iD", id);

    final JPAUpdateResult act = cut.updateEntity(requestEntity, em, HttpMethod.DELETE);

    assertFalse(act.wasCreate());
    assertNotNull(((Organization) act.getModifyedEntity()).getAddress());
    final PostalAddressData afterImage = ((Organization) act.getModifyedEntity()).getAddress();

    assertEquals("45", afterImage.getHouseNumber());
    assertEquals("Test", afterImage.getCityName());
    assertEquals("12345", afterImage.getPostalCode());
    assertEquals("Example Street", afterImage.getStreetName());
  }

  @Test
  public void checkPatchEmptyComplexCollctionProperty() throws ODataJPAProcessException, ODataJPAModelException {

    final String id = "2";
    final Person beforImage = new Person();
    final List<Map<String, Object>> newInhouseAddresses = new ArrayList<>(2);
    beforImage.setID(id);
    beforImage.getInhouseAddress().add(new InhouseAddress("DEV", "D-2"));

    doReturn(helper.getJPAEntityType("Persons")).when(requestEntity).getEntityType();
    doReturn(beforImage).when(em).find(Person.class, id);

    data.put("inhouseAddress", newInhouseAddresses);
    keys.put("iD", id);

    final JPAUpdateResult act = cut.updateEntity(requestEntity, em, HttpMethod.DELETE);

    assertFalse(act.wasCreate());
    assertNotNull(((Person) act.getModifyedEntity()).getInhouseAddress());
    assertTrue(((Person) act.getModifyedEntity()).getInhouseAddress().isEmpty());
  }

  @Test
  public void checkPatchComplexCollctionProperty() throws ODataJPAProcessException, ODataJPAModelException {

    final String id = "2";
    final Person beforImage = new Person();
    final List<Map<String, Object>> newInhouseAddresses = new ArrayList<>(2);
    beforImage.setID(id);
    beforImage.getInhouseAddress().add(new InhouseAddress("DEV", "D-2"));

    doReturn(helper.getJPAEntityType("Persons")).when(requestEntity).getEntityType();
    doReturn(beforImage).when(em).find(Person.class, id);

    final Map<String, Object> addr1 = new HashMap<>();
    addr1.put("taskID", "MAIN");
    addr1.put("floor", "U1");
    newInhouseAddresses.add(addr1);
    final Map<String, Object> addr2 = new HashMap<>();
    addr2.put("taskID", "EDU");
    addr2.put("floor", "E");
    newInhouseAddresses.add(addr2);

    data.put("inhouseAddress", newInhouseAddresses);
    keys.put("iD", id);

    final JPAUpdateResult act = cut.updateEntity(requestEntity, em, HttpMethod.PATCH);

    assertFalse(act.wasCreate());
    assertNotNull(((Person) act.getModifyedEntity()).getInhouseAddress());
    final List<InhouseAddress> actInhouseAddrs = ((Person) act.getModifyedEntity()).getInhouseAddress();
    assertEquals(2, actInhouseAddrs.size());
    assertTrue(actInhouseAddrs.get(0).getBuilding() == null || actInhouseAddrs.get(0).getBuilding().isEmpty());
    assertTrue(actInhouseAddrs.get(1).getBuilding() == null || actInhouseAddrs.get(1).getBuilding().isEmpty());
  }

  @Test
  public void checkPatchOnePrimitiveCollectionValue() throws ODataJPAModelException, ODataJPAProcessException {
    final String id = "1";
    final Organization beforImage = new Organization(id);
    final List<String> newComments = Arrays.asList("This is a test", "YAT");
    beforImage.getComment().add("YAC");

    doReturn(helper.getJPAEntityType("Organizations")).when(requestEntity).getEntityType();
    doReturn(beforImage).when(em).find(Organization.class, id);

    data.put("comment", newComments);
    keys.put("iD", id);

    final JPAUpdateResult act = cut.updateEntity(requestEntity, em, HttpMethod.DELETE);

    assertFalse(act.wasCreate());
    assertNotNull(((Organization) act.getModifyedEntity()).getComment());
    final List<String> actComments = ((Organization) act.getModifyedEntity()).getComment();
    assertEquals(2, actComments.size());
    assertTrue(actComments.contains("YAT"));
    assertTrue(actComments.contains("This is a test"));
  }

  @Test
  public void checkPatchCreateBindingLinkBetweenTwoEntities() throws ODataJPAModelException,
      ODataJPAProcessException {
    // URL: ../AdministrativeDivisions(DivisionCode='DE51',CodeID='NUTS2',CodePublisher='Eurostat')
    // Body: {
    // "Parent@odata.bind": ["AdministrativeDivisions(DivisionCode='DE5',CodeID='NUTS1',CodePublisher='Eurostat')" ] }
    final JPAEntityType et = helper.getJPAEntityType("AdministrativeDivisions");
    final JPAAssociationPath path = et.getAssociationPath("Children");
    final Map<JPAAssociationPath, List<JPARequestLink>> relationLinks = new HashMap<>();
    final JPARequestLink link = mock(JPARequestLink.class);
    final Map<String, Object> childKeys = new HashMap<>();
    final AdministrativeDivision parent = new AdministrativeDivision(new AdministrativeDivisionKey("Eurostat", "NUTS1",
        "DE5"));
    final AdministrativeDivision child = new AdministrativeDivision(new AdministrativeDivisionKey("Eurostat", "NUTS2",
        "DE51"));

    doReturn(et).when(requestEntity).getEntityType();
    keys.put("divisionCode", "DE5");
    keys.put("codeID", "NUTS1");
    keys.put("codePublisher", "Eurostat");
    doReturn(relationLinks).when(requestEntity).getRelationLinks();

    relationLinks.put(path, Arrays.asList(link));
    doReturn(et).when(link).getEntityType();
    doReturn(childKeys).when(link).getRelatedKeys();
    childKeys.put("divisionCode", "DE51");
    childKeys.put("codeID", "NUTS2");
    childKeys.put("codePublisher", "Eurostat");
    doReturn(childKeys).when(link).getValues();

    doReturn(parent).when(em).find(eq(et.getTypeClass()), eq(parent.getKey()));
    doReturn(child).when(em).find(eq(et.getTypeClass()), eq(child.getKey()));

    final JPAUpdateResult act = cut.updateEntity(requestEntity, em, HttpMethod.DELETE);

    assertNotNull(act);
    assertEquals(parent, act.getModifyedEntity());
    assertEquals(1, parent.getChildren().size());
    assertEquals("DE51", parent.getChildren().get(0).getDivisionCode());
  }

  @Test
  public void checkAuditFieldsSetOnCreate() throws ODataJPAModelException, ODataJPAProcessException {
    final OrganizationWithAudit act = createOrganization();
    cut.validateChanges(em);
    assertNotNull(act.getCreatedBy());
    assertNotNull(act.getCreatedAt());
    assertEquals(act.getCreatedBy(), act.getUpdatedBy());
    assertEquals(act.getCreatedAt(), act.getUpdatedAt());
  }

  @Test
  public void checkAuditFieldsSetOnUpdate() throws ODataJPAModelException, ODataJPAProcessException {
    final OrganizationWithAudit act = (OrganizationWithAudit) updateOrganization().getModifyedEntity();
    cut.validateChanges(em);
    assertNotNull(act.getUpdatedBy());
    assertNotNull(act.getUpdatedAt());
    assertNotEquals(act.getCreatedBy(), act.getUpdatedBy());
    assertNotEquals(act.getCreatedAt(), act.getUpdatedAt());
  }

  @Test
  public void checkAutorisationsCreateRejectsNotClaimsNotAllowed() throws ODataJPAModelException,
      ODataJPAProcessException {
    final JPAExampleModifyException act = assertThrows(JPAExampleModifyException.class,
        () -> createPersonProtected(null));
    assertEquals(HttpStatusCode.FORBIDDEN.getStatusCode(), act.getStatusCode());
  }

  @Test
  public void checkAutorisationsCreateRejectsAttributeNotPresent() throws ODataJPAModelException,
      ODataJPAProcessException {
    final JPAODataClaimProvider claims = mock(JPAODataClaimProvider.class);
    final JPAExampleModifyException act = assertThrows(JPAExampleModifyException.class, () -> createPersonProtected(
        claims));
    assertEquals(HttpStatusCode.FORBIDDEN.getStatusCode(), act.getStatusCode());
  }

  @Test
  public void checkAutorisationsCreateRejectsAttributeNotMatch() throws ODataJPAModelException,
      ODataJPAProcessException {
    final JPAClaimsPair<String> claim = new JPAClaimsPair<>("DOT01");
    final JPAODataClaimProvider claims = mock(JPAODataClaimProvider.class);
    when(claims.get("BuildingNumber")).thenReturn(Collections.singletonList(claim));
    final JPAExampleModifyException act = assertThrows(JPAExampleModifyException.class,
        () -> createPersonProtected(claims));
    assertEquals(HttpStatusCode.FORBIDDEN.getStatusCode(), act.getStatusCode());
  }

  @Test
  public void checkAutorisationsCreateRejectedOnyOneProvided() throws ODataJPAModelException,
      ODataJPAProcessException {
    final JPAClaimsPair<String> claim = new JPAClaimsPair<>("MID*");
    final JPAODataClaimProvider claims = mock(JPAODataClaimProvider.class);
    when(claims.get("BuildingNumber")).thenReturn(Collections.singletonList(claim));
    final JPAExampleModifyException act = assertThrows(JPAExampleModifyException.class,
        () -> createPersonProtected(claims));
    assertEquals(HttpStatusCode.FORBIDDEN.getStatusCode(), act.getStatusCode());
  }

  @Test
  public void checkAutorisationsCreateAllowedWithWildCardMulti() throws ODataJPAModelException, // NOSONAR
      ODataJPAProcessException {
    final JPAClaimsPair<String> claim = new JPAClaimsPair<>("MID*");
    createPersonProtected(createPersonProtectedClaims(claim));
  }

  @Test
  public void checkAutorisationsCreateAllowedWithWildCardSingle() throws ODataJPAModelException, // NOSONAR
      ODataJPAProcessException {
    final JPAClaimsPair<String> claim = new JPAClaimsPair<>("MID_5");
    createPersonProtected(createPersonProtectedClaims(claim));
  }

  @Test
  public void checkAutorisationsCreateAllowedWithWildCardMix() throws ODataJPAModelException, // NOSONAR
      ODataJPAProcessException {
    final JPAClaimsPair<String> claim = new JPAClaimsPair<>("M_D*");
    createPersonProtected(createPersonProtectedClaims(claim));
  }

  @Test
  public void checkAutorisationsCreateRejectsWildCardNotMatch() throws ODataJPAModelException,
      ODataJPAProcessException {
    final JPAClaimsPair<String> claim = new JPAClaimsPair<>("D_D*");
//    final JPAExampleModifyException act = assertThrows(JPAExampleModifyException.class,
//        () -> createPersonProtected(createPersonProtectedClaims(claim)));
//    assertEquals(HttpStatusCode.FORBIDDEN.getStatusCode(), act.getStatusCode());
  }

  @Test
  public void checkAutorisationsCreateAllowedInRangeAllowsWildcard() throws ODataJPAModelException, // NOSONAR
      ODataJPAProcessException {
    final JPAClaimsPair<String> claim = new JPAClaimsPair<>("MID00", "MID99");
    createPersonProtected(createPersonProtectedClaims(claim));
  }

  @Test
  public void checkAutorisationsCreateAllowedInRangeWildcard() throws ODataJPAModelException, // NOSONAR
      ODataJPAProcessException {
    final JPAClaimsPair<String> claim = new JPAClaimsPair<>("MID0+", "MID9*");
    createPersonProtected(createPersonProtectedClaims(claim));
  }

  @Test
  public void checkAutorisationsCreateRejectRangeWilrdcardMin() throws ODataJPAModelException,
      ODataJPAProcessException {
    final JPAClaimsPair<String> claim = new JPAClaimsPair<>("MI+0*", "MID99");
    final JPAExampleModifyException act = assertThrows(JPAExampleModifyException.class,
        () -> createPersonProtected(createPersonProtectedClaims(claim)));
    assertEquals(HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), act.getStatusCode());
  }

  @Test
  public void checkAutorisationsCreateRejectRangeWilrdcarMax() throws ODataJPAModelException,
      ODataJPAProcessException {
    final JPAClaimsPair<String> claim = new JPAClaimsPair<>("MID00", "MI*99");
    final JPAExampleModifyException act = assertThrows(JPAExampleModifyException.class,
        () -> createPersonProtected(createPersonProtectedClaims(claim)));
    assertEquals(HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), act.getStatusCode());
  }

  @Test
  public void checkAutorisationsCreateAllowedTwoClaims() throws ODataJPAModelException, // NOSONAR
      ODataJPAProcessException {
    final JPAClaimsPair<String> claim1 = new JPAClaimsPair<>("MID25");
    final JPAClaimsPair<String> claim2 = new JPAClaimsPair<>("MID00");
    final JPAClaimsPair<String> claim3 = new JPAClaimsPair<>("MID99");
    final JPAODataClaimProvider claims = createPersonProtectedClaims(null);
    when(claims.get("BuildingNumber")).thenReturn(Arrays.asList(claim2, claim1, claim3));
    createPersonProtected(claims);
  }

  @Test
  public void checkAutorisationsCreateAllowedInRange() throws ODataJPAModelException, // NOSONAR
      ODataJPAProcessException {

    buildOrganizationMockForAuthorizationTest();
    data.put("id", 50);
    cut.createEntity(requestEntity, em);

  }

  @Test
  public void checkAutorisationsCreateRejectRangeToLow() throws ODataJPAModelException,
      ODataJPAProcessException {

    buildOrganizationMockForAuthorizationTest();
    data.put("id", 5);
    final JPAExampleModifyException act = assertThrows(JPAExampleModifyException.class,
        () -> cut.createEntity(requestEntity, em));
    assertEquals(HttpStatusCode.FORBIDDEN.getStatusCode(), act.getStatusCode());
  }

  @Test
  public void checkAutorisationsCreateRejectRangeToHigh() throws ODataJPAModelException,
      ODataJPAProcessException {

    buildOrganizationMockForAuthorizationTest();
    data.put("id", 500);

    final JPAExampleModifyException act = assertThrows(JPAExampleModifyException.class,
        () -> cut.createEntity(requestEntity, em));
    assertEquals(HttpStatusCode.FORBIDDEN.getStatusCode(), act.getStatusCode());
  }

  private void buildOrganizationMockForAuthorizationTest() throws ODataJPAModelException {
    final JPAEntityType et = mock(JPAEntityType.class);
    final JPAAttribute idAttribute = mock(JPAAttribute.class);
    final JPAProtectionInfo protectionInfo = mock(JPAProtectionInfo.class);
    final JPAPath path = mock(JPAPath.class);
    final Optional<JPAODataClaimProvider> claims = Optional.of(mock(JPAODataClaimProvider.class));
    final JPAClaimsPair<Integer> idClaim = new JPAClaimsPair<>(10, 100);

    when(requestEntity.getEntityType()).thenReturn(et);
    when(requestEntity.getClaims()).thenReturn(claims);

    when(claims.get().get("ID")).thenReturn(Collections.singletonList(idClaim));
    when(claims.get().user()).thenReturn(Optional.of("Willi"));

    when(path.getPath()).thenReturn(Collections.singletonList(idAttribute));

    when(et.getTypeClass()).thenAnswer(new Answer<Class<?>>() {
      @Override
      public Class<?> answer(final InvocationOnMock invocation) throws Throwable {
        return OrganizationWithAudit.class;
      }
    });
    when(et.getKey()).thenReturn(Collections.singletonList(idAttribute));
    when(et.getProtections()).thenReturn(Collections.singletonList(protectionInfo));
    when(et.getAttribute("id")).thenReturn(Optional.of(idAttribute));
    when(protectionInfo.supportsWildcards()).thenReturn(false);
    when(protectionInfo.getAttribute()).thenReturn(idAttribute);
    when(protectionInfo.getClaimName()).thenReturn("ID");
    when(protectionInfo.getPath()).thenReturn(path);
    when(idAttribute.getInternalName()).thenReturn("id");
  }

  private JPAODataClaimProvider createPersonProtectedClaims(final JPAClaimsPair<String> claim) {
    final JPAODataClaimProvider claims = mock(JPAODataClaimProvider.class);
    final JPAClaimsPair<String> userClaim = new JPAClaimsPair<>("Willi");
    when(claims.get("BuildingNumber")).thenReturn(Collections.singletonList(claim));
    when(claims.get("Creator")).thenReturn(Collections.singletonList(userClaim));
    when(claims.get("Updator")).thenReturn(Collections.singletonList(userClaim));
    return claims;
  }

  private void createPersonProtected(final JPAODataClaimProvider claims) throws ODataJPAModelException,
      ODataJPAProcessException {
    final JPAEntityType et = helper.getJPAEntityType("PersonProtecteds");
    final Map<String, Object> address = new HashMap<>();
    final Map<String, Object> inhouseAddress = new HashMap<>();
    final Map<String, Object> protectedAdminInfo = new HashMap<>();
    final Map<String, Object> changeInfo = new HashMap<>();
    doReturn(et).when(requestEntity).getEntityType();
    doReturn(Optional.ofNullable(claims)).when(requestEntity).getClaims();

    inhouseAddress.put("building", "MID25");
    address.put("inhouseAddress", inhouseAddress);
    changeInfo.put("by", "Willi");
    protectedAdminInfo.put("created", changeInfo);
    protectedAdminInfo.put("updated", changeInfo);
    data.put("iD", "100");
    data.put("inhouseAddress", address);
    data.put("protectedAdminInfo", protectedAdminInfo);

    cut.createEntity(requestEntity, em);
  }

  private Object createAdminDiv() throws ODataJPAModelException, ODataJPAProcessException {
    doReturn(helper.getJPAEntityType("AdministrativeDivisions")).when(requestEntity).getEntityType();

    data.put("divisionCode", "DE51");
    data.put("codeID", "NUTS2");
    data.put("countryCode", "DEU");
    data.put("codePublisher", "Eurostat");

    final Object act = cut.createEntity(requestEntity, em);
    return act;
  }

  private OrganizationWithAudit createOrganization() throws ODataJPAModelException, ODataJPAProcessException {
    final JPAEntityType et = mock(JPAEntityType.class);
    final JPAAttribute attribute = mock(JPAAttribute.class);
    final Optional<JPAODataClaimProvider> claims = Optional.of(mock(JPAODataClaimProvider.class));
    when(requestEntity.getEntityType()).thenReturn(et);
    when(requestEntity.getClaims()).thenReturn(claims);
    when(claims.get().user()).thenReturn(Optional.of("Willi"));
    when(et.getTypeClass()).thenAnswer(new Answer<Class<?>>() {
      @Override
      public Class<?> answer(final InvocationOnMock invocation) throws Throwable {
        return OrganizationWithAudit.class;
      }
    });
    when(et.getKey()).thenReturn(Collections.singletonList(attribute));
    when(attribute.getInternalName()).thenReturn("id");
    data.put("name1", "Example SE");
    data.put("iD", 100);

    return (OrganizationWithAudit) cut.createEntity(requestEntity, em);
  }

  private JPAUpdateResult updateOrganization() throws ODataJPAModelException, ODataJPAProcessException {
    // Create before image
    final OrganizationWithAudit beforeImage = new OrganizationWithAudit();
    beforeImage.setId(100);
    beforeImage.setCreatedAt(LocalDateTime.of(2019, 12, 24, 11, 10));
    beforeImage.setCreatedBy("Willi");
    // Create attributes that may be changed
    final JPAAttribute idAttribute = mock(JPAAttribute.class);
    when(idAttribute.getInternalName()).thenReturn("id");
    when(idAttribute.isComplex()).thenReturn(false);
    final JPAAttribute nameAttribute = mock(JPAAttribute.class);
    when(nameAttribute.getInternalName()).thenReturn("name");
    when(nameAttribute.isComplex()).thenReturn(false);
    // Create entity type
    final Optional<JPAODataClaimProvider> claims = Optional.of(mock(JPAODataClaimProvider.class));
    when(claims.get().user()).thenReturn(Optional.of("Marven"));
    final JPAEntityType et = mock(JPAEntityType.class);
    when(requestEntity.getClaims()).thenReturn(claims);
    when(requestEntity.getEntityType()).thenReturn(et);
    when(requestEntity.getBeforeImage()).thenReturn(Optional.of(beforeImage));
    when(et.getTypeClass()).thenAnswer(new Answer<Class<?>>() {
      @Override
      public Class<?> answer(final InvocationOnMock invocation) throws Throwable {
        return OrganizationWithAudit.class;
      }
    });
    when(et.getKey()).thenReturn(Collections.singletonList(idAttribute));
    when(et.getAttribute("id")).thenReturn(Optional.of(idAttribute));
    when(et.getAttribute("name")).thenReturn(Optional.of(nameAttribute));
    when(em.find(OrganizationWithAudit.class, 100)).thenReturn(beforeImage);

    data.put("name1", "Example SE");
    data.put("id", 100);
    keys.put("id", 100);

    return cut.updateEntity(requestEntity, em, HttpMethod.PATCH);
  }

  private JPAUpdateResult updateSimplePrimitiveValue() throws ODataJPAModelException, ODataJPAProcessException {
    final String id = "1";
    final Organization beforImage = new Organization(id);
    final JPAEntityType et = helper.getJPAEntityType("Organizations");
    beforImage.setName1("Example Ltd");

    doReturn(et).when(requestEntity).getEntityType();
    doReturn(beforImage).when(em).find(eq(et.getTypeClass()), any());

    data.put("name1", "Example SE");
    keys.put("iD", id);

    final JPAUpdateResult act = cut.updateEntity(requestEntity, em, HttpMethod.PATCH);
    return act;
  }
}
