# Changelog

The format here is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [2.0] - 2026-10-25
This release changes the API, so code written for 1.x needs updating.
With streams, `new CSSMinifier(reader).minify(writer)` becomes
`new CSSMinifier().minify(reader, writer)`, and the same goes for
`JSMinifier`, but the new method closes neither stream.

### Added
- Added an `Automatic-Module-Name` to the jar's manifest, so on the
  module path, its module is `net.logicsquad.minifier`, rather than a
  name derived from the jar's file name. #51

### Changed
- Replaced the `Reader`/`Writer` API with
  `String minify(String source)`. `CSSMinifier` and `JSMinifier` have
  no-argument constructors and keep no state between calls, so one
  instance can minify any number of resources, from any number of
  threads. For streams, `Minifier` has a default
  `minify(Reader, Writer)`, which writes nothing unless minification
  succeeds. #32
- `MinificationException` now says what went wrong, and where, as in
  `Unterminated comment at line 12, column 5`, and `line()` and
  `column()` return the position. It no longer wraps an `IOException`,
  or one of the minifiers' own exceptions, which have gone. #33
- Removed the line break at the start of `JSMinifier`'s output, and
  the `\r\n` at the end of `CSSMinifier`'s. #34
- Made `CSSMinifier` and `JSMinifier` final. #48
- Both minifiers now keep comments that start with `/*!`, which mark a
  licence or copyright notice, and remove all others, including the
  `/**` comments that `CSSMinifier` used to keep. #50

### Removed
- Removed `AbstractMinifier`, and the minifiers' constructors that take
  a `Reader`. #32
- Removed `CSSMinifier`'s debug logging, and with it the dependency on
  `slf4j-api`, so minifier has no runtime dependencies. #49

### Fixed
- Fixed a failed minification leaving partial output behind, and
  `CSSMinifier` ignoring errors writing its output. #32
- Fixed `CSSMinifier` dropping a kept comment after a rule's last
  declaration, or in an otherwise empty rule. #50

## [1.7] - 2026-10-25
### Fixed
- Fixed JavaScript operators running together, which could change
  what they mean: `b + ++c` became `b+++c`, which is `(b++) + c`. #40
- Fixed tabs in JavaScript strings, regular expressions and template
  literals becoming spaces, and line continuations in strings failing
  with `\r\n` line breaks. #41
- Fixed template literals that span lines, which failed to minify, and
  template literals nested in a substitution, which were minified as
  code. #42
- Fixed JavaScript statements that only a line break separates
  running together, before `!`, `~` or a string, or after a template
  literal. #43
- Fixed a regular expression literal running into the word after it,
  which was then read as its flags. #44
- Fixed a `/` in a character class ending a regular expression
  literal. #45
- Fixed regular expression literals being taken for division, as
  after `return`, `=>` or the condition of an `if`, and division at
  the start of a line being taken for a regular expression literal.
  #46
- Fixed U+FFFF being added to the end of JavaScript that ends in `/`.
  #47

## [1.6] - 2026-10-24
### Changed
- Updated `central-publishing-maven-plugin` to 0.12.0, since Maven
  Central rejects the bundles that 0.11.0 builds with Maven 3.10.

### Fixed
- Fixed dropping of empty `@layer` blocks, which still set the layer
  order. #30
- Fixed dropping of statements, such as `@import` and `@layer`, and
  kept comments in front of an empty rule. #31
- Fixed dropping of at-rule statements inside a block, such as
  `@layer` statements, which set the layer order. #35
- Fixed minification making a misplaced `@import` or `@namespace` rule
  valid, by dropping an empty rule in front of it. #36
- Fixed reordering of declarations, which could change which value
  wins: they are no longer sorted by property name, or moved after
  nested rules. #37
- Fixed minification making an ignored `@charset` rule valid, which
  could change how browsers decode the stylesheet. #38
- Fixed the tokens on either side of a removed comment running
  together, as in `0/**/auto`, which became `0auto`. #39

## [1.5] - 2026-09-24
### Changed
- Values are no longer lowercased (except six-digit hex colours),
  since some are case-sensitive, such as custom property values and
  animation, counter, grid area and container names. #23

- Tidied up the POM, removing unused site, reporting and PMD
  configuration.

### Fixed
- Fixed lowercasing of strings and `url()` values, which corrupted
  data URIs. #16
- Fixed replacement of `font-weight: bolder` and `lighter` with fixed
  weights. #17
- Fixed collapsing of `background-position` values, such as `0 0` to
  `0`. #18
- Fixed corruption of eight-digit hex colours, and of `rgb()` values
  with an alpha channel. #19
- Fixed conversion of `rgb()` values with components above 255 to
  invalid hex colours. #20
- Fixed collapsing of repeated values, such as `3px 3px` to `3px`, in
  properties where that changes their meaning. #21
- Fixed simplifications being applied inside strings and `url()`
  values. #22
- Fixed alteration of strings in selectors and `@import` rules. #24
- Fixed parsing of kept `/** … */` comments as CSS, and of `/*` in
  strings as a comment. #25
- Fixed dropping of anything after the last rule, such as a kept
  comment. A stylesheet made up only of `@import` rules no longer
  minifies to nothing. #26
- Fixed dropping of an unclosed rule at the end of the input. #27
- Fixed unclosed strings and `url()` values at the end of the
  input. #28
- Fixed deletion of line breaks, which could run tokens together; they
  are now treated as whitespace. #29

## [1.4] - 2026-08-09
### Added
- Now supports nested rules. #11

### Changed
- Removed `log4j2.properties`. #12
- Improved exception handling in some edge cases. #13

### Fixed
- Fixed minification of template literals. #8
- Fixed mis-parsing of single-quoted strings and `url()` values
  containing `)`. #14
- Fixed over-stripping of quotes in `url()`. #15

## [1.3] - 2024-10-28
### Fixed
- Fixed an edge case where a valid comment syntax was being flagged as
  unterminated. #4
- Fixed over-simplification of parameters to grid-template-columns,
  grid-template-rows. #7

## [1.2] - 2023-12-14
### Fixed
- Toned down some overly-aggressive whitespace removal. #5

## [1.1] - 2023-07-06
### Changed
- Updated some dependency versions (Surefire, JUnit).

### Fixed
- Changed `log4j-slf4j-impl` → `slf4j-api` (but adds
  `log4j-slf4j2-impl` for unit tests).
- Fixed issue with over-eager property truncation, leading to
  occasional invalid CSS.

## [1.0] - 2021-12-29
### Changed
- Updated some dependency versions.

## [0.2] - 2021-01-10
### Added
- Updated `README.md` with instructions for getting started.
- Added missing `pmd.version` property to POM.
- Improved exception handling in `JSMinifier` and `CSSMinifier`.

## [0.1] - 2021-01-01
Initial release.
