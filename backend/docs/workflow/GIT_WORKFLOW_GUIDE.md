# Git Workflow Guide

> This document defines Git workflows for both small–medium teams and large teams.

---

# 📚 Table of Contents

* [Part 1 — Small/Medium Team Workflow](#part-1--smallmedium-team-workflow)

    * [Branch Naming](#branch-naming)
    * [Workflow Overview](#workflow-overview)
    * [Rules](#rules)
* [Part 2 — Large Team Workflow](#part-2--large-team-workflow)

    * [Overview](#overview)
    * [Branch Structure](#branch-structure)
    * [Sprint Lifecycle](#sprint-lifecycle)
    * [Release Flow](#release-flow)
    * [Hotfix Flow](#hotfix-flow)
    * [Pros & Cons](#pros--cons)

---

# Part 1 — Small/Medium Team Workflow

## Branch Naming

### Standard

```
feat/<JIRA-ID>/<description>
fix/<JIRA-ID>/<description>
chore/<JIRA-ID>/<description>
```

### Example

```
feat/BAM-40/dashboard
fix/BAM-30/authentication
```

---

## Workflow Overview

```
feature → PR → dev → (optional staging) → main
```

---

## Rules

### 1. Branching

* Create branch from `dev`
* No direct commits to `dev` or `main`

### 2. Before PR

* Code must be locally tested
* Branch must be updated with latest `dev`

### 3. Pull Request

* Must pass CI
* Must have at least 1 approval
* Must have no conflicts

### 4. Merge Strategy

* Merge into `dev`
* After testing → merge `dev` → `main`

### 5. Optional Staging (Recommended)

```
dev → staging → main
```

### 6. Hotfix

```
main → hotfix → main → dev
```

### 7. Conflict Handling

* Resolve conflicts in feature branch
* Do NOT resolve directly in `dev`

---

# Part 2 — Large Team Workflow

## Overview

Designed for:

* Large teams (>8 developers)
* Parallel feature development
* Strict staging control
* Sprint-based delivery

---

## Branch Structure

### Long-lived branches

* `main` → Production
* `develop` → Integration
* `stg/latest` → Staging baseline

### Sprint branches

* `Sprint.YYYY.MM`
* `Sprint.YYYY.MM-dev`

### Feature branches

```
feat/<JIRA-ID>/<description>
fix/<JIRA-ID>/<description>
hotfix/<JIRA-ID>/<description>
```

---

## Sprint Lifecycle

### 1. Sprint Start

```
git checkout stg/latest
git checkout -b Sprint.2025.08
```

* Update version
* Classify features:

    * STG Features
    * DEV-only Features

---

### 2. During Sprint

#### STG Features

```
feature → Sprint → develop
```

#### DEV-only Features

```
feature → Sprint-dev → develop
```

### Critical Rule

> NEVER merge feature directly into `develop`

---

### Conflict Handling

* Resolve conflicts ONLY in `develop`
* Keep Sprint branch clean (only staging-ready code)

---

## End of Sprint

### Ideal

```
Sprint → staging
```

### Exception

* Revert unfinished features
* Move them to next sprint

---

## Release Flow

```
staging → main
```

---

## Hotfix Flow

```
main → hotfix → main → develop
```

---

## Pros & Cons

### Advantages

* Strong staging control
* Reduced release risk
* Clear sprint structure

### Trade-offs

* More complex
* Requires discipline

---

## Summary Diagram

### Stable Features

```
feature → Sprint → develop → staging → main
```

### Unstable Features

```
feature → Sprint-dev → develop
```

---