# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/), and this project adheres
to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

- ![STAT] Add support of compact notation in ISO 8601 time interval [#159](https://github.com/jdemetra/jdplus-main/issues/159)
- ![OTHER] Add Windows aarch_64 to standalone binaries [#659](https://github.com/jdemetra/jdplus-main/issues/659)

### Changed

- ![UI] Unify period display in GUI [#489](https://github.com/jdemetra/jdplus-main/issues/489)

## [3.5.1] - 2025-06-12

### Fixed

- ![UI] Fix missing handling of workspace events (save/saveas) [#602](https://github.com/jdemetra/jdplus-main/issues/602)
- ![UI] Fix flaky clipboard on Windows [#632](https://github.com/jdemetra/jdplus-main/issues/632)
- ![UI] Fix removal of single dates from calendars [#624](https://github.com/jdemetra/jdplus-main/issues/624)
- ![UI] Fix handling of SA specifications [#639](https://github.com/jdemetra/jdplus-main/issues/639)

### Added

- ![IO] Add missing items in the dictionaries (partim) [#570](https://github.com/jdemetra/jdplus-main/issues/570)
- ![UI] Add handling of priorities in multi-processing [#611](https://github.com/jdemetra/jdplus-main/issues/611)

### Changed

- ![OTHER] Migrate OSSRH to Central Portal [#641](https://github.com/jdemetra/jdplus-main/issues/641)
- ![OTHER] Migrate protobuf-maven-plugin from org.xolstice.maven.plugins to io.github.ascopes [#658](https://github.com/jdemetra/jdplus-main/issues/658)

## [3.5.0] - 2025-04-09

### Fixed

- ![STAT] Correct the handling of short series in X11
- ![STAT] Correct the handling of backcasting in X11
- ![STAT] Correct the detection of outliers with pre-specified seasonal outliers [#561](https://github.com/jdemetra/jdplus-main/issues/561)
- ![UI] Correct the revision analysis panel for short series
- ![UI] Fix missing split into yearly components feature [#550](https://github.com/jdemetra/jdplus-main/issues/550)
- ![UI] Remove DEBUG option in contextual menu [#535](https://github.com/jdemetra/jdplus-main/issues/535)
- ![UI] Fix startup crash on Java 21 [#551](https://github.com/jdemetra/jdplus-main/issues/551)
- ![UI] Fix incompatibility with plug-ins of Incubator
- ![UI] Fix a bug in the restore of specifications in multi-saprocessing [#565](https://github.com/jdemetra/jdplus-main/issues/565)

### Changed

- ![UI] Change "error" message for large differences between sa and raw annual totals to "severe" [#567](https://github.com/jdemetra/jdplus-main/issues/567)
- ![UI] Display more series in the details of the pre-processing panel (REGARIMA models) [#561](https://github.com/jdemetra/jdplus-main/issues/561)
- ![OTHER] Upgrade to last protobuf libraries

### Added

- ![IO] Add missing items in the dictionaries (partim) [#564](https://github.com/jdemetra/jdplus-main/issues/564)
- ![IO] Add (partial) reset of SA-Processing [#572](https://github.com/jdemetra/jdplus-main/issues/572)

## [3.4.0] - 2025-02-13

### Fixed

- ![STAT] Correct the refreshing of models with Julian Easter [#465](https://github.com/jdemetra/jdplus-main/issues/465)
- ![STAT] Correct a bug in the estimation of an ARIMA model using multiple starting points (X13 only, under very specific conditions)
- ![STAT] Add a missing revision policy in the cruncher [#495](https://github.com/jdemetra/jdplus-main/issues/495)
- ![IO] Use the same name for "SAProcessing" in java and R
- ![IO] Add missing information in the exports of the residuals of REGARIMA models
- ![UI] Correct the "Check Last" user interface [#511](https://github.com/jdemetra/jdplus-main/issues/511)
- ![UI] Correct the "SI-Ratio" panel (last values not shown)  [#518](https://github.com/jdemetra/jdplus-main/issues/518)
- ![OTHER] Fix Protobuf transfer of invalid statistical tests

### Added

- ![IO] Add option to CsvMatrixOutput to output the full column names instead of always outputting shortened ones [#394](https://github.com/jdemetra/jdplus-main/issues/394)

### Changed

- Modernize use of NIO API
- ![UI] Display automatically the selection box of specifications in a multi-processing window when some series are added while the default specification is not defined.

## [3.3.0] - 2024-10-21

### Fixed

- ![UI] Remove unecessary parameters in xml (X11) [#399](https://github.com/jdemetra/jdplus-main/issues/399)

### Added

- ![STAT] Add likelihood ratio test for time-varying trading days
- ![STAT] Add combined seasonality tests
- ![STAT] Add visual spectral tests
- ![UI] Add option to specify the length of the combined seasonality tests on last years visual spectral tests

### Changed

- ![STAT] Improve Canova-Hansen tests for trading days and for seasonality (add options and change R interface)

## [3.2.4] - 2024-07-11

### Fixed

- ![OTHER] Increase staging progress timeout for Maven deployment

## [3.2.3] - 2024-07-11

### Added

- ![UI] Add link to online help documentation [#217](https://github.com/jdemetra/jdplus-main/issues/217)
- ![UI] Add additional output in TramoSeats/X13 [#217](https://github.com/jdemetra/jdplus-main/issues/217)
- ![UI] Add benchmarking results in TramoSeats/X13 output [#289](https://github.com/jdemetra/jdplus-main/issues/289)

### Fixed

- ![STAT] Fix ArrayOutOfBoundsException for edge case in extreme value correction [[#368](https://github.com/jdemetra/jdplus-main/issues/368)]
- ![IO] Fix encoding of space characters in URI [#254](https://github.com/jdemetra/jdplus-main/issues/254)
- ![IO] Fix parsing of .ods files [#309](https://github.com/jdemetra/jdplus-main/issues/309)
- ![IO] Fix grid parsing when some headers are null [#328](https://github.com/jdemetra/jdplus-main/issues/328)
- ![IO] Fix reading v2 legacy WS when some user defined calendars have fixed and non-fixed coefficients [#183](https://github.com/jdemetra/jdplus-main/issues/183)
- ![IO] Fix SQL queries [#347](https://github.com/jdemetra/jdplus-main/issues/347)
- ![UI] Fix default toolbar and menu [#311](https://github.com/jdemetra/jdplus-main/issues/311)
- ![UI] Fix slow dialog box when opening a file [#313](https://github.com/jdemetra/jdplus-main/issues/313)
- ![UI] Fix option show-provider-nodes cannot be reset between sessions [#292](https://github.com/jdemetra/jdplus-main/issues/292)
- ![UI] Fix missing description field [#291](https://github.com/jdemetra/jdplus-main/issues/291)
- ![UI] Fix "Copy spec. to workspace" button [#228](https://github.com/jdemetra/jdplus-main/issues/228)
- ![UI] Fix NPE in SaBatchUI renderer [#326](https://github.com/jdemetra/jdplus-main/issues/326)
- ![UI] Fix GUI Excel output [#303](https://github.com/jdemetra/jdplus-main/issues/303)
- ![UI] Fix some problems in saving regression effects [#295](https://github.com/jdemetra/jdplus-main/issues/295)
- ![UI] Fix typos in spec dialog boxes
- ![UI] Fix handling of regression variables with lags [#323](https://github.com/jdemetra/jdplus-main/issues/323)
- ![UI] Fix numerical issues in canonical decomposition [#301](https://github.com/jdemetra/jdplus-main/issues/301)
- ![UI] Fix a bug in protobuf translation of TramoSeats results

### Changed

- ![UI] Replace splash screen with AI-generated image by [@jadoull](https://github.com/jadoull)
- ![UI] Change the layout of SA summaries (TramoSeats/X13) [#262](https://github.com/jdemetra/jdplus-main/issues/262)
- ![UI] Change the handling of the decomposition mode in X11 spec box

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

[Unreleased]: https://github.com/jdemetra/jd3-main/compare/v3.5.1...HEAD
[3.5.1]: https://github.com/jdemetra/jd3-main/compare/v3.5.0...v3.5.1
[3.5.0]: https://github.com/jdemetra/jd3-main/compare/v3.4.0...v3.5.0
[3.4.0]: https://github.com/jdemetra/jd3-main/compare/v3.3.0...v3.4.0
[3.3.0]: https://github.com/jdemetra/jd3-main/compare/v3.2.4...v3.3.0
[3.2.4]: https://github.com/jdemetra/jd3-main/compare/v3.2.3...v3.2.4
[3.2.3]: https://github.com/jdemetra/jd3-main/compare/v3.2.2...v3.2.3
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
