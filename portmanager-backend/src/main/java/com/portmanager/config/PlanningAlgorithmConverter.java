package com.portmanager.config;

import com.portmanager.model.PlanningAlgorithm;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class PlanningAlgorithmConverter implements Converter<String, PlanningAlgorithm> {

    @Override
    public PlanningAlgorithm convert(String source) {
        return PlanningAlgorithm.fromString(source);
    }
}
