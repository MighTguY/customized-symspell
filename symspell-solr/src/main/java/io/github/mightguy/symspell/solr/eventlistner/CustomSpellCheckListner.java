
package io.github.mightguy.symspell.solr.eventlistner;

import io.github.mightguy.spellcheck.symspell.api.SpellChecker;
import io.github.mightguy.spellcheck.symspell.common.DictionaryItem;
import io.github.mightguy.spellcheck.symspell.exception.SpellCheckException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.CharsRefBuilder;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.core.SolrCore;
import org.apache.solr.core.SolrEventListener;
import org.apache.solr.schema.FieldType;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.search.SolrIndexSearcher;

@Slf4j
public class CustomSpellCheckListner implements SolrEventListener {

  private final SolrCore core;
  private final SpellChecker checker;
  private final List<String> fieldArr;

  /**
   * Constructor for listner
   * @param core
   * @param checker
   * @param fieldArr
   */
  public CustomSpellCheckListner(SolrCore core, SpellChecker checker, String[] fieldArr) {
    this.core = core;
    this.checker = checker;
    this.fieldArr = Arrays.asList(fieldArr);
  }

  @Override
  public void postCommit() {
    // Nothing to do at post commit
  }

  @Override
  public void postSoftCommit() {
    // Nothing to do at pre commit
  }

  @Override
  public void newSearcher(SolrIndexSearcher newSearcher, SolrIndexSearcher currentSearcher) {
    // firstSearcher event Or New Searcher Event
    try {
      log.info("Loading spell index for spellchecker: "
          + checker);
      reload(newSearcher, checker);
    } catch (IOException | SpellCheckException e) {
      log.error("Exception in reloading spell check index for spellchecker: ", e);
    }
  }

  /**
   * Init method
   * @param args
   */
  @Override
  public void init(NamedList args) {
    // Nothing to do at init
  }

  /**
   * Relod method of spellcheck listner
   * @param newSearcher
   * @param checker
   * @throws IOException
   * @throws SpellCheckException
   */
  public void reload(SolrIndexSearcher newSearcher, SpellChecker checker)
      throws IOException, SpellCheckException {

    DirectoryReader productsIndexReader = newSearcher.getIndexReader();
    Fields fields = MultiFields.getFields(productsIndexReader);
    IndexSchema schema = newSearcher.getCore().getLatestSchema();
    long time = System.currentTimeMillis();
    for (String field : fields) {
      if (!fieldArr.contains(field)) {
        continue;
      }
      FieldType type = schema.getField(field).getType();
      int insertionsCount = 0;
      for (TermsEnum iterator = fields.terms(field).iterator(); iterator.next() != null; ) {
        BytesRef term = iterator.term();
        CharsRefBuilder charsRefBuilder = new CharsRefBuilder();
        type.indexedToReadable(term, charsRefBuilder);
        insertionsCount++;
        checker.getDataHolder().addItem(
            new DictionaryItem(charsRefBuilder.toString().trim(), (double) iterator.totalTermFreq(),
                0.0));
      }
      log.info("Spellcheck Dictionary populated for Field Name {}, Count {}", field,
          insertionsCount);
    }
    log.info("Data for SpellChecker  was populated. Time={} ms",
        (System.currentTimeMillis() - time));
  }

}
