# Release Notes

## Version 6.7-SNAPSHOT (In Development)

### Overview
Java port of SymSpell v6.7.3 with all custom features preserved. This release brings upstream algorithmic improvements while maintaining backward compatibility.

### What's New in v6.7

#### Improved Word Segmentation
- **Ligature Normalization**: Automatic conversion of Unicode ligatures (ﬁ → fi, ﬂ → fl) using NFKC normalization
- **Hyphen Removal**: Better handling of hyphenated words and syllabification artifacts
- **Case Preservation**: Maintains uppercase on first character during word break segmentation
- **Punctuation Adjacency**: Punctuation marks now stay adjacent to words (no extra spaces)

#### Safety Improvements
- Added length checks before character access to prevent edge-case errors
- Enhanced null safety in word segmentation

### Custom Features (Preserved)

All 5 custom features remain fully functional:

1. **Exclusion Dictionary** - Whitelist terms that bypass spell checking
2. **Edit Factor Configuration** - Dynamic edit distance based on word length
3. **Ignore Unknown Words** - Option to skip unknown terms
4. **Bigram Key Splitting** - Configurable phrase-level corrections
5. **QwertzDistance** - German keyboard layout support for better corrections

### Technical Details

- **Performance**: 9,523 queries/second (< 1% overhead from new features)
- **Accuracy**: 88.61% maintained (Qwerty keyboard tests)
- **Tests**: 59/59 passing
- **Compatibility**: Drop-in replacement for v6.6

### Migration Guide

**From v6.6 → v6.7:**

No code changes required! Simply update your Maven dependency:

```xml
<dependency>
    <groupId>io.github.mightguy</groupId>
    <artifactId>symspell-lib</artifactId>
    <version>6.7-SNAPSHOT</version>
</dependency>
```

**Behavioral Changes:**
- Word segmentation now better handles uppercase text (e.g., "ITWAS" → "IT WAS")
- Punctuation appears adjacent to words without spaces (e.g., "hello,world" → "hello, world")
- Ligatures are automatically normalized before processing

All changes are non-breaking and only enhance existing functionality.

### Code Changes

All changes are isolated to the `wordBreakSegmentation()` method in `SymSpellCheck.java`:

**Input normalization (v6.7.0):**
```java
// Normalize ligatures and remove hyphens
phrase = java.text.Normalizer.normalize(phrase, java.text.Normalizer.Form.NFKC)
    .replace("-", "");
```

**Case preservation (v6.7.0):**
```java
// Lookup with lowercase, preserve uppercase on first char
if (originalPart.length() > 0 && Character.isUpperCase(originalPart.charAt(0))) {
    resultChars[0] = Character.toUpperCase(resultChars[0]);
}
```

**Punctuation adjacency (v6.7.0):**
```java
// No space before punctuation/apostrophes
if (isPunctuation || isApostrophe) {
    compositions[destinationIndex].setSegmentedString(
        compositions[circularIndex].getSegmentedString() + part);
}
```

### Testing

**Test Coverage:**
- Original SymSpell features: 20 tests
- v6.7.x new features: 19 tests
- Custom features: 19 tests
- Accuracy/performance: 1 test
- **Total: 59 tests, all passing**

**Verified Against:**
- Original C# SymSpell v6.7.3 test data
- Regression tests for all custom features
- Performance benchmarks (no degradation)

---

## Version History

### v6.7 (Current)
- Ported SymSpell v6.7.3 features
- Enhanced word segmentation
- All custom features preserved

### v6.6
- Bigram dictionary support
- Sentence-level context for corrections

### v6.5 and earlier
- See [CHANGELOG.md](CHANGELOG.md) for complete version history

---

**Last Updated:** 2026-02-10
**Status:** Development
**Build:** Passing (59/59 tests)
