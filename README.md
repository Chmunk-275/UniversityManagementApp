# Project Overview

The objective of this project is to create a University Management System capable of role-based navigation, importing and exporting data, as well as managing faculty, students, subjects, courses, and events. The program contains interactive modules to provide a cohesive system for managing academic institutions.

## Introduction

This document provides a detailed user manual outlining the implementation steps and architectural design of the University Management Application. The document specifies details on the application's structure, its constituent components (including directories, packages, classes, and functions), and their interactions. This manual is intended for developers and technical users seeking an in-depth understanding of the application's codebase for maintenance, extension, or learning purposes. It describes the organization of the codebase, the purpose of different modules, the logic flow, and functionality of the various software components that constitute the application, ensuring all aspects of the repository structure are covered.

## Project Structure

The University Management Application follows a standard directory structure to organize its components, facilitating navigation and maintenance of the codebase.

**Root Level:** At the root of the repository, essential files and directories define the project:

| File/Directory   | Contents                              | Purpose                                              |
|------------------|---------------------------------------|------------------------------------------------------|
| `src/`           | Java source code organized into packages | Contains the main implementation logic of the application. |
| `test/`          | Java test files mirroring the `src/` structure | Holds unit tests and integration tests.             |
| `resources/`     | fxml UI files and static assets       | Stores non-Java code resources required by the application. |
| `pom.xml/`       | Project build configuration file      | Defines project dependencies, build settings, and metadata. |
| `.gitignore`     | List of files and directories to be ignored by Git | Specifies files not to be tracked by version control. |
| `README.md` (This file) | Project description, setup, usage instructions | Provides an overview and essential information.     |
