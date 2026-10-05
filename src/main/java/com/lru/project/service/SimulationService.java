package com.lru.project.service;

import com.lru.project.model.SimulationModels.SimulateRequest;
import com.lru.project.model.SimulationModels.SimulationOutcome;
import com.lru.project.repository.SimulationRepository;
import org.springframework.stereotype.Service;

@Service
public class SimulationService {
    private final PageReplacementService simulator;
    private final SimulationRepository repository;

    public SimulationService(PageReplacementService simulator, SimulationRepository repository) {
        this.simulator = simulator;
        this.repository = repository;
    }

    public SimulationOutcome simulateAndSave(SimulateRequest request) {
        if (request == null) throw new IllegalArgumentException("Simulation input is required.");
        if (request.frames() <= 0 || request.frames() > 100) {
            throw new IllegalArgumentException("Frames must be between 1 and 100.");
        }
        int[] pages = simulator.parseReferences(request.referenceString());
        SimulationOutcome result = simulator.simulate(pages, request.frames());
        int id = repository.insert(result);
        return new SimulationOutcome(id, result.frames(), result.referenceString(), result.hits(),
                result.faults(), result.hitRatio(), null, result.steps());
    }
}
