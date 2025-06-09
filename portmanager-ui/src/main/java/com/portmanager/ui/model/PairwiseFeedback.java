package com.portmanager.ui.model;

public class PairwiseFeedback {
    private final String comparisonId;
    private final String chosenPlan;   // "A" or "B"

    public PairwiseFeedback(String comparisonId, String chosenPlan) {
                this.comparisonId = comparisonId;
                this.chosenPlan   = chosenPlan;
            }
    public String getComparisonId() { return comparisonId; }
    public String getChosenPlan()   { return chosenPlan;   }
}
