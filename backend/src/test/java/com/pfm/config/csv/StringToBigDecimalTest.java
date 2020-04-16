package com.pfm.config.csv;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@SuppressWarnings("PMD.UnusedPrivateMethod")
class StringToBigDecimalTest {

  @ParameterizedTest
  @MethodSource("testInputProvider")
  void shouldConvertStringToBigDecimal(String input, BigDecimal expected) {
    //Given

    //When
    final BigDecimal actual = StringToBigDecimal.convert(input);

    //Then
    assertThat(actual, is(equalTo(expected)));
  }

  private static Stream<Object> testInputProvider() {
    return Stream.of(
        Arguments.of("1.0", getBigDecimal(1.00)),
        Arguments.of("  1.0", getBigDecimal(1.00)),
        Arguments.of("  1,0", getBigDecimal(1.00)),
        Arguments.of("  1,04  ", getBigDecimal(1.04)),
        Arguments.of("  1,044  ", getBigDecimal(1.04)),
        Arguments.of("  1,045  ", getBigDecimal(1.05)),
        Arguments.of("  1 ", getBigDecimal(1.00))
    );
  }

  private static BigDecimal getBigDecimal(double value) {
    return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP);
  }

}
