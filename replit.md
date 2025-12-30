# Janssen Project Documentation

## Overview
The Janssen Project is an open-source digital identity infrastructure software under The Linux Foundation. This repository contains the full codebase including enterprise identity and access management components, documentation, and deployment tools.

This Replit environment is configured to serve the project documentation site.

## Project Architecture
- **Primary Language**: Java (Maven multi-module project)
- **Documentation**: MkDocs with Material theme
- **Key Components**:
  - `jans-auth-server/` - OAuth/OpenID Connect authorization server
  - `jans-fido2/` - FIDO2 passkey authentication
  - `jans-scim/` - SCIM user management API
  - `jans-config-api/` - Configuration REST APIs
  - `jans-casa/` - Self-service web portal
  - `jans-cedarling/` - Rust-based policy decision point (Cedar)
  - `jans-cli-tui/` - Text-based UI configuration tool
  - `docs/` - MkDocs documentation source

## Running the Documentation

### Development
The documentation server runs using MkDocs on port 5000:
```bash
uv run mkdocs serve --dev-addr 0.0.0.0:5000
```

### Building for Production
```bash
uv run mkdocs build
```
This generates static files in the `site/` directory.

## Dependencies
- Python 3.11+
- MkDocs with Material theme
- Various MkDocs plugins (git-revision-date, include-markdown, macros, etc.)

## Deployment
Configured as a static site deployment:
- Build command: `uv run mkdocs build`
- Public directory: `site/`
