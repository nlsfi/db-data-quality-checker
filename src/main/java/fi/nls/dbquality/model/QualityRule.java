package fi.nls.dbquality.model;

public class QualityRule {
    private String ruleUniqueId;
    private String sql;

    public String getRuleUniqueId() {
        return ruleUniqueId;
    }

    public void setRuleUniqueId(String ruleUniqueId) {
        this.ruleUniqueId = ruleUniqueId;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }
}
