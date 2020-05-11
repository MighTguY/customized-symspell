
package io.github.mightguy.symspell.solr.component;

import io.github.mightguy.spellcheck.symspell.api.CharDistance;
import io.github.mightguy.spellcheck.symspell.api.DataHolder;
import io.github.mightguy.spellcheck.symspell.api.SpellChecker;
import io.github.mightguy.spellcheck.symspell.api.StringDistance;
import io.github.mightguy.spellcheck.symspell.common.DictionaryItem;
import io.github.mightguy.spellcheck.symspell.common.Murmur3HashFunction;
import io.github.mightguy.spellcheck.symspell.common.SpellCheckSettings;
import io.github.mightguy.spellcheck.symspell.common.SuggestionItem;
import io.github.mightguy.spellcheck.symspell.common.Verbosity;
import io.github.mightguy.spellcheck.symspell.common.WeightedDamerauLevenshteinDistance;
import io.github.mightguy.spellcheck.symspell.exception.SpellCheckException;
import io.github.mightguy.spellcheck.symspell.impl.InMemoryDataHolder;
import io.github.mightguy.spellcheck.symspell.impl.SymSpellCheck;
import io.github.mightguy.symspell.solr.eventlistner.CustomSpellCheckListner;
import io.github.mightguy.symspell.solr.utils.Constants;
import io.github.mightguy.symspell.solr.utils.SearchRequestUtil;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.solr.common.StringUtils;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.core.SolrCore;
import org.apache.solr.handler.component.ResponseBuilder;
import org.apache.solr.handler.component.SearchComponent;
import org.apache.solr.util.plugin.SolrCoreAware;

@Slf4j
public class SpellcheckComponent extends SearchComponent implements SolrCoreAware {

  private NamedList initParams;
  @Getter
  private SpellChecker spellChecker;
  private CustomSpellCheckListner customSpellCheckListner;
  private int threshold = 0;

  public static final String COMPONENT_NAME = "custom_spellcheck";

  @Override
  public void init(NamedList args) {
    super.init(args);
    this.initParams = args;
  }

  @Override
  public void prepare(ResponseBuilder rb) throws IOException {
    SolrParams params = rb.req.getParams();
    threshold = params.getInt(Constants.SPELLCHECK_THRESHOLD, 15);
    if (!params.getBool(COMPONENT_NAME, false)) {
      return;
    }
    try {
      if (params.getBool(Constants.SPELLCHECK_BUILD, false)) {
        customSpellCheckListner.reload(rb.req.getSearcher(), spellChecker);
        rb.rsp.add("command", "build");
      }
    } catch (SpellCheckException ex) {
      log.error("Unable to build spellcheck indexes");
      throw new IOException(ex);
    }

  }

  @Override
  public void process(ResponseBuilder rb) throws IOException {
    if (!rb.req.getParams().getBool(Constants.SPELLCHECK_ENABLE, true) || SearchRequestUtil
        .resultGreaterThanThreshold(rb.rsp, threshold)) {
      log.debug("Spellcheck is disbaled either by query or result  greater than threshold [{}]",
          threshold);
      return;
    }
    SolrParams params = rb.req.getParams();
    String q = params.get(Constants.SPELLCHECK_Q, params.get(CommonParams.Q));
    boolean sow = params.getBool(Constants.SPELLCHECK_SOW, true);
    List<SuggestionItem> suggestions;
    try {
      if (sow) {
        suggestions = spellChecker.lookupCompound(q);
      } else {
        suggestions = spellChecker.lookupCompound(q, 2, false);
      }
      if (!CollectionUtils.isEmpty(suggestions)) {
        addToResponse(rb, suggestions);
      }
    } catch (SpellCheckException ex) {
      log.error("exception occured while looking for spelling suggestions");
      throw new IOException(ex);
    }

  }


  private void addToResponse(ResponseBuilder rb, List<SuggestionItem> suggestions) {
    rb.rsp.add("spell_suggestions", toNamedList(suggestions));
  }

  private NamedList toNamedList(List<SuggestionItem> suggestionItems) {
    NamedList result = new NamedList();
    if (CollectionUtils.isEmpty(suggestionItems)) {
      return result;
    }
    Map<String, String> suggestions = suggestionItems.parallelStream().collect(
        Collectors.toMap(SuggestionItem::getTerm,
            s -> s.getCount() + "," + s.getDistance() + "," + s.getScore()));

    result.add("spellcheck", suggestions);
    result.add("correctlySpelled", suggestionItems.get(0).getDistance() == 0);
    return result;
  }

  @Override
  public String getDescription() {
    return "SymSpell based Spellchecker Component";
  }

  @Override
  public void inform(SolrCore core) {
    if (initParams == null) {
      return;
    }

    log.info("Initializing spell checkers");

    if (initParams.getName(0).equals("spellcheckers")) {
      Object cfg = initParams.getVal(0);
      if (cfg instanceof NamedList) {
        addSpellChecker(core, (NamedList) cfg);
      } else if (cfg instanceof Map) {
        addSpellChecker(core, new NamedList((Map) cfg));
      } else if (cfg instanceof List) {
        for (Object o : (List) cfg) {
          if (o instanceof Map) {
            addSpellChecker(core, new NamedList((Map) o));
          }
        }
      }
    }

    log.info("Spell checker  Initialization completed");
  }

  @Override
  public Category getCategory() {
    return Category.SPELLCHECKER;
  }

  private StringDistance getStringDistance(NamedList spellchecker,
      SpellCheckSettings spellCheckSettings, SolrCore core) {

    String chardistanceClassname = SearchRequestUtil
        .getFromNamedList(spellchecker, "chardistance_classname", null);

    CharDistance charDistance = null;
    if (chardistanceClassname != null) {
      charDistance = SearchRequestUtil
          .getClassFromLoader(chardistanceClassname, core.getResourceLoader(), CharDistance.class,
              new String[0], toObjectArr());
    }

    return new WeightedDamerauLevenshteinDistance(spellCheckSettings.getDeletionWeight(),
        spellCheckSettings.getInsertionWeight(), spellCheckSettings.getReplaceWeight(),
        spellCheckSettings.getTranspositionWeight(), charDistance);
  }

  private void addSpellChecker(SolrCore core, NamedList spellcheckerNL) {

    SpellCheckSettings spellCheckSettings = SpellCheckSettings.builder()
        .deletionWeight(SearchRequestUtil.getFromNamedList(spellcheckerNL, "deleteionWeight", 1.0f))
        .insertionWeight(
            SearchRequestUtil.getFromNamedList(spellcheckerNL, "insertionWeight", 1.0f))
        .replaceWeight(SearchRequestUtil.getFromNamedList(spellcheckerNL, "replaceWeight", 1.0f))
        .transpositionWeight(
            SearchRequestUtil.getFromNamedList(spellcheckerNL, "transpositionWeight", 1.0f))
        .maxEditDistance(
            SearchRequestUtil.getFromNamedList(spellcheckerNL, "maxEditDistance", 2.0d))
        .prefixLength(SearchRequestUtil.getFromNamedList(spellcheckerNL, "prefixLength", 7))
        .verbosity(Verbosity.valueOf(
            SearchRequestUtil
                .getFromNamedList(spellcheckerNL, "verbosity", Verbosity.ALL.name())))
        .countThreshold(SearchRequestUtil.getFromNamedList(spellcheckerNL, "countThreshold", 10))
        .doKeySplit(
            SearchRequestUtil.getFromNamedList(spellcheckerNL, "createBigram", true))
        .keySplitRegex(
            SearchRequestUtil.getFromNamedList(spellcheckerNL, "bigramSplitRegex", "\\s+"))
        .build();

    StringDistance stringDistance = getStringDistance(spellcheckerNL, spellCheckSettings, core);

    DataHolder dataHolder = new InMemoryDataHolder(spellCheckSettings, new Murmur3HashFunction());

    spellChecker = new SymSpellCheck(dataHolder, stringDistance, spellCheckSettings);

    String[] fieldList = SearchRequestUtil.getFromNamedList(spellcheckerNL, "field_names", "")
        .split("\\s+");

    // Register event listeners for this SpellChecker
    customSpellCheckListner = new CustomSpellCheckListner(core, spellChecker, fieldList);
    core.registerFirstSearcherListener(customSpellCheckListner);

    String unigramsFile = SearchRequestUtil.getFromNamedList(spellcheckerNL, "unigrams_file", null);
    String bigramsFile = SearchRequestUtil.getFromNamedList(spellcheckerNL, "bigrams_file", null);
    String exclusionsFile = SearchRequestUtil
        .getFromNamedList(spellcheckerNL, "exclusions_file", null);
    String exclustionnFileSeperator = SearchRequestUtil
        .getFromNamedList(spellcheckerNL, "exclusions_file_sp", "\\s+");
    loadDefault(unigramsFile, bigramsFile, exclusionsFile, spellChecker, core,
        exclustionnFileSeperator);
    boolean buildOnCommit = Boolean.parseBoolean((String) spellcheckerNL.get("buildOnCommit"));
    boolean buildOnOptimize = Boolean.parseBoolean((String) spellcheckerNL.get("buildOnOptimize"));
    if (buildOnCommit || buildOnOptimize) {
      log.info("Registering newSearcher listener for spellChecker");
      core.registerNewSearcherListener(
          new CustomSpellCheckListner(core, spellChecker, fieldList));
    }

  }

  private void loadDefault(String unigramsFile, String bigramsFile, String exclusionsFile,
      SpellChecker spellChecker,
      SolrCore core, String exclusionListSperatorRegex) {
    try {
      if (!StringUtils.isEmpty(unigramsFile)) {
        loadUniGramFile(core.getResourceLoader().openResource(unigramsFile),
            spellChecker.getDataHolder());
      }

      if (!StringUtils.isEmpty(bigramsFile)) {
        loadBiGramFile(core.getResourceLoader().openResource(bigramsFile),
            spellChecker.getDataHolder());
      }

      if (!StringUtils.isEmpty(exclusionsFile)) {
        loadExclusions(core.getResourceLoader().openResource(exclusionsFile),
            spellChecker.getDataHolder(), exclusionListSperatorRegex);
      }

    } catch (SpellCheckException | IOException ex) {
      log.error("Error occured while loading default Configs for Spellcheck");
    }
  }

  private void loadUniGramFile(InputStream inputStream, DataHolder dataHolder)
      throws IOException, SpellCheckException {
    try (BufferedReader br = new BufferedReader(
        new InputStreamReader(inputStream))) {
      String line;
      while ((line = br.readLine()) != null) {
        String[] arr = line.split("\\s+");
        dataHolder.addItem(new DictionaryItem(arr[0], Double.parseDouble(arr[1]), -1.0));
      }
    }
  }

  private void loadBiGramFile(InputStream inputStream, DataHolder dataHolder)
      throws IOException, SpellCheckException {
    try (BufferedReader br = new BufferedReader(
        new InputStreamReader(inputStream))) {
      String line;
      while ((line = br.readLine()) != null) {
        String[] arr = line.split("\\s+");
        dataHolder
            .addItem(new DictionaryItem(arr[0] + " " + arr[1], Double.parseDouble(arr[2]), -1.0));
      }
    }
  }

  private void loadExclusions(InputStream inputStream, DataHolder dataHolder, String seperatorRegex)
      throws IOException, SpellCheckException {
    try (BufferedReader br = new BufferedReader(
        new InputStreamReader(inputStream))) {
      String line;
      while ((line = br.readLine()) != null) {
        String[] arr = line.split(seperatorRegex);
        if (arr.length == 2) {
          dataHolder.addExclusionItem(arr[0], arr[1]);
        }
      }
    }
  }

  private Object[] toObjectArr(Object... args) {
    return args;
  }

}
