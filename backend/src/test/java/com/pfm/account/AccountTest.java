package com.pfm.account;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static pl.pojo.tester.api.assertion.Assertions.assertPojoMethodsFor;

import java.math.BigDecimal;
import org.junit.Test;
import pl.pojo.tester.api.assertion.Method;

public class AccountTest {

  @Test
  public void shouldVerifyEqualsAndHashCodeAndToString() {
    // given
    final Class<?> classUnderTest = Account.class;

    // when

    // then
    assertPojoMethodsFor(classUnderTest) // TODO remove all tests testing generated code - Lombok is ignored now
        .testing(Method.TO_STRING)
        .testing(Method.EQUALS)
        .testing(Method.HASH_CODE)
        .testing(Method.SETTER)
        .areWellImplemented();
  }

  @Test
  public void shouldVerifyMissingCaseInEqualsId() {
    Account account = Account.builder().id(5L).build();
    Account other = Account.builder().id(null).build();

    assertFalse(account.equals(other));
  }

  @Test
  public void shouldVerifyMissingCaseInEqualsName() {
    Account account = Account.builder().name("mBank").build();
    Account other = Account.builder().name(null).build();

    assertFalse(account.equals(other));
  }

  @Test
  public void shouldVerifyMissingCaseInEqualsBalance() {
    Account account = Account.builder().balance(BigDecimal.ONE).build();
    Account other = Account.builder().balance(null).build();

    assertFalse(account.equals(other));
  }

  @Test
  public void shouldVerifyToString() {
    // given
    final Class<?> classUnderTest = Account.AccountBuilder.class;

    // when

    // then
    assertPojoMethodsFor(classUnderTest)
        .testing(Method.TO_STRING)
        .areWellImplemented();
  }
}
