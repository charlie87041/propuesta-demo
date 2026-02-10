# CCPM Development Environment

Docker-based development environment with all dependencies for Claude Code PM workflows.



## Prerequisites

- Docker and Docker Compose installed
- Python 3.x (for initial project generation)
- GitHub account with personal access token

## Complete Setup Guide

### Step 1: Generate Claude Code Project Structure

Before using Docker, generate the Claude Code project structure:
```bash
# From the repository root
python build/claude/scripts/generate_claude.py --project-name Demo --language java
```

This creates:
- `.claude/` directory with rules, agents, and plugins
- `CLAUDE.md` project memory file
- Required configuration files

### Step 2: Run /architect in claude code for generating the project artifacts and docker files

### Step 3: Build the Docker Image

```bash
cd docker
docker-compose build
```

### Step 4: Configure Environment Variables

```bash
cp .env.example .env
```

Edit `.env` with your values:
```bash
# Required for CCPM GitHub integration
GITHUB_TOKEN=ghp_your_personal_access_token

# Git configuration
GIT_AUTHOR_NAME=Your Name
GIT_AUTHOR_EMAIL=your.email@example.com
GIT_COMMITTER_NAME=Your Name
GIT_COMMITTER_EMAIL=your.email@example.com
```

> **Getting a GitHub Token**: Go to GitHub → Settings → Developer settings → Personal access tokens → Generate new token. Required scopes: `repo`, `read:org`, `workflow`.

### Step 5: Start the Development Container

```bash
docker-compose up -d ccpm-dev
```

### Step 6: Enter the Container

```bash
docker-compose exec ccpm-dev bash
```

### Step 7: Authenticate GitHub CLI (First Time Only)

Inside the container:

```bash
# Authenticate with GitHub
gh auth login
# Choose: GitHub.com → HTTPS → Paste authentication token

# Verify authentication
gh auth status

# Install required extension for sub-issues
gh extension install yahsan2/gh-sub-issue
```

### Step 8: Initialize CCPM (`/pm:init`)

```bash
# Inside container
bash .claude/plugins/ccpm-main/ccpm/scripts/pm/init.sh
```

Expected output:
```
✅ GitHub CLI (gh) installed
✅ GitHub authenticated
✅ gh-sub-issue extension installed
✅ Directories created
```

### Step 8: Verify Setup

```bash
# Check all tools are available
java -version
gradle --version
node --version
gh --version

# Check CCPM is ready
bash .claude/plugins/ccpm-main/ccpm/scripts/pm/help.sh
```

## Quick Reference (After Initial Setup)

```bash
# Start environment
cd docker && docker-compose up -d ccpm-dev

# Enter container
docker-compose exec ccpm-dev bash

# Inside container - run CCPM commands
bash .claude/plugins/ccpm-main/ccpm/scripts/pm/prd-new.sh <feature-name>
bash .claude/plugins/ccpm-main/ccpm/scripts/pm/prd-parse.sh <feature-name>
bash .claude/plugins/ccpm-main/ccpm/scripts/pm/epic-oneshot.sh <feature-name>

# Exit and stop
exit
docker-compose down
```

## Usage Workflows

### Running CCPM Commands (TDD Planning Workflow)

```bash
# Inside container

# 1. Create PRD from existing tasks
bash .claude/plugins/ccpm-main/ccpm/scripts/pm/prd-new.sh cookies-store

# 2. Parse PRD into issues
bash .claude/plugins/ccpm-main/ccpm/scripts/pm/prd-parse.sh cookies-store

# 3. Create GitHub issues in one shot
bash .claude/plugins/ccpm-main/ccpm/scripts/pm/epic-oneshot.sh cookies-store

# 4. Start working on an issue
bash .claude/plugins/ccpm-main/ccpm/scripts/pm/issue-start.sh 1

# 5. Complete an issue
bash .claude/plugins/ccpm-main/ccpm/scripts/pm/issue-complete.sh 1
```

### Running Node.js Scripts

```bash
# Inside container
node scripts/tdd-to-prd.js artifacts/specs/cookies-store/tasks.md cookies-store
```

### Running Gradle Tasks

```bash
# Inside container
gradle build
gradle test
gradle bootRun
```

### With PostgreSQL and Redis

```bash
# Start with database services
docker-compose --profile with-db up -d

# Your services will be available at:
# - PostgreSQL: localhost:5432
# - Redis: localhost:6379
```

## Volume Mounts

| Volume | Purpose |
|--------|---------|
| `..:/workspace` | Project directory (your code) |
| `gradle-cache` | Gradle dependencies cache |
| `npm-cache` | npm packages cache |
| `gh-config` | GitHub CLI authentication |
| `git-config` | Git configuration |

## Stopping

```bash
# Stop containers
docker-compose down

# Stop and remove volumes (clean slate)
docker-compose down -v
```

## Troubleshooting

### Permission Issues

If you encounter permission issues with mounted volumes:

```bash
# Check your user ID
id -u

# Rebuild with your UID
docker-compose build --build-arg USER_UID=$(id -u) --build-arg USER_GID=$(id -g)
```

### GitHub Authentication Lost

```bash
# Re-authenticate inside container
gh auth login
```

### Gradle Cache Issues

```bash
# Clear gradle cache
docker volume rm docker_gradle-cache
docker-compose up -d ccpm-dev
```

## Customization

### Adding More Tools

Edit `Dockerfile` to add additional tools:

```dockerfile
# Example: Add Python
RUN apt-get update && apt-get install -y python3 python3-pip
```

### Changing Java Version

Modify the base image in `Dockerfile`:

```dockerfile
# For Java 17
FROM eclipse-temurin:17-jdk-jammy
```

### Changing Gradle Version

Update the `GRADLE_VERSION` environment variable:

```dockerfile
ENV GRADLE_VERSION=8.6
```

## Complete Workflow Example: Cookies Store

```bash
# 1. Generate Claude Code structure (on host)
python build/claude/scripts/generate_claude.py --project-name CookiesStore --language java

# 2. Build and start Docker environment
cd docker
docker-compose build
cp .env.example .env
# Edit .env with your GITHUB_TOKEN
docker-compose up -d ccpm-dev
docker-compose exec ccpm-dev bash

# 3. Inside container - Initialize CCPM
gh auth login
gh extension install yahsan2/gh-sub-issue
bash .claude/plugins/ccpm-main/ccpm/scripts/pm/init.sh

# 4. Run TDD planning workflow
# PRD already exists at prds/cookies-store.md
bash .claude/plugins/ccpm-main/ccpm/scripts/pm/prd-new.sh cookies-store
bash .claude/plugins/ccpm-main/ccpm/scripts/pm/prd-parse.sh cookies-store
bash .claude/plugins/ccpm-main/ccpm/scripts/pm/epic-oneshot.sh cookies-store

# 5. Start implementing first issue
bash .claude/plugins/ccpm-main/ccpm/scripts/pm/issue-start.sh 1
gradle test  # TDD: Red phase
# ... implement ...
gradle test  # TDD: Green phase
bash .claude/plugins/ccpm-main/ccpm/scripts/pm/issue-complete.sh 1
```

## Directory Structure Inside Container

```
/workspace/                          # Your mounted project
├── .claude/
│   ├── plugins/ccpm-main/          # CCPM plugin
│   ├── prds/                       # PRD files (created by /pm:init)
│   ├── epics/                      # Epic files
│   └── rules/                      # Project rules
├── artifacts/specs/                 # Design specifications
├── prds/                           # Alternative PRD location
├── docker/                         # Docker configuration
└── ...
```
