package com.pfm.transaction.csvimport;

import static com.pfm.config.csv.CsvIngParserConfig.ENCODING;
import static com.pfm.helpers.TestUsersProvider.userMarian;
import static com.pfm.transaction.csvimport.CsvImportController.APPLICATION_VND_MS_EXCEL;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pfm.helpers.IntegrationTestsBase;
import java.util.stream.Stream;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

@SuppressWarnings("PMD.UnusedPrivateMethod")
class CsvImportControllerTest extends IntegrationTestsBase {

  private static final long MOCK_ACCOUNT_ID = 99;

  @MockBean
  private CsvParserService csvParserService;

  @BeforeEach
  public void setUp() throws Exception {
    userId = callRestToRegisterUserAndReturnUserId(userMarian());
    token = callRestToAuthenticateUserAndReturnToken(userMarian());
  }

  @Test
  void shouldReturnInternalServerErrorWhenSomethingGoWrongWithServer() throws Exception {
    //Given
    when(csvParserService.convertToTransactions(any())).thenThrow(TransactionsParsingException.class);
    MockMultipartFile file = new MockMultipartFile("file", "file", APPLICATION_VND_MS_EXCEL, "file".getBytes(ENCODING));

    //When
    final int status = callRestToImportTransactionsFromCsvFileAndReturnStatus(MOCK_ACCOUNT_ID, file);

    //Then
    assertThat(status, is(equalTo(HttpStatus.SC_INTERNAL_SERVER_ERROR)));
  }

  @ParameterizedTest
  @MethodSource("shouldReturnBadRequestForWrongUploadedFileContentTypeParams")
  void shouldReturnBadRequestForWrongUploadedFileContentType(String fileContentType) throws Exception {
    //Given
    MockMultipartFile file = new MockMultipartFile("file", "file", fileContentType, "file".getBytes(ENCODING));

    //When
    final int status = callRestToImportTransactionsFromCsvFileAndReturnStatus(MOCK_ACCOUNT_ID, file);

    //Then
    assertThat(status, is(equalTo(HttpStatus.SC_BAD_REQUEST)));
  }

  @Test
  void shouldReturnBadRequestForNonExistingTargetAccount() throws Exception {
    //Given
    MockMultipartFile file = new MockMultipartFile("file", "file", APPLICATION_VND_MS_EXCEL, "file".getBytes(ENCODING));
    when(csvParserService.convertToTransactions(file)).thenThrow(TargetAccountNotFoundException.class);

    //When
    final int status = callRestToImportTransactionsFromCsvFileAndReturnStatus(MOCK_ACCOUNT_ID, file);

    //Then
    assertThat(status, is(equalTo(HttpStatus.SC_BAD_REQUEST)));
    verify(csvParserService, times(1)).convertToTransactions(file);
  }

  private static Stream<Object> shouldReturnBadRequestForWrongUploadedFileContentTypeParams() {
    return Stream.of(
        Arguments.of(""),
        Arguments.of("Not valid mime type"),
        Arguments.of(MediaType.APPLICATION_ATOM_XML_VALUE),
        Arguments.of(MediaType.APPLICATION_CBOR_VALUE),
        Arguments.of(MediaType.APPLICATION_FORM_URLENCODED_VALUE),
        Arguments.of(MediaType.APPLICATION_JSON_VALUE),
        Arguments.of(MediaType.APPLICATION_OCTET_STREAM_VALUE),
        Arguments.of(MediaType.APPLICATION_PROBLEM_JSON_VALUE),
        Arguments.of(MediaType.APPLICATION_PROBLEM_XML_VALUE),
        Arguments.of(MediaType.APPLICATION_RSS_XML_VALUE),
        Arguments.of(MediaType.ALL_VALUE),
        Arguments.of(MediaType.APPLICATION_STREAM_JSON_VALUE),
        Arguments.of(MediaType.APPLICATION_XHTML_XML_VALUE),
        Arguments.of(MediaType.APPLICATION_XML_VALUE),
        Arguments.of(MediaType.IMAGE_JPEG_VALUE),
        Arguments.of(MediaType.IMAGE_PNG_VALUE),
        Arguments.of(MediaType.MULTIPART_FORM_DATA_VALUE),
        Arguments.of(MediaType.MULTIPART_MIXED_VALUE),
        Arguments.of(MediaType.TEXT_EVENT_STREAM_VALUE),
        Arguments.of(MediaType.TEXT_HTML_VALUE),
        Arguments.of(MediaType.TEXT_MARKDOWN_VALUE),
        Arguments.of(MediaType.TEXT_XML_VALUE),
        Arguments.of(MediaType.TEXT_PLAIN_VALUE),
        Arguments.of(MediaType.IMAGE_GIF_VALUE)
    );
  }

}
