# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/), and this project adheres
to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

- ![UI] Add link to online help documentation [#217](https://github.com/jdemetra/jdplus-main/issues/217)

### Fixed

- ![IO] Fix encoding of space characters in URI [#254](https://github.com/jdemetra/jdplus-main/issues/254)
- ![IO] Fix parsing of .ods files [#309](https://github.com/jdemetra/jdplus-main/issues/309)
- ![UI] Fix default toolbar and menu [#311](https://github.com/jdemetra/jdplus-main/issues/311)
- ![UI] Fix slow dialog box when opening a file [#313](https://github.com/jdemetra/jdplus-main/issues/313)
- ![UI] Fix option show-provider-nodes cannot be reset between sessions [#292](https://github.com/jdemetra/jdplus-main/issues/292)
- ![UI] Fix missing description field [#291](https://github.com/jdemetra/jdplus-main/issues/291)
- ![UI] Fix "Copy spec. to workspace" button [#228](https://github.com/jdemetra/jdplus-main/issues/228)
- ![UI] Fix NPE in SaBatchUI renderer [#326](https://github.com/jdemetra/jdplus-main/issues/326)

### Changed

- ![UI] Replace splash screen with AI-generated image by [@jadoull](https://github.com/jadoull)

## [3.2.2] - 2024-03-14

### Added

- ![STAT] Add some kernels in linear filters (trapezoidal...)
- ![STAT] Add output in X13
- ![OTHER] Add more platform-specific packages [#226](https://github.com/jdemetra/jdplus-main/issues/226)
- ![OTHER] Add methods to simplify the serialization of spec files from external tools (R)

### Fixed

- ![STAT] Correct the computation of the likelihood in robust augmented Kalman filter in case of missing values
- ![STAT] Complete Seats computed by the Kalman smoother (add SA series)
- ![UI] Fix NPE when exporting image to the clipboard [#216](https://github.com/jdemetra/jdplus-main/issues/216)
- ![UI] Fix split-into-yearly-components action [#145](https://github.com/jdemetra/jdplus-main/issues/145)
- ![UI] Fix configure action [#215](https://github.com/jdemetra/jdplus-main/issues/215)
- ![UI] Fix drag&drop from Excel [#197](https://github.com/jdemetra/jdplus-main/issues/197)
- ![UI] Fix typo in "Kruskal-Wallis" [#203](https://github.com/jdemetra/jdplus-main/issues/203)
- ![UI] Fix missing chart export to SVG [#238](https://github.com/jdemetra/jdplus-main/issues/238)
- ![OTHER] Fix shifts in csv output

### Changed

- ![STAT] Use exact forecasts in X12 (as in J+D 2.x), instead of conditional forecasts (as in FORTRAN)
- ![UI] Modify branding to differentiate from v2
- ![UI] Simplify action enablers [#247](https://github.com/jdemetra/jdplus-main/issues/247)
- ![OTHER] Bump bundled runtime in platform-specific packages to JDK21
- ![OTHER] Improve the output dictionaries for the main algorithms

## [3.2.1] - 2023-12-07

### Fixed

- ![STAT] Provide missing final seasonal filter in X11
- ![STAT] Fix sigma vector in the X11 specification box
- ![UI] Fix missing default SA specification in the launching of the GUI
- ![UI] Fix missing local menu actions

### Added

- ![STAT] Provide additional bias correction in X11 (log-additive decomposition)

## [3.2.0] - 2023-11-23

### Changed

- ![STAT] Move seasonal component from incubator to main
- ![OTHER] Set explicit declaration of extension points
- ![OTHER] Set explicit declaration of interchangeable processors

### Fixed

- ![STAT] Fix TsData aggregation on limited data
- ![STAT] Correct raw figures for trading days effects
- ![UI] Fix action IDs to prevent conflicts
- ![IO] Fix date format in multi-document export to VTable CSV [#156](https://github.com/jdemetra/jdplus-main/issues/156)

### Added

- ![STAT] Generate forecasts in univariate state space models
- ![OTHER] Add R facilities for time series providers

## [3.1.1] - 2023-10-11

### Fixed

- ![UI] Fix startup options that fail with bundled JRE

## [3.1.0] - 2023-10-11

### Changed

- ![OTHER] DeepSelect instead of Select to read MetaData with "." in key
- ![UI] Modify menus for reference specifications

### Fixed

- ![STAT] Correct deviances in diffuse likelihood
- ![STAT] Correct covariance of ARMA parameters in models with quasi-unit roots in AR
- ![UI] Save correctly modified multiprocessing
- ![UI] Fix non-removable star on data source nodes
- ![IO] Fix NPE in grid reader when header is null

### Added

- ![STAT] Add Poisson distribution
- ![OTHER] Serialize High-frequency series (modelling part)
- ![OTHER] Add auxiliary functions to simplify the use of JD+ from R
- ![OTHER] Add specifications and auxiliary functions for linear filters
- ![OTHER] Add default modelling for seasonal adjustment (STL...)
- ![OTHER] Add handling of time varying trading days
- ![UI] Add export of SA documents to Excel

## [3.0.2] - 2023-06-14

### Changed

- ![OTHER] DeepSelect instead of Select to read MetaData with "." in key
- ![UI] Change DataSourceProviderBuddy signature to broaden use

### Fixed

- ![STAT] Correct forecasts/backcasts in the Burman algorithm (SEATS)
- ![STAT] Implement X11 without seasonal component
- ![STAT] Correct Tramo outliers detection with missing values
- ![UI] Fix missing provider info in property sheet
- ![UI] Fix missing icons in providers

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

[Unreleased]: https://github.com/jdemetra/jd3-main/compare/v3.2.2...HEAD
[3.2.2]: https://github.com/jdemetra/jd3-main/compare/v3.2.1...v3.2.2
[3.2.1]: https://github.com/jdemetra/jd3-main/compare/v3.2.0...v3.2.1
[3.2.0]: https://github.com/jdemetra/jd3-main/compare/v3.1.1...v3.2.0
[3.1.1]: https://github.com/jdemetra/jd3-main/compare/v3.1.0...v3.1.1
[3.1.0]: https://github.com/jdemetra/jd3-main/compare/v3.0.2...v3.1.0
[3.0.2]: https://github.com/jdemetra/jd3-main/compare/v3.0.1...v3.0.2
[3.0.1]: https://github.com/jdemetra/jd3-main/compare/v3.0.0...v3.0.1
[3.0.0]: https://github.com/jdemetra/jd3-main/releases/tag/v3.0.0

[STAT]: https://img.shields.io/badge/-STAT-068C09
[OTHER]: https://img.shields.io/badge/-OTHER-e4e669
[IO]: https://img.shields.io/badge/-IO-F813F7
[UI]: https://img.shields.io/badge/-UI-5319E7
