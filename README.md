# Dart Scripts Runner

![Downloads](https://img.shields.io/jetbrains/plugin/d/18726-dart-scripts-runner)
![Version](https://img.shields.io/jetbrains/plugin/v/18726-dart-scripts-runner)
[![License](https://img.shields.io/badge/license-Apache%202-blue.svg)](LICENSE)

IntelliJ plugin to run scripts on pubspec.yaml for Dart projects

## Installation

Press `Ctrl+Alt+S` to open the IDE settings and select Plugins.

Search `Dart Scripts Runner` in the Marketplace and click `Install`.

![Install plugin](docs/images/install-plugin.png)

## Using

Write your custom script in the `scripts` property of the `pubspec.yaml` file.

![Run script on pubspec.yaml](docs/images/pubspec-scripts-running.png)

Dart Scripts Runner will add a run button on each script line.
Click that button to run the script.

![Edit run configuration](docs/images/dart-script-configuration.png)

## Example

```yaml
scripts:
  # Without options:
  #   directory: <project directory>
  #   terminal:  false
  pub_get: flutter pub get

  # With options
  pod_install:
    script: pod install   # Script text                        (required)
    directory: ./ios      # Working directory                  (default: <project directory>)
    terminal: true        # Should execute script in terminal? (default: false)
```
