# Requirements Document

## Introduction

Este documento especifica los requisitos para un sistema colaborativo de gestión de documentos que permite a múltiples usuarios con diferentes roles trabajar en proyectos documentales a través de un flujo de estados estructurado (edición → consulta → publicado). El sistema facilita la colaboración mediante propuestas de cambio con discusiones, versionado completo, y organización por workspaces.

## Glossary

- **System**: El sistema colaborativo de gestión de documentos
- **Workspace**: Espacio de trabajo que agrupa proyectos y usuarios con roles específicos
- **Project**: Proyecto documental con metadata, contenido, estado y roles asignados
- **Author**: Usuario que crea y gestiona un proyecto
- **Editor**: Usuario que realiza correcciones en estado de edición
- **Consultant**: Usuario que puede crear propuestas de cambio en estado de consulta
- **Change_Proposal**: Propuesta de modificación sobre una sección del documento con discusión asociada
- **Document_Version**: Versión publicada de un documento con historial completo
- **Project_State**: Estado actual del proyecto (creation, editing, consultation, published)

## Requirements

### Requirement 1: Workspace Management

**User Story:** As a user, I want to create and manage workspaces, so that I can organize projects and collaborate with other users in isolated environments.

#### Acceptance Criteria

1. WHEN a user creates a workspace, THE System SHALL create a new workspace with the user as owner
2. WHEN a workspace owner invites a user, THE System SHALL add that user to the workspace member list
3. WHEN a user is added to a workspace, THE System SHALL allow that user to view all projects within that workspace
4. THE System SHALL associate each project with exactly one workspace
5. WHEN a user accesses a workspace, THE System SHALL display all projects where the user has any role

### Requirement 2: Project Creation and Metadata

**User Story:** As an author, I want to create projects with name, description, and index, so that I can establish the structure for collaborative document development.

#### Acceptance Criteria

1. WHEN an author creates a project within a workspace, THE System SHALL create a new project with the author as owner
2. WHEN creating a project, THE System SHALL require a project name, description, and document index
3. WHEN a project is created, THE System SHALL initialize the project in "creation" state
4. THE System SHALL store the project metadata (name, description, index) and associate it with the creating author
5. WHEN a project is created, THE System SHALL generate a unique project identifier

### Requirement 3: Role Assignment

**User Story:** As an author, I want to assign editors and consultants to my project, so that I can control who can contribute at each stage of the document lifecycle.

#### Acceptance Criteria

1. WHEN an author assigns an editor role to a workspace member, THE System SHALL grant that user editor permissions for the project
2. WHEN an author assigns a consultant role to a workspace member, THE System SHALL grant that user consultant permissions for the project
3. WHEN an author removes a role from a user, THE System SHALL revoke that user's permissions for the project
4. THE System SHALL allow a user to have different roles in different projects within the same workspace
5. WHEN querying project roles, THE System SHALL return the complete list of users and their assigned roles

### Requirement 4: Initial Content Generation

**User Story:** As an author, I want to generate initial document content, so that editors and consultants have a starting point for their work.

#### Acceptance Criteria

1. WHEN an author creates initial content for a project, THE System SHALL store the content associated with the project
2. WHEN initial content is added, THE System SHALL maintain the document structure according to the defined index
3. THE System SHALL allow the author to modify initial content while the project is in "creation" state
4. WHEN content is stored, THE System SHALL preserve formatting and structure information

### Requirement 5: Project State Transitions

**User Story:** As an author, I want to transition my project through different states, so that I can control the workflow from creation to publication.

#### Acceptance Criteria

1. WHEN an author transitions a project from "creation" to "editing", THE System SHALL change the project state and enable editor access
2. WHEN an author transitions a project from "editing" to "consultation", THE System SHALL change the project state and enable consultant access
3. WHEN an author transitions a project from "consultation" to "published", THE System SHALL generate a document version and change the project state
4. THE System SHALL prevent state transitions that skip intermediate states
5. WHEN a project is in "published" state, THE System SHALL allow the author to restart the cycle by transitioning to "editing" state
6. THE System SHALL record the timestamp of each state transition

### Requirement 6: Editor Corrections

**User Story:** As an editor, I want to make corrections to the document when it's in editing state, so that I can improve the document quality before consultation.

#### Acceptance Criteria

1. WHEN a project is in "editing" state, THE System SHALL allow assigned editors to modify document content
2. WHEN an editor makes a correction, THE System SHALL record the change with editor identity and timestamp
3. WHEN a project is not in "editing" state, THE System SHALL prevent editors from modifying content
4. THE System SHALL track all editor corrections for inclusion in the version history

### Requirement 7: Change Proposal Creation

**User Story:** As a consultant, I want to select parts of the document and create change proposals, so that I can suggest improvements with collaborative discussion.

#### Acceptance Criteria

1. WHEN a project is in "consultation" state, THE System SHALL allow consultants to select document sections
2. WHEN a consultant selects a section and initiates a proposal, THE System SHALL create a new change proposal linked to that section
3. WHEN a change proposal is created, THE System SHALL initialize an empty discussion thread
4. THE System SHALL associate each change proposal with the creating consultant and the selected document section
5. WHEN a project is not in "consultation" state, THE System SHALL prevent creation of new change proposals

### Requirement 8: Change Proposal Discussion

**User Story:** As a consultant, I want to participate in discussions on change proposals, so that consultants can collaborate on refining proposed changes.

#### Acceptance Criteria

1. WHEN a consultant adds a comment to a proposal discussion, THE System SHALL append the comment with consultant identity and timestamp
2. THE System SHALL restrict discussion participation to users with consultant role on the project
3. WHEN a discussion is queried, THE System SHALL return all comments in chronological order
4. THE System SHALL allow the proposal author to view all discussion comments

### Requirement 9: Change Proposal Submission

**User Story:** As a proposal author, I want to submit a final version of my proposal, so that the project author can review and approve it.

#### Acceptance Criteria

1. WHEN a proposal author submits a final version, THE System SHALL mark the proposal as "submitted" and store the final content
2. WHEN a proposal is submitted, THE System SHALL prevent further modifications to the proposal content
3. THE System SHALL allow the proposal author to submit only if they created the proposal
4. WHEN a proposal is submitted, THE System SHALL notify the project author for review

### Requirement 10: Change Proposal Cancellation

**User Story:** As a proposal author, I want to cancel my proposal, so that I can remove proposals that are no longer relevant.

#### Acceptance Criteria

1. WHEN a proposal author cancels a proposal, THE System SHALL mark the proposal as "cancelled"
2. WHEN a proposal is cancelled, THE System SHALL preserve the proposal and discussion history
3. THE System SHALL allow only the proposal author to cancel their own proposals
4. WHEN a proposal is cancelled, THE System SHALL exclude it from the approval workflow

### Requirement 11: Change Proposal Merging

**User Story:** As a proposal author, I want to merge my proposal with another proposal, so that consultants can combine complementary suggestions.

#### Acceptance Criteria

1. WHEN a proposal author initiates a merge with another proposal, THE System SHALL send a merge request to the other proposal author
2. WHEN the other proposal author approves the merge, THE System SHALL combine both proposals into a single proposal
3. WHEN proposals are merged, THE System SHALL combine both discussion threads in chronological order
4. THE System SHALL assign both original authors as co-authors of the merged proposal
5. WHEN a merge is rejected, THE System SHALL maintain both proposals as separate entities

### Requirement 12: Change Proposal Approval and Rejection

**User Story:** As a project author, I want to approve or reject change proposals, so that I can control which changes are incorporated into the document.

#### Acceptance Criteria

1. WHEN a project author approves a submitted proposal, THE System SHALL mark the proposal as "approved" and incorporate the changes into the document
2. WHEN a project author rejects a submitted proposal, THE System SHALL mark the proposal as "rejected" and preserve the original content
3. THE System SHALL allow only the project author to approve or reject proposals
4. WHEN a proposal is approved, THE System SHALL record the approval timestamp and update the document content
5. WHEN a proposal is rejected, THE System SHALL record the rejection timestamp and reason

### Requirement 13: Document Version Generation

**User Story:** As a project author, I want to generate published versions of my document, so that I can create immutable snapshots with complete change history.

#### Acceptance Criteria

1. WHEN a project transitions to "published" state, THE System SHALL generate a new document version
2. WHEN generating a version, THE System SHALL include all editor corrections with timestamps and identities
3. WHEN generating a version, THE System SHALL include all approved change proposals with discussion history
4. WHEN generating a version, THE System SHALL include all rejected proposals with rejection reasons
5. THE System SHALL assign a sequential version number to each published version
6. WHEN a version is generated, THE System SHALL create an immutable snapshot that cannot be modified

### Requirement 14: Version History and Traceability

**User Story:** As a user, I want to view the complete history of document versions, so that I can understand how the document evolved over time.

#### Acceptance Criteria

1. WHEN a user queries version history, THE System SHALL return all published versions in chronological order
2. WHEN viewing a specific version, THE System SHALL display the complete document content as it existed at publication
3. WHEN viewing a version, THE System SHALL display all editor corrections that were applied
4. WHEN viewing a version, THE System SHALL display all change proposals (approved and rejected) with full discussion threads
5. THE System SHALL allow users to compare differences between consecutive versions

### Requirement 15: Iterative Workflow Cycles

**User Story:** As a project author, I want to repeat the editing-consultation-publication cycle, so that I can continuously improve the document through multiple iterations.

#### Acceptance Criteria

1. WHEN a project is in "published" state, THE System SHALL allow the author to transition back to "editing" state
2. WHEN starting a new cycle, THE System SHALL preserve all previous versions and their history
3. WHEN in a new cycle, THE System SHALL allow creation of new proposals independent of previous cycles
4. THE System SHALL maintain a clear association between each proposal and the cycle in which it was created
5. WHEN generating a new version, THE System SHALL increment the version number sequentially

### Requirement 16: User Authentication and Authorization

**User Story:** As a system administrator, I want to authenticate users and enforce role-based permissions, so that only authorized users can perform specific actions.

#### Acceptance Criteria

1. WHEN a user attempts to perform an action, THE System SHALL verify the user's identity
2. WHEN a user attempts to perform an action, THE System SHALL verify the user has the required role for that action
3. IF a user lacks required permissions, THEN THE System SHALL reject the action and return an authorization error
4. THE System SHALL enforce that only authors can transition project states
5. THE System SHALL enforce that only editors can modify content in "editing" state
6. THE System SHALL enforce that only consultants can create proposals in "consultation" state

### Requirement 17: Data Persistence and Integrity

**User Story:** As a system administrator, I want all data to be persisted reliably, so that no information is lost and data integrity is maintained.

#### Acceptance Criteria

1. WHEN any entity is created or modified, THE System SHALL persist the changes to the database immediately
2. WHEN a transaction fails, THE System SHALL rollback all changes to maintain data consistency
3. THE System SHALL enforce referential integrity between related entities (projects, proposals, users, workspaces)
4. WHEN deleting a workspace, THE System SHALL prevent deletion if projects exist within it
5. THE System SHALL maintain audit logs of all state transitions and approvals

### Requirement 18: Concurrent Access Handling

**User Story:** As a user, I want the system to handle concurrent edits gracefully, so that multiple users can work simultaneously without data loss.

#### Acceptance Criteria

1. WHEN multiple editors modify the same document section simultaneously, THE System SHALL detect the conflict
2. IF a conflict is detected, THEN THE System SHALL prevent the second save and notify the user
3. WHEN multiple consultants create proposals simultaneously, THE System SHALL allow all proposals to be created independently
4. THE System SHALL use optimistic locking to prevent lost updates

### Requirement 19: Notification System

**User Story:** As a user, I want to receive notifications about relevant events, so that I stay informed about project activity.

#### Acceptance Criteria

1. WHEN a project author transitions project state, THE System SHALL notify all users with roles in that project
2. WHEN a proposal is submitted, THE System SHALL notify the project author
3. WHEN a proposal is approved or rejected, THE System SHALL notify the proposal author
4. WHEN a merge request is received, THE System SHALL notify the target proposal author
5. THE System SHALL store notifications until they are marked as read by the user

### Requirement 20: Search and Filtering

**User Story:** As a user, I want to search and filter projects and proposals, so that I can quickly find relevant information.

#### Acceptance Criteria

1. WHEN a user searches by project name, THE System SHALL return all projects matching the search term where the user has access
2. WHEN a user filters by project state, THE System SHALL return only projects in the specified state
3. WHEN a user filters proposals by status, THE System SHALL return only proposals with the specified status
4. THE System SHALL support filtering projects by workspace
5. THE System SHALL support searching proposals by content or author

### Requirement 21: Gestión del Índice del Proyecto

**User Story:** As an author, I want to modify and reorganize the project index after creation, so that I can adapt the document structure as the project evolves.

#### Acceptance Criteria

1. WHEN an author modifies the index structure, THE System SHALL update the project index and preserve the change history
2. WHEN an author reorganizes index sections, THE System SHALL maintain the association between content and index sections
3. THE System SHALL allow index modifications in any project state
4. WHEN the index is modified, THE System SHALL record the timestamp and author of the change
5. THE System SHALL validate that index modifications maintain document structure integrity

### Requirement 22: Exportación de Versiones Publicadas

**User Story:** As a user, I want to export published versions to PDF and generate shareable links, so that I can distribute documents outside the system.

#### Acceptance Criteria

1. WHEN a user requests PDF export of a published version, THE System SHALL generate a PDF with preserved formatting
2. WHEN a user generates a shareable link for a version, THE System SHALL create a unique public URL
3. THE System SHALL allow the project author to control access permissions for shareable links
4. WHEN a shareable link is accessed, THE System SHALL display the version content without requiring authentication
5. THE System SHALL track access statistics for shareable links (views, downloads)

### Requirement 23: Imagen de Portada del Proyecto (Opcional)

**User Story:** As an author, I want to upload a cover image for my project, so that projects are visually identifiable in listings.

#### Acceptance Criteria

1. WHEN an author uploads a cover image, THE System SHALL store the image and associate it with the project
2. THE System SHALL validate image format (PNG, JPG, WebP) and size (max 5MB)
3. WHEN displaying project listings, THE System SHALL show the cover image if available
4. THE System SHALL allow the author to update or remove the cover image at any time
5. IF no cover image is provided, THE System SHALL display a default placeholder
