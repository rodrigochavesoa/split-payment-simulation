package br.com.splitpayment.api;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/simulations")
public class SimulationController {
    private final SimulationOrchestratorService orchestratorService;

    public SimulationController(SimulationOrchestratorService orchestratorService) {
        this.orchestratorService = orchestratorService;
    }

    @PostMapping("/float-impact")
    public ResponseEntity<SimulationResponse> simulate(@Valid @RequestBody SimulationRequest request) {
        return ResponseEntity.ok(orchestratorService.simulate(request));
    }
}
