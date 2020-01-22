package io.github.mightguy.symspell.solr.utils;

import org.apache.solr.common.util.NamedList;
import org.apache.solr.core.SolrResourceLoader;
import org.apache.solr.response.BasicResultContext;
import org.apache.solr.response.SolrQueryResponse;

public final class SearchRequestUtil {

  private SearchRequestUtil() {
  }

  public static <T> T getClassFromLoader(String className, SolrResourceLoader loader,
      Class abstractClass, String[] subPackages, Object[] args) {
    Object obj = loader.newInstance(className, abstractClass, subPackages, new Class[0], args);
    if (obj == null) {
      throw new FatalException("Can't load spell checker: " + className);
    }
    return (T) obj;
  }

  public static <T> T getFromNamedList(NamedList namedList, String key, T def) {
    T val = (T) namedList.get(key);
    if (val == null) {
      val = def;
    }
    return val;
  }

  public static boolean resultGreaterThanThreshold(SolrQueryResponse rsp,
      long spellCheckThreshold) {
    return !resultLessThanThreshold(rsp, spellCheckThreshold);
  }

  public static boolean resultLessThanThreshold(SolrQueryResponse rsp, long spellCheckThreshold) {
    if (null == rsp.getResponse()) {
      return true;
    }
    return ((BasicResultContext) rsp.getResponse()).getDocList().matches() < spellCheckThreshold;
  }


}
