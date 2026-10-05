package com.lru.project.controller;

import com.lru.project.model.SimulationModels.ApiError;
import com.lru.project.model.SimulationModels.CacheRequest;
import com.lru.project.model.SimulationModels.CacheResponse;
import com.lru.project.model.SimulationModels.HistoryRow;
import com.lru.project.model.SimulationModels.SimulateRequest;
import com.lru.project.model.SimulationModels.SimulationOutcome;
import com.lru.project.repository.SimulationRepository;
import com.lru.project.service.CacheDemoService;
import com.lru.project.service.SimulationService;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class SimulationController {
    private final SimulationService simulations;
    private final SimulationRepository history;
    private final CacheDemoService cache;

    public SimulationController(SimulationService simulations, SimulationRepository history, CacheDemoService cache) {
        this.simulations = simulations;
        this.history = history;
        this.cache = cache;
    }

    @PostMapping("/simulate")
    public SimulationOutcome simulate(@RequestBody SimulateRequest request) {
        return simulations.simulateAndSave(request);
    }

    @GetMapping("/history")
    public List<HistoryRow> history() {
        return history.findAll();
    }

    @DeleteMapping("/history")
    public Map<String, Object> clearHistory() {
        int deleted = history.deleteAll();
        return Map.of("deleted", deleted, "message", "Simulation history cleared.");
    }

    @PostMapping("/cache")
    public CacheResponse cache(@RequestBody CacheRequest request, HttpSession session) {
        return cache.operate(session, request);
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "ok");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> invalidInput(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(new ApiError(e.getMessage()));
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ApiError> databaseFailure(DataAccessException e) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new ApiError("Database operation failed. Check that MySQL is running and the lrudb settings are correct."));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> unexpectedFailure(Exception e) {
        return ResponseEntity.internalServerError().body(new ApiError("The request could not be completed."));
    }
}
