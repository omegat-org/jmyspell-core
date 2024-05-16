# fork of jmyspell-core

This repository is a forked jmyspell-core project used for OmegaT project.

## Changes from original

- Use UTF-8 for the code
- Use LF for line terminator character
- Migrate to a Gradle build system
- Fix several obvious bad coding styles
  - constant ErrorInfo.MAX_SUGGESTIONS to be final
  - constant Utils.XPRODUCT to be final
  - remove redundant final from methods
  - remove redundant public from interface
  - use `isEmpty()` for condition that compare with zero length
  - fix: define `Word#hashCode`, `Word` class already has `equals` definition
  - fix: unwanted raw types
- style: apply spotbugs
- Quality assurance by checking with SpotBugs static code analysis
- CI: test with GitHub Actions
- Support Gradle develocity scan

## License

jmyspell-core is distributed under LGPL 2.1