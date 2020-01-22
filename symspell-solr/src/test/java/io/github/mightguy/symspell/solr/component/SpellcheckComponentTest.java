
package io.github.mightguy.symspell.solr.component;

import io.github.mightguy.spellcheck.symspell.common.Verbosity;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.solr.SolrTestCaseJ4;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.handler.component.ResponseBuilder;
import org.apache.solr.handler.component.SearchComponent;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.junit.BeforeClass;
import org.junit.Test;

public class SpellcheckComponentTest extends SolrTestCaseJ4 {


  @BeforeClass
  public static void beforeClass() throws Exception {
    initCore("solrconfig-complete-spellcheck.xml", "schema.xml");
    assertU(adoc("id", "1", "title", "this is a title", "bind", "true"));
    assertU(adoc("id", "2", "title", "this is another title", "bind", "true"));
    assertU(adoc("id", "3", "title", "Mary had a little lamb", "bind", "false"));
    assertU(commit());
  }

  @Test
  public void testBasicInterface() throws Exception {
    //make sure the basics are in place
    assertQ(req("q", "*:*", CommonParams.DEBUG_QUERY, "true"),
        "//str[@name='rawquerystring']='*:*'",
        "//str[@name='querystring']='*:*'",
        "//str[@name='parsedquery']='MatchAllDocsQuery(*:*)'",
        "//str[@name='parsedquery_toString']='*:*'",
        "count(//lst[@name='explain']/*)=3",
        "//lst[@name='explain']/str[@name='1']",
        "//lst[@name='explain']/str[@name='2']",
        "//lst[@name='explain']/str[@name='3']",
        "//str[@name='QParser']",// make sure the QParser is specified
        "count(//lst[@name='timing']/*)=3", //should be three pieces to timings
        "//lst[@name='timing']/double[@name='time']",
        //make sure we have a time value, but don't specify its result
        "count(//lst[@name='prepare']/*)>0",
        "//lst[@name='prepare']/double[@name='time']",
        "count(//lst[@name='process']/*)>0",
        "//lst[@name='process']/double[@name='time']"
    );
  }


  @Test
  public void testReloadOnStart() throws Exception {
    NamedList args = new NamedList();
    NamedList spellchecker = new NamedList();
    spellchecker.add("deleteionWeight", 0.8f);
    spellchecker.add("insertionWeight", 1.01f);
    spellchecker.add("replaceWeight", 0.9f);
    spellchecker.add("transpositionWeight", 0.7f);
    spellchecker.add("maxEditDistance", 2.0d);
    spellchecker.add("prefixLength", 12);
    spellchecker.add("verbosity", Verbosity.ALL.name());
    spellchecker.add("field_names", "title");
    spellchecker.add("buildOnCommit", "true");
    spellchecker.add("buildOnOptimize", "true");
    spellchecker.add("unigrams_file", "unigrams.txt");
    spellchecker.add("bigrams_file", "bigrams.txt");
    spellchecker.add("chardistance_classname",
        "io.github.mightguy.spellcheck.symspell.common.QwertyDistance");
    args.add("spellcheckers", spellchecker);

    SpellcheckComponent checker = new SpellcheckComponent();
    checker.init(args);
    checker.inform(h.getCore());

    SolrQueryRequest request = req("cspellcheck.q", "filippp", "wt", "json", CommonParams.Q, "*:*",
        "qt", "/spellcheck");
    List<SearchComponent> components = new ArrayList<>();
    for (String name : h.getCore().getSearchComponents().keySet()) {
      components.add(h.getCore().getSearchComponent(name));
    }

    ResponseBuilder rb = new ResponseBuilder(request, new SolrQueryResponse(), components);
    checker.prepare(rb);

    try {
      checker.process(rb);
    } catch (NullPointerException e) {
      fail("NullPointerException due to reload not initializing analyzers");
    }

    NamedList spellcheckSuggestions = ((NamedList) rb.rsp.getValues().get("spell_suggestions"));
    boolean isCorrectlySpelled = (Boolean) spellcheckSuggestions.get("correctlySpelled");
    Map<String, String> spellCheckMap = (HashMap) spellcheckSuggestions.get("spellcheck");
    assertEquals("filippo", spellCheckMap.entrySet().iterator().next().getKey().trim());
    assertFalse(isCorrectlySpelled);
    rb.req.close();
  }

  @Test
  public void testDefaultWeightedLD() throws Exception {
    NamedList args = new NamedList();
    NamedList spellchecker = new NamedList();
    spellchecker.add("maxEditDistance", 2.0d);
    spellchecker.add("prefixLength", 12);
    spellchecker.add("verbosity", Verbosity.ALL.name());
    spellchecker.add("field_names", "title");
    spellchecker.add("buildOnCommit", "true");
    spellchecker.add("buildOnOptimize", "true");
    spellchecker.add("unigrams_file", "unigrams.txt");
    spellchecker.add("bigrams_file", "bigrams.txt");
    args.add("spellcheckers", spellchecker);

    SpellcheckComponent checker = new SpellcheckComponent();
    checker.init(args);
    checker.inform(h.getCore());

    SolrQueryRequest request = req("cspellcheck.q", "fast aod", "wt", "json", CommonParams.Q, "*:*",
        "qt", "/spellcheck");
    List<SearchComponent> components = new ArrayList<>();
    for (String name : h.getCore().getSearchComponents().keySet()) {
      components.add(h.getCore().getSearchComponent(name));
    }

    ResponseBuilder rb = new ResponseBuilder(request, new SolrQueryResponse(), components);
    checker.prepare(rb);

    try {
      checker.process(rb);
    } catch (NullPointerException e) {
      fail("NullPointerException due to reload not initializing analyzers");
    }

    NamedList spellcheckSuggestions = ((NamedList) rb.rsp.getValues().get("spell_suggestions"));
    boolean isCorrectlySpelled = (Boolean) spellcheckSuggestions.get("correctlySpelled");
    Map<String, String> spellCheckMap = (HashMap) spellcheckSuggestions.get("spellcheck");
    assertEquals("fast and", spellCheckMap.entrySet().iterator().next().getKey().trim());
    assertFalse(isCorrectlySpelled);
    rb.req.close();
  }


}

