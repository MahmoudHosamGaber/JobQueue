# Job Queue System - Design Spec
## Purpose

The system has 2 parts producers and consumers. The producer recieves job requests and routes them to consumers. Consumers request a job, process it, and submit a result.

## Producer

### Domain model

Job
* id — unique identifier
* payload — work description (string)
* state — enum: PENDING → IN_PROGRESS → SUCCEEDED
* result — arbitrary JSON (optional)

Valid transitions
* create → PENDING
* request-next → IN_PROGRESS
* submit-result → SUCCEEDED
* Invalid transitions must be rejected (e.g., submitting when not IN_PROGRESS).

### API

POST /jobs
* Request body: { "payload": "<string>" }
* Success: 201 Created, body: JobDTO (see DTOs); Location: /jobs/{id}
* Errors: 400 for invalid input, 500 for DB errors

GET /jobs/{id}
* Success: 200 OK, body: JobDTO
* Errors: 404 Not Found

POST /jobs/next
* Consumer asks for the next available job.
* Behavior: if no pending jobs → 204 No Content. Otherwise the next pending job is assigned to the caller: its state becomes IN_PROGRESS and a JobResponse (id, payload) is returned with 200 OK.
* Caller must call the submit endpoint to complete the job.

POST /jobs/{id} (submit result)
* Request body: { "result": <any JSON> }
* Behavior: if job is IN_PROGRESS, store result, set SUCCEEDED, return 200 OK.
* Errors: 404 if missing, 409 (or 400) if job not IN_PROGRESS, 500 for DB errors.

DTOs (explicit shapes)
* JobDTO — { id, payload, state, result }
* JobResponse — { id, payload } (used by consumer when receiving assigned work)
* ErrorResponse — { message }

## Consumer 

