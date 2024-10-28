# Changelog

The format here is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

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
