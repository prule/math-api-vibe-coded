package com.paulrule.mathapi.adapter.in.web;

import com.paulrule.mathapi.application.PerformCalculation;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/calculations")
class CalculationController {

    private final PerformCalculation performCalculation;

    CalculationController(PerformCalculation performCalculation) {
        this.performCalculation = performCalculation;
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    CalculationResponse calculate(@Valid @RequestBody CalculationRequest request) {
        return CalculationResponse.from(performCalculation.perform(request.operation(), request.a(), request.b()));
    }
}
