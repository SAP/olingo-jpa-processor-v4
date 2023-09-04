package com.sap.olingo.jpa.processor.cb.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.persistence.Tuple;
import javax.persistence.criteria.JoinType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.cb.ProcessorCriteriaQuery;
import com.sap.olingo.jpa.processor.cb.exceptions.NotImplementedException;
import com.sap.olingo.jpa.processor.core.testmodel.Person;
import com.sap.olingo.jpa.processor.core.testmodel.Team;

class JoinTableJoinTest extends BuilderBaseTest {
  private JoinTableJoin<Person, Team> cut;
  private AliasBuilder ab;
  private CriteriaBuilderImpl cb;
  private ProcessorCriteriaQuery<Tuple> q;
  private JPAAssociationPath path;

  @BeforeEach
  void setup() throws ODataJPAModelException {
    ab = new AliasBuilder();
    cb = new CriteriaBuilderImpl(sd, new ParameterBuffer());
    q = cb.createTupleQuery();
    path = sd.getEntity(Person.class).getAssociationPath("Teams");
    cut = new JoinTableJoin<>(path, JoinType.LEFT, q.from(Person.class), ab, cb);
  }

  @Test
  void testConstructor() {
    assertNotNull(cut);
  }

  @Test
  void testGetAttributeNotImplemented() {
    assertThrows(NotImplementedException.class, () -> cut.getAttribute());
  }

  @Test
  void testHashCode() {
    assertNotEquals(0, cut.hashCode());
  }

  @Test
  void testEquals() {
    assertTrue(cut.equals(cut)); // NOSONAR
    assertFalse(cut.equals(null)); // NOSONAR
  }

  @Test
  void testJoinTypeInner() {
    assertEquals(JoinType.INNER, cut.getJoinType());
  }

  @Test
  void testGetParentReturnsInnerJoin() throws ODataJPAModelException {
    final JoinTableJoin<Person, Team> other = new JoinTableJoin<>(path, JoinType.RIGHT, q.from(Person.class), ab, cb);
    final AbstractJoinImp<?, Person> act = (AbstractJoinImp<?, Person>) cut.getParent();
    assertNotNull(act);
    assertFalse(act.equals(cut)); // NOSONAR
    assertTrue(act.equals(act)); // NOSONAR
    assertFalse(act.equals(other.getParent())); // NOSONAR
    assertNotEquals(0, act.hashCode());
    assertThrows(NotImplementedException.class, () -> act.getAttribute());
    assertEquals(JoinType.LEFT, act.getJoinType());
  }
}
