<div align="center">
    <a href="https://plugins.jetbrains.com/plugin/22746-flutter-arb-action">
        <img src="./src/main/resources/META-INF/pluginIcon.svg" width="320" height="320" alt="logo"/>
    </a>
</div>
<h1 align="center">Flutter Arb Action</h1>
<p align="center">Plugin for IntelliJ-based IDEs (Android Studio).</p>

<p align="center">
<a href="https://www.apache.org/licenses/LICENSE-2.0"><img src="https://img.shields.io/badge/License-Apache_2.0-yellow.svg"></a>
<a href="https://plugins.jetbrains.com/plugin/22746-flutter-arb-action"><img src="https://img.shields.io/jetbrains/plugin/r/rating/22746-flutter-arb-action"></a>
<a href="https://plugins.jetbrains.com/embeddable/install/22746"><img src="https://img.shields.io/jetbrains/plugin/d/22746-flutter-arb-action.svg?style=flat-square"></a>
<a href="https://plugins.jetbrains.com/plugin/22746-flutter-arb-action"><img src="https://img.shields.io/jetbrains/plugin/v/22746-flutter-arb-action.svg?style=flat-square"></a>
</p>

# Table of contents

- [Core Features](#core-features)
- [Compatibility](#compatibility)
- [Install](#install)
- [Screenshots](#screenshots)

## Core features

<li>Detects arb file based on l10n.yaml file</li>
<li>Moves strings to arb file</li>
<li>Supports interpolated string</li>
<li>Simple completion system based on currently entered keys in arb file</li>
<li>Sorts arb file</li>

## Compatibility

IntelliJ IDEA, Android Studio

## Install

<a href="https://plugins.jetbrains.com/embeddable/install/22746">
    <img src="./screenshots/get_from_marketplace.png" width="300"/>
</a>

Or you could install it inside your IDE:

For Windows & Linux - <kbd>File</kbd> > <kbd>Settings</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd> > <kbd>Search for "Flutter_arb_action"</kbd> > <kbd>Install Plugin</kbd> > <kbd>Restart IntelliJ IDEA</kbd>

For Mac - <kbd>IntelliJ IDEA</kbd> > <kbd>Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd> > <kbd>Search for "Flutter_arb_action"</kbd> > <kbd>Install Plugin</kbd>  > <kbd>Restart IntelliJ IDEA</kbd>

## Screenshots

### Extension

<img width="640" alt="extension" src="./screenshots/extension.png">

### Configuration (l10n.yaml)

<img alt="settings" src="./screenshots/configuration.png">

### Quick Fixes for String

<img alt="quick_fixes" src="./screenshots/quick_fixes.png">

### Move to arb dialog

<img alt="dialog" src="./screenshots/dialog.png">
<img alt="dialog_completion" src="./screenshots/dialog_completion.png">

### Results

<img width="640" alt="dart_file" src="./screenshots/result_dart_file.png">
<img alt="dart_file" src="./screenshots/result_arb_file.png">