package com.example.producer;

import org.hibernate.annotations.Type;

import com.fasterxml.jackson.databind.JsonNode;
import com.vladmihalcea.hibernate.type.json.JsonType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Job {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String payload;
	private JobState state;

	@Type(JsonType.class)
	@Column(columnDefinition = "jsonb")
	private JsonNode result;

	public Job(String payload, JobState state) {
		this.payload = payload;
		this.state = state;
	}

	protected Job() {
	}

	public JsonNode getResult() {
		return result;
	}

	@Override
	public String toString() {
		return "Job [id=" + id + ", payload=" + payload + ", state=" + state + "]";
	}

	public Long getId() {
		return id;
	}

	public String getPayload() {
		return payload;
	}

	public JobState getState() {
		return state;
	}

	public void setState(JobState state) {
		this.state = state;
	}

	public void setResult(JsonNode result) {
		this.result = result;
	}
}
