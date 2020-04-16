package com.pfm.transaction.csvimport;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class CsvParserTest {

  @Mock
  private MultipartFile multipartFile;

  @InjectMocks
  private CsvParser csvParser;

  @Test
  void shouldThrowIoExceptionWhenParserThrowsIoException() throws IOException {
    //Given
    Mockito.when(multipartFile.getInputStream()).thenThrow(IOException.class);

    //Then
    assertThrows(TransactionsParsingException.class, () -> csvParser.parse(multipartFile));
  }

}
