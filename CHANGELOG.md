# Changelog

All notable changes to the Customized SymSpell project.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [6.7-1] 

### Added
- **Ligature normalization** - Automatic Unicode NFKC conversion (ﬁ → fi)
- **Case preservation** - Maintains uppercase on first character in word segmentation
- **Hyphen removal** - Better handling of syllabification artifacts
- **Punctuation adjacency** - Punctuation stays adjacent to words
- **Safety checks** - Length validation in character operations
- **Test suite** - 19 new tests for v6.7 features (59 total, all passing)

### Changed
- Enhanced `wordBreakSegmentation()` method with v6.7 improvements
- Improved accuracy for mixed-case input and special characters

### Performance
- Maintained: 9,523 queries/second
- Accuracy: 88.61% (Qwerty keyboard distance)
- Overhead: < 1% from new features

### Preserved
All 5 custom features remain fully functional:
- Exclusion Dictionary
- Edit Factor Configuration
- Ignore Unknown Words
- Bigram Key Splitting
- QwertzDistance (German keyboard)

## [6.6] - Previous Release

### Features
- Java port of SymSpell v6.6
- Weighted Damerau-Levenshtein distance
- Keyboard-based distance (Qwerty/Qwertz)
- Bigram dictionary support
- Compound word correction
- Word segmentation
- Custom exclusion dictionary

---

**For detailed release information, see [ReleaseNotes.md](ReleaseNotes.md)**
