package io.github.mightguy.microserivce.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Response {

  protected HttpStatus status;
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
  protected String message;
  protected Object resp;

  public Response() {
  }

  public Response(HttpStatus status, String message) {
    this.status = status;
    this.message = message;
  }

  /**
   *
   * @param status
   * @param message
   * @param resp
   */
  public Response(HttpStatus status, String message, Object resp) {
    this.status = status;
    this.message = message;
    this.resp = resp;
  }
}

