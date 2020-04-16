package com.pfm.filters;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.List;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

@Slf4j
@Order(2)
@Component
@SuppressWarnings("PMD.TooManyMethods")
public class LoggingFilter extends OncePerRequestFilter {

  static final String REQUEST_MARKER = "|>";
  static final String RESPONSE_MARKER = "|<";
  private static final List<MediaType> VISIBLE_TYPES = List.of(
      MediaType.valueOf("text/*"),
      MediaType.APPLICATION_FORM_URLENCODED,
      MediaType.APPLICATION_JSON,
      MediaType.APPLICATION_XML,
      MediaType.valueOf("application/*+json"),
      MediaType.valueOf("application/*+xml"),
      MediaType.MULTIPART_FORM_DATA
  );

  private void logRequestMethod(ContentCachingRequestWrapper request) {
    String queryString = request.getQueryString();
    if (queryString == null) {
      log.info("{} {} {}", REQUEST_MARKER, request.getMethod(), request.getRequestURI());
    } else {
      log.info("{} {} {}?{}", REQUEST_MARKER, request.getMethod(), request.getRequestURI(), queryString);
    }
  }

  private void logRequestHeaders(ContentCachingRequestWrapper request) {
    Collections.list(request.getHeaderNames())
        .forEach(headerName -> Collections.list(request.getHeaders(headerName))
            .forEach(headerValue ->
                log.debug("{} {}: {}", REQUEST_MARKER, headerName, headerValue)));
  }

  private void logRequestBody(ContentCachingRequestWrapper request) {
    byte[] content = request.getContentAsByteArray();
    if (content.length > 0) {
      logContent(content, request.getContentType(), request.getCharacterEncoding(), REQUEST_MARKER);
    }
  }

  private void logResponse(ContentCachingResponseWrapper response) {
    int status = response.getStatus();
    log.info("{} {} {}", RESPONSE_MARKER, status, HttpStatus.valueOf(status).getReasonPhrase());

    response.getHeaderNames().forEach(headerName ->
        response.getHeaders(headerName).forEach(headerValue -> log.info("{} {}: {}", RESPONSE_MARKER, headerName, headerValue)));

    byte[] content = response.getContentAsByteArray();
    if (content.length > 0) {
      logContent(content, response.getContentType(), response.getCharacterEncoding(), RESPONSE_MARKER);
    }
  }

  private void logContent(byte[] content, String contentType, String contentEncoding, String prefix) {
    boolean visible = false;

    if (contentType != null) {
      MediaType mediaType = MediaType.valueOf(contentType);
      visible = VISIBLE_TYPES.stream().anyMatch(visibleType -> visibleType.includes(mediaType));
    } else {
      log.warn("No content type was specified");
    }

    if (visible) {
      try {
        String contentAsString = new String(content, contentEncoding);
        log.info("{} {} {}", prefix, System.lineSeparator(), contentAsString);
      } catch (UnsupportedEncodingException e) {
        log.info("{} [{} bytes content]", prefix, content.length);
        log.warn("Not able to convert content", e);
      }
    } else {
      log.info("{} [{} bytes content]", prefix, content.length);
    }
  }

  private ContentCachingRequestWrapper wrapRequest(HttpServletRequest request) {
    if (request instanceof ContentCachingRequestWrapper) {
      return (ContentCachingRequestWrapper) request;
    } else {
      return new ContentCachingRequestWrapper(request);
    }
  }

  private ContentCachingResponseWrapper wrapResponse(HttpServletResponse response) {
    if (response instanceof ContentCachingResponseWrapper) {
      return (ContentCachingResponseWrapper) response;
    } else {
      return new ContentCachingResponseWrapper(response);
    }
  }

  @Override
  protected void doFilterInternal(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull FilterChain filterChain)
      throws ServletException, IOException {
    if (isAsyncDispatch(request)) {
      filterChain.doFilter(request, response);
    } else {
      doFilterWrapped(wrapRequest(request), wrapResponse(response), filterChain);
    }
  }

  private void doFilterWrapped(ContentCachingRequestWrapper request, ContentCachingResponseWrapper response, FilterChain filterChain)
      throws ServletException, IOException {
    try {
      beforeRequest(request);
      filterChain.doFilter(request, response);
    } finally {
      afterRequest(request, response);
      response.copyBodyToResponse();
    }
  }

  private void beforeRequest(ContentCachingRequestWrapper request) {

    if (log.isInfoEnabled()) {
      logRequestMethod(request);
    }

    if (log.isDebugEnabled()) {
      logRequestHeaders(request);
    }
  }

  private void afterRequest(ContentCachingRequestWrapper request,
      ContentCachingResponseWrapper response) {

    if (log.isInfoEnabled()) {
      logRequestBody(request);
      logResponse(response);
    }
  }
}
