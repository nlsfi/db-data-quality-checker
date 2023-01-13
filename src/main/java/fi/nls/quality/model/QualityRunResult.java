/*
 * TODO Add license and copyright.
 */
package fi.nls.quality.model;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class QualityRunResult {

    private List<QualityResult> qualityResults;
    private Map<String, Set<UUID>> executedRulesById;

    public QualityRunResult(List<QualityResult> qualityResults, Map<String, Set<UUID>> executedRulesById) {
        this.qualityResults = qualityResults;
        this.executedRulesById = executedRulesById;
    }

    public List<QualityResult> getQualityResults() {
        return qualityResults;
    }

    public Map<String, Set<UUID>> getExecutedRulesById() {
        return executedRulesById;
    }
}
