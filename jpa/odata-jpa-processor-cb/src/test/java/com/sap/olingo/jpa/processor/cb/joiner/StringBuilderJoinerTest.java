package com.sap.olingo.jpa.processor.cb.joiner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

class StringBuilderJoinerTest {
  private static final String delimiter = "//";
  private StringBuilderJoiner<SqlConvertible> cut;
  private StringBuilder statement;
  private SqlConvertible first;

  @BeforeEach
  void setup() {
    statement = new StringBuilder();
    cut = new StringBuilderJoiner<>(statement, delimiter);

    first = mock(SqlConvertible.class);
    when(first.asSQL(statement)).thenAnswer(new AsSqlAnswer());
  }

  @Test
  void testMergeReturnsThis() {
    assertEquals(cut, cut.merge());
  }

  @Test
  void testFinishReturnsStatement() {
    assertEquals(statement, cut.finish());
  }

  @Test
  void testAddFirst() {
    assertEquals(cut, cut.add(first));
    assertEquals("Test", cut.finish().toString());
  }

  @Test
  void testAddSecond() {
    final SqlConvertible second = mock(SqlConvertible.class);
    when(second.asSQL(statement)).thenAnswer(new AsSqlAnswer());
    assertEquals(cut, cut.add(first));
    assertEquals(cut, cut.add(second));
    assertEquals("Test//Test", cut.finish().toString());
  }

  private static class AsSqlAnswer implements Answer<StringBuilder> {

    @Override
    public StringBuilder answer(final InvocationOnMock invocation) throws Throwable {
      final StringBuilder statement = invocation.getArgument(0);
      return statement.append("Test");
    }

  }
}
