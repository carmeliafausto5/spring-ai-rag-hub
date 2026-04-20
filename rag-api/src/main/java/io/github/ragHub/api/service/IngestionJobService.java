package io.github.ragHub.api.service;

import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

@Service
public class IngestionJobService {
    private final ConcurrentHashMap<String, String> jobs = new ConcurrentHashMap<>();

    public void submit(String jobId) { jobs.put(jobId, "PENDING"); }
    public void complete(String jobId) { jobs.put(jobId, "DONE"); }
    public void fail(String jobId, String msg) { jobs.put(jobId, "FAILED: " + msg); }
    public String getStatus(String jobId) { return jobs.getOrDefault(jobId, "NOT_FOUND"); }
}
