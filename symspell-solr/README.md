# Customized SymSpell SpellCheck Solr Integration


## Usage

To Integrate with Solr there are 2 major parts
1. SpellcheckComponent
2. SpellcheckHandler


### SpellChecker Initialization

To use this spellchecker, add the `SpellcheckComponent` component in the `solrconfig.xml` 

as example:

```
<!-- Minimilasitc application -->

  <searchComponent name="spellcheck_custom"
    class="io.github.mightguy.symspell.solr.component.SpellcheckComponent">
    <lst name="spellcheckers">
      <str name="field_names">title</str>
      <int name="prefixLength">12</int>
      <str name="buildOnCommit">true</str>
      <int name="countThreshold">0</int>
    </lst>
  </searchComponent>
 ```
 
 This configuration will use Solr Index as the source of truth, to specify which all fields we can use to build the dictionary we can specify the names in comma seperated format under `field_names` attribute.
 The complete configuration params looks like as mentioned below:
 
 ```
 <!-- Full config for application -->
 
 <searchComponent name="spellcheck_custom"
       class="io.github.mightguy.symspell.solr.component.SpellcheckComponent">
       <lst name="spellcheckers">
          <float name="deleteionWeight">1.0</float>
          <float name="insertionWeight">1.0</float>
          <float name="replaceWeight">1.0<float>
          <float name="transpositionWeight">1.0</float>
          <double name="maxEditDistance">1.0</double>
          <int name="prefixLength">12</int>
          <str name="verbosity">ALL</str>
          <str name="field_names">title,text</str>
          <str name="buildOnCommit">true</str>
          <str name="buildOnOptimize">true</str>
          <str name="unigrams_file">unigrams.txt</str>
          <str name="bigrams_file">bigrams.txt</str>
          <str name="chardistance_classname">io.github.mightguy.spellcheck.symspell.common.QwertyDistance</str>
       </lst>
     </searchComponent>
 
 ```
 
 To add custom unigram and bigram dictionary, add the file names under: `unigrams_file` and `bigrams_file` respectively. 
 
### SpellcheckHandler

Spellcheck handler is the custom request handler to check what is the correct spelling of the terms, by hitting the request handler.

config is  deifned as follows: 
```
  <requestHandler name="/spellcheck"
    class="io.github.mightguy.symspell.solr.requesthandler.SpellcheckHandler">
    <arr name="first-components">
      <str>spellcheck_custom</str>
    </arr>
  </requestHandler>
```

### QueryParams for SpellcheckComponent

The query parmater for spellchecks are as follows:
1. `cspellcheck.q`->  Query term for spellcheck correction
2. `cspellcheck.threshold` -> Threshold to check if the spellcheck is required
3. `cspellcheck.enable` -> Wether to do spellcorrection or not
4. `cspellcheck.build` -> Rebuild the dictionary 
5. `cspellcheck.dataload.unigram` -> Only required in case of SpellcheckHandler, to get the count in unigrams dictionary
5. `cspellcheck.dataload.bigram` -> Only required in case of SpellcheckHandler, to get the count in bigrams dictionary
