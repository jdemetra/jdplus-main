# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/), and this project adheres
to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Changed

- ![UI] Change DataSourceProviderBuddy signature to broaden use

### Fixed

- ![STAT] Correct forecasts/backcasts in the Burman algorithm (SEATS)
- ![STAT] Implement X11 without seasonal component
- ![STAT] Correct Tramo outliers detection with missing values
- ![UI] Fix missing provider info in property sheet

## [3.0.1] - 2023-05-11

This is a **patch release** of JDemetra+ v3.0.1.  
Its sole purpose is to simplify the installation by providing Windows-specific binaries in addition to the platform-independent packages.
These binaries are self-sufficient and therefore don't require Java anymore.

Install instructions are available at https://github.com/jdemetra/jdplus-main#installing.

### Added

- ![OTHER] Add Windows binaries to release

## [3.0.0] - 2023-05-02

This is the **initial release** of JDemetra+ v3.0.0.  
[Java SE 17 or later](https://whichjdk.com/) version is required to run it.

[Unreleased]: https://github.com/jdemetra/jd3-main/compare/v3.0.1...HEAD
[3.0.1]: https://github.com/jdemetra/jd3-main/compare/v3.0.0...v3.0.1
[3.0.0]: https://github.com/jdemetra/jd3-main/releases/tag/v3.0.0

[STAT]: https://img.shields.io/badge/-STAT-068C09
[OTHER]: https://img.shields.io/badge/-OTHER-e4e669
[IO]: https://img.shields.io/badge/-IO-F813F7
[UI]: https://img.shields.io/badge/-UI-5319E7
