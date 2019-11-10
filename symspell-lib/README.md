# Customized SymSpell SpellCheck Lib

## Usage

The SypellChecker Object requires 
1. SpellCheckSettings
2. DataHolder
3. StringDistance

### SpellCheckSettings Initialization
```
SpellCheckSettings spellCheckSettings = SpellCheckSettings.builder()
        .countThreshold(1)
        .deletionWeight(1)
        .insertionWeight(1)
        .replaceWeight(1)
        .maxEditDistance(2)
        .transpositionWeight(1)
        .topK(5)
        .prefixLength(10)
        .verbosity(Verbosity.ALL).build();
```

If we need to use Default IMPL then use
```
SpellCheckSettings spellCheckSettings = SpellCheckSettings.builder()
       .build();
```

### DataHolder Initialization
```
DataHolder dataHolder = new InMemoryDataHolder(spellCheckSettings, new Murmur3HashFunction());
```

### StringDistance Initialization
```
StringDistance weightedDamerauLevenshteinDistance =
    new WeightedDamerauLevenshteinDistance(
        spellCheckSettings.getDeletionWeight(),
        spellCheckSettings.getInsertionWeight(),
        spellCheckSettings.getReplaceWeight(),
        spellCheckSettings.getTranspositionWeight(),
        new QwertyDistance());
```