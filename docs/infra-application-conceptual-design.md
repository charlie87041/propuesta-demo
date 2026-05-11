# Infra Application Conceptual Design

## Purpose

`infra-application` is intended to become an internal infrastructure control plane for project-based deployments.

Its goal is not to hardcode the infrastructure of a single ecommerce application, but to manage infrastructure as a catalog of:

- projects
- environments
- applications inside each project
- shared infrastructure resources
- deployment definitions
- deployment execution history

The application is designed to run independently from the main system database and uses `SQLite` as its local persistence layer, which fits its offline-like operational model even if it is accessed through a browser.

## Core Concept

The app is centered around the idea that:

- a `Project` defines the global infrastructure context
- a `Project` contains one or more `Environment`
- a `Project` contains one or more `Application`
- shared infrastructure lives at project/environment level
- each app defines how it should be deployed inside that context
- Pulumi is the provisioning engine
- SQLite is the internal catalog and operational memory

This creates a clear distinction between:

- `desired state`: what the user defines in the app
- `deployed state`: what was actually provisioned and recorded after execution

## Domain Model

### Project

Represents the root business unit or initiative.

Responsibilities:

- define identity and ownership
- define a default AWS region
- act as the parent of environments and applications

Suggested fields:

- `id`
- `key`
- `name`
- `description`
- `defaultRegion`
- `owner`
- `status`
- `createdAt`
- `updatedAt`

### Environment

Represents a deployment context inside a project.

Responsibilities:

- separate `dev`, `staging`, `prod`
- allow overrides such as region or domain
- scope project resources and deployments

Suggested fields:

- `id`
- `projectId`
- `name`
- `region`
- `domain`
- `status`
- `createdAt`
- `updatedAt`

### Application

Represents a deployable app inside a project.

Responsibilities:

- describe the app identity
- define packaging type
- define execution target

Suggested fields:

- `id`
- `projectId`
- `key`
- `name`
- `description`
- `buildType`
- `serviceType`
- `runtime`
- `sourceLocation`
- `defaultPort`
- `status`
- `createdAt`
- `updatedAt`

Supported conceptual combinations in the first version:

- `docker + ecs`
- `docker + ec2`
- `local-code + ec2`

Unsupported initially:

- `local-code + ecs`

### ProjectResourceDefinition

Represents a shared infrastructure resource in the context of a project/environment.

Examples:

- VPC
- subnet
- security group
- S3 bucket
- ECS cluster
- IAM role
- RDS
- SQS
- SNS

Responsibilities:

- define common infrastructure once
- allow multiple applications to consume the same resource

Suggested fields:

- `id`
- `projectId`
- `environmentId`
- `resourceType`
- `name`
- `description`
- `configJson`
- `status`
- `createdAt`
- `updatedAt`

### ApplicationServiceDefinition

Represents the actual service footprint where an application runs.

Examples:

- dedicated EC2 host
- ECS service
- compute profile for a given app in a given environment

Responsibilities:

- define where the application lives
- define service-specific parameters

Suggested fields:

- `id`
- `applicationId`
- `environmentId`
- `serviceType`
- `name`
- `configJson`
- `status`
- `createdAt`
- `updatedAt`

### ApplicationResourceBinding

Links an application to shared project resources.

Examples:

- app uses bucket `assets`
- app attaches to security group `public-web`
- app depends on ECS cluster `main`

Responsibilities:

- avoid duplicating shared resources
- make application dependencies explicit

Suggested fields:

- `id`
- `applicationId`
- `environmentId`
- `projectResourceDefinitionId`
- `bindingType`
- `mountAs`
- `configJson`
- `createdAt`
- `updatedAt`

### DeploymentDefinition

Describes how an application is provisioned and delivered.

Responsibilities:

- define artifact source
- define entrypoint/startup strategy
- define deployment-specific settings

Suggested fields:

- `id`
- `applicationId`
- `environmentId`
- `buildType`
- `artifactSource`
- `entrypoint`
- `deployConfigJson`
- `status`
- `createdAt`
- `updatedAt`

### DeploymentRun

Represents a concrete execution of infrastructure operations.

Examples:

- `preview`
- `up`
- `destroy`
- `refresh`

Responsibilities:

- record audit history
- record status and timing
- capture operational results

Suggested fields:

- `id`
- `projectId`
- `environmentId`
- `applicationId`
- `operation`
- `status`
- `triggeredBy`
- `startedAt`
- `finishedAt`
- `summary`
- `logsPath`
- `errorMessage`
- `createdAt`
- `updatedAt`

### ResourceOutput

Represents the outputs produced by a deployment run.

Examples:

- public IP
- bucket name
- service URL
- ARN
- security group ID

Responsibilities:

- expose useful operational outputs
- allow the UI to show applied state

Suggested fields:

- `id`
- `deploymentRunId`
- `resourceType`
- `resourceName`
- `outputKey`
- `outputValue`
- `createdAt`
- `updatedAt`

### InfraUser

Represents a local user for the offline-style admin app.

Responsibilities:

- authenticate access to the application
- allow a single seeded user in the first version

Suggested fields:

- `id`
- `username`
- `passwordHash`
- `displayName`
- `status`
- `createdAt`
- `updatedAt`

## Architectural Separation

The conceptual architecture is split into three business areas and one shared area.

### Catalog

Owns:

- `Project`
- `Environment`
- `Application`

Purpose:

- identity
- ownership
- project organization

### Topology

Owns:

- `ProjectResourceDefinition`
- `ApplicationServiceDefinition`
- `ApplicationResourceBinding`

Purpose:

- desired infrastructure shape
- shared resources
- application dependencies

### Deployment

Owns:

- `DeploymentDefinition`
- `DeploymentRun`
- `ResourceOutput`
- Pulumi integration

Purpose:

- execution
- audit
- outputs
- provisioning lifecycle

### Auth

Owns:

- `InfraUser`
- local login/logout
- seeded access

Purpose:

- lightweight authenticated access
- no roles in the first version

## Operational Model

The intended workflow is:

1. Create a project.
2. Create environments for that project.
3. Create one or more applications inside the project.
4. Define shared resources at project/environment level.
5. Define application service targets.
6. Bind applications to shared resources.
7. Define deployment settings.
8. Run `preview`.
9. Run `up`.
10. Inspect outputs and execution history.

This keeps design-time editing separate from infrastructure execution.

## Persistence Strategy

`SQLite` is used for:

- project catalog
- application catalog
- topology definitions
- deployment history
- resource outputs
- local users

`SQLite` is not the intended source of truth for cloud state itself.

Pulumi and the target provider remain the source of truth for applied infrastructure.

## Pulumi Role

Pulumi is expected to act as the provisioning engine, not the domain model.

The domain defines the desired model.
Pulumi translates and applies it.

That means:

- the app should not directly create cloud resources ad hoc from controllers
- infrastructure execution should be centralized in the deployment area
- previews, applies, destroys and refreshes should become tracked `DeploymentRun` records

## Security Model

The first version keeps security intentionally simple:

- one local seeded user is enough
- authentication is session-based
- no role management yet
- all authenticated users are effectively operators

This matches the offline-style operational model of the app.

## UI Direction

The frontend direction agreed so far is:

- Thymeleaf server-rendered views
- Tailwind CSS for interface styling
- authenticated browser-based workflows

The primary interface is not a SPA. The main user experience should be implemented as server-rendered MVC pages using forms, redirects, flash messages and model errors.

JSON endpoints should be reserved for technical integration points, health/status checks, or asynchronous infrastructure operations where a page-level flow would be awkward.

## Implementation Guidelines

### 1. Organize the code by business context

Prefer package organization by bounded context instead of global technical layers.

Recommended high-level package areas:

- `catalog`
- `topology`
- `deployment`
- `auth`
- `shared`

Within each context, prefer keeping related pieces close together:

- `domain`
- `repository`
- `service`
- `web`

### 2. Keep desired state and deployed state separate

Do not mix editable definitions with applied outputs.

Editable definitions:

- projects
- environments
- applications
- shared resources
- service definitions
- deployment definitions

Applied state:

- deployment runs
- resource outputs

### 3. Use structured entities plus flexible JSON

The current model intentionally uses:

- strong relational structure for ownership and identity
- `configJson` fields where resource-specific variability is high

This allows fast iteration without prematurely over-normalizing resource-specific configuration.

### 4. Validate business compatibility in services

Compatibility rules should live in service/application logic, not in controllers.

Examples:

- `local-code + ecs` not allowed in v1
- environment must belong to the same project as the app
- deployment build type must match application build type
- one service definition per app/environment in v1

### 5. Keep persistence local and isolated

This app should remain independent from the main ecommerce database.

Guidelines:

- use local `SQLite`
- keep schema migrations under `infra-application`
- do not reuse the main system domain model
- avoid coupling to unrelated `common` business entities

### 6. Keep auth intentionally lightweight

For the first version:

- no user management UI
- one seeded user is enough
- password stored hashed
- session-based authentication
- all non-public routes require authentication

### 7. Treat Pulumi as infrastructure runtime, not as domain

Pulumi integration should:

- read from domain definitions
- generate infrastructure execution
- persist execution results

Pulumi should not define the business structure of the app.

### 8. Favor auditable operations

Every relevant infrastructure operation should become traceable.

Guidelines:

- create a `DeploymentRun` record for each operation
- persist summaries, timestamps and outcomes
- persist outputs separately
- never hide operational failures behind silent service calls

### 9. Design the HTML layer for gradual growth

Since the UI will use `Thymeleaf + Tailwind`:

- start with simple authenticated pages
- build a reusable layout early
- keep forms aligned with domain concepts
- avoid overloading one screen with too many infrastructure concerns
- prefer MVC controllers for user-facing workflows
- use API controllers only for technical or async operations

Suggested first UI milestones:

- login
- dashboard
- projects list/create
- project detail
- environments
- applications

### 10. Keep the first version intentionally constrained

The first version should optimize for coherence, not full AWS coverage.

Recommended constraints:

- a small list of supported resource types
- a small list of supported build/service combinations
- one local seeded operator
- local SQLite database
- deployment history tracked from day one

## Memory

This section is a running log of what has already been decided or implemented.

### Conceptual decisions

- `infra-application` is a separate Spring Boot application.
- It is intended to behave as a project-based infrastructure control plane.
- The primary UI is Thymeleaf MVC, not SPA/API-first.
- Tailwind is the styling approach for server-rendered templates.
- Project creation includes provider selection and credentials capture.
- Provider credentials are stored encrypted at rest in SQLite.
- Infrastructure is modeled around `Project -> Environment -> Application`.
- Shared infrastructure belongs to project/environment scope.
- Application-specific runtime/provisioning belongs to app scope.
- `SQLite` is the internal persistence layer.
- Pulumi is the provisioning engine.
- Tailwind will be used for Thymeleaf interfaces.
- Authentication is required, but there are no roles for now.

### Current implementation status

- `infra-application` module exists and is registered as an independent app.
- AWS SDK and Pulumi dependencies were added.
- LocalStack-related configuration was prepared.
- `SQLite`, `Flyway`, `JPA`, `Spring Security` and `Thymeleaf` dependencies were added.
- Local datasource configuration was prepared in `application.yml`.
- Flyway migration `V1__create_infra_schema.sql` was created.
- Flyway migration `V2__add_providers_and_project_credentials.sql` was added.
- Core entities were created for:
  - projects
  - environments
  - applications
  - project resource definitions
  - application service definitions
  - application resource bindings
  - deployment definitions
  - deployment runs
  - resource outputs
  - infra users
- cloud providers
- project credentials (encrypted payload)
- Spring Data repositories were created for those entities.
- Base service layer was created for:
  - project creation/listing
  - environment creation/listing
  - application creation/listing
  - project resource definitions
  - application service definitions
  - application resource bindings
  - deployment definitions
  - deployment runs
  - resource outputs
- provider catalog and seeding
- encrypted project credential persistence
- Session-based authentication was added.
- Login, logout and `me` endpoints were created.
- A seeded user mechanism was created through configuration properties.
- A seeded `aws` cloud provider mechanism was created.
- Global API error handling for the infra module was added.
- A Tailwind login page was created.
- Unauthenticated browser requests redirect to `/login`.
- A protected dashboard page was created.
- A reusable Tailwind app shell fragment was created.
- Project list and project creation pages were created as Thymeleaf MVC screens.
- Project creation now captures provider credentials from the form and stores them encrypted.
- User-facing project workflows now prefer MVC controllers and server-rendered forms.
- JSON endpoints remain available only as technical support endpoints for now.
- Project editing was added as a Thymeleaf MVC workflow.
- Projects can be disabled and enabled from the project list.
- Projects can be deleted only when they have no topology definitions, application bindings, deployment definitions, or deployment runs. Local catalog children such as credentials, apps and environments are removed with the project.
- Project provider credentials can be added or replaced from the edit screen, and they are validated as JSON before being encrypted and persisted.
- Project-specific environment management is exposed as an action inside each row of the `Projects` screen.
- `/projects/{projectId}/environments` is the primary environment management route for a selected project.
- Application management is exposed as an independent global catalog through the `Applications` submenu under `Projects`.
- The application catalog now supports create, edit, enable, disable and guarded delete flows through `/applications`.
- Archived projects are treated as read-only containers: their environments, applications, topology definitions and deployment definitions/runs cannot be created, mutated or deleted.
- Application `sourceLocation` currently accepts an existing local directory for `LOCAL_CODE` apps or a Docker repository reference for `DOCKER` apps.
- Project and application keys are normalized to lowercase before persistence.
- Application names must be unique within the same project.
- Catalog lists now use explicit ordering: projects and applications by newest first, and project environments by name.
- Project environments now support create, edit, enable, disable and guarded delete flows.
- Environment deletion is blocked when topology resources, application service definitions, resource bindings, deployment definitions or deployment runs reference it.
- Archived environments cannot receive new topology or deployment records until reactivated.

### Pending implementation areas

- review and statically validate the newly added backend code
- create Thymeleaf pages and Tailwind-based layout
- add web controllers for catalog/topology/deployment flows
- connect domain definitions to actual Pulumi execution
- persist real deployment outputs from Pulumi runs
- add richer validation around `configJson` by resource/service type
- decide the first UI flow for project onboarding
