package io.github.mightguy.symspell.solr.requesthandler;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static org.hamcrest.Matchers.equalTo;

import io.github.mightguy.spellcheck.symspell.exception.SpellCheckException;
import io.github.mightguy.symspell.solr.utils.Constants;
import org.apache.solr.SolrTestCaseJ4;
import org.apache.solr.common.params.CommonParams;
import org.junit.BeforeClass;
import org.junit.Test;

public class SpellcheckHandlerTest extends SolrTestCaseJ4 {


  @BeforeClass
  public static void beforeClass() throws Exception {
    initCore("solrconfig-minimalistic-spellcheck.xml", "schema.xml");
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
  public void testQuery() throws Exception {
    assertThat(h.query("/spellcheck", req("cspellcheck.q", "titel", "wt", "json")),
        hasJsonPath("$.lookup_wordbreak.CorrectedString", equalTo("title")));

    assertThat(h.query("/spellcheck", req("cspellcheck.q", "anther tie", "wt", "json")),
        hasJsonPath("$.lookup_wordbreak.CorrectedString", equalTo("another title")));

    assertThat(h.query("/spellcheck", req("cspellcheck.q", "Maryha d litatle mamb", "wt", "json")),
        hasJsonPath("$.lookup_wordbreak.CorrectedString", equalTo("Mary had little lamb")));
  }


  @Test
  public void testExclusionList() throws Exception {
    assertThat(h.query("/spellcheck", req("cspellcheck.q", "sweetnr", "wt", "json")),
        hasJsonPath("$.lookup_wordbreak.CorrectedString", equalTo("sweetner")));

    assertThat(h.query("/spellcheck", req("cspellcheck.q", "tiatlle", "wt", "json")),
        hasJsonPath("$.lookup_wordbreak.CorrectedString", equalTo("title")));

  }


  @Test
  public void testQueryWithUnigramBigram() throws Exception {
    assertThat(h.query("/spellcheck",
        req("cspellcheck.q", "title", Constants.SPELLCHECK_DATALOAD_BIGRAM, "true",
            Constants.SPELLCHECK_DATALOAD_UNIGRAM, "true", "wt", "json")),
        hasJsonPath("$.UNIGRAM", equalTo(2.0)));
  }

  @Test(expected = SpellCheckException.class)
  public void testInvalidInputs() throws Exception {
    assertThat(h.query("/spellcheck", req("cspellcheck.q", "", "wt", "json")),
        hasJsonPath("$.lookup_wordbreak.CorrectedString", equalTo("")));
  }

  @Test(expected = NullPointerException.class)
  public void testNullInputs() throws Exception {
    assertThat(h.query("/spellcheck", req("cspellcheck.q", null, "wt", "json")),
        hasJsonPath("$.lookup_wordbreak.CorrectedString", equalTo("")));
  }

}
