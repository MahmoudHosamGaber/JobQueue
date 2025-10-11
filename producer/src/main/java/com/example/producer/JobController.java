package com.example.producer;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;

@RestController
public class JobController {

	public final JobRepository repo;
	Queue<Job> jobQueue = new ArrayDeque<>();

	public JobController(JobRepository repo) {
		this.repo = repo;
	}

	@PostMapping("/jobs")
	public ResponseEntity<?> createJob(@RequestBody JobRequest request) {
		Job job = repo.save(new Job(request.payload, JobState.PENDING));
		return ResponseEntity.status(201).body(new JobDTO(job));
	}

	@GetMapping("/jobs/{id}")
	public ResponseEntity<JobDTO> getJob(@PathVariable long id) {
		Job job = repo.getReferenceById(id);
		return ResponseEntity.status(200).body(new JobDTO(job));
	}

	@PostMapping("/jobs/next")
	public ResponseEntity<?> requestJob() {
		if (jobQueue.isEmpty()) {
			List<Job> jobs = new ArrayList<>();
			jobs = repo.findByState(JobState.PENDING);
			jobQueue.addAll(jobs);
		}
		if (jobQueue.isEmpty()) {
			return ResponseEntity.noContent().build();
		}
		Job job = jobQueue.poll();
		assert job.getState() == JobState.PENDING;
		job.setState(JobState.IN_PROGRESS);
		repo.save(job);
		return ResponseEntity.ok().body(new JobResponse(job));
	}

	@PostMapping("/jobs/{id}")
	public ResponseEntity<?> submitJob(@PathVariable long id, @RequestBody JsonNode body) {
		Job job;
		job = repo.getReferenceById(id);
		assert job.getState() == JobState.IN_PROGRESS;
		JsonNode result = body.get("result");
		job.setResult(result);
		job.setState(JobState.SUCCEEDED);
		repo.save(job);
		return ResponseEntity.ok().build();
	}

	record JobRequest(String payload) {
	}

	record JobDTO(long id, String payload, JobState state, JsonNode result) {
		JobDTO(Job job) {
			this(job.getId(), job.getPayload(), job.getState(), job.getResult());
		}
	}

	record JobResponse(long id, String payload) {
		JobResponse(Job job) {
			this(job.getId(), job.getPayload());
		}
	}
}
