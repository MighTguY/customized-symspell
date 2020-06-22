package io.github.mightguy.microserivce.service;

import io.github.mightguy.microserivce.config.SpellcheckConfiguration;
import io.github.mightguy.microserivce.model.Response;
import io.github.mightguy.spellcheck.symspell.api.CharDistance;
import io.github.mightguy.spellcheck.symspell.api.StringDistance;
import io.github.mightguy.spellcheck.symspell.common.QwertyDistance;
import io.github.mightguy.spellcheck.symspell.common.QwertzDistance;
import io.github.mightguy.spellcheck.symspell.common.WeightedDamerauLevenshteinDistance;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class SpellcheckService {

  StringDistance stringDistance;
  SpellcheckConfiguration configuration;
  QwertyDistance qwertyDistance = new QwertyDistance();
  QwertzDistance qwertzDistance = new QwertzDistance();


  /**
   *
   * @param configuration
   */
  @Autowired
  public SpellcheckService(SpellcheckConfiguration configuration) {
    this.configuration = configuration;
    stringDistance = new WeightedDamerauLevenshteinDistance(configuration.getDeletionWeight(),
        configuration.getInsertionWeight(), configuration.getReplaceWeight(),
        configuration.getTranspositionWeight(),
        getCharDistanceObject(configuration.getCharDistance()));
  }

  @PostConstruct
  public void init() {

  }

  public Response getEditDistance(String source, String target, double maxED) {
    double distance = stringDistance.getDistance(source, target, maxED);
    return generateResponse(source, target, distance);
  }

  /**
   *
   * @param source
   * @param target
   * @return
   */
  public Response getEditDistance(String source, String target) {
    double distance = stringDistance.getDistance(source, target);
    return generateResponse(source, target, distance);
  }

  /**
   *
   * @param source
   * @param target
   * @param distance
   * @return
   */
  public Response generateResponse(String source, String target, double distance) {
    Map<String, Object> responseMap = new HashMap<>();
    responseMap.put("Source", source);
    responseMap.put("Target", target);
    responseMap.put("Edit Distance", distance);
    responseMap.put("Configurtion", configuration.toString());
    return new Response(HttpStatus.OK, "Sucess", responseMap);
  }

  private CharDistance getCharDistanceObject(String algorithm) {
    CharDistance charDistance = null;
    if ("QWERTY".equals(algorithm)) {
      return qwertyDistance;
    }
    if ("QWERTZ".equals(algorithm)) {
      return qwertzDistance;
    }
    return null;
  }


}
