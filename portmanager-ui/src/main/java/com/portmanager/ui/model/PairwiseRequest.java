package com.portmanager.ui.model;

public class PairwiseRequest {
    private String comparisonId;
    private PlanResponse planA;
    private PlanResponse planB;

    public String getComparisonId() { return comparisonId; }

    public PlanResponse getPlanA() {
        return planA;
    }

    public PlanResponse getPlanB() {
        return planB;
    }
}
