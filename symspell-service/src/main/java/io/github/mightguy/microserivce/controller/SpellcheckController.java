package io.github.mightguy.microserivce.controller;

import io.github.mightguy.microserivce.model.Response;
import io.github.mightguy.microserivce.service.SpellcheckService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Api(
    tags = {"Edit Distance"}
)
@RequestMapping("/distance")
public class SpellcheckController {

  @Autowired
  SpellcheckService service;

  /**
   *
   * @param sourceWord
   * @param targetWord
   * @param maxEd
   * @return
   */
  @ApiOperation(
      value = "API for calculating the Edit  Distance",
      notes =
          "For given source and target String calculate the Edit Distance",
      code = 200,
      response = Response.class)
  @ApiResponses(
      value = {
          @ApiResponse(
              code = 400,
              message = "SpellCheckExceptions",
              response = Response.class),
          @ApiResponse(code = 200, response = Response.class, message = "")
      })
  @GetMapping("/spellcheck/")
  public Response getEditDistance(
      @Valid @RequestParam(name = "source") String sourceWord,
      @Valid @RequestParam(name = "target") String targetWord,
      @RequestParam(name = "maxED", required = false) Double maxEd) {
    if (maxEd != null) {
      return service.getEditDistance(sourceWord, targetWord, maxEd);
    }
    return service.getEditDistance(sourceWord, targetWord);
  }
}
