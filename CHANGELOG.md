# Changelog

The format here is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

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
