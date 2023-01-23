package fi.nls.dbquality.model;

import java.util.List;

public class QualityRule {
    private String ruleId;
    private String category;
    private String type;
    private String attributeName;
    private String sql;
    private List<Description> descriptions;
    private String priority;
    private String target;
    private String qgisExpression;
    private List<String> checkIfChanged;

    public String getRuleId() {
        return ruleId;
    }

    public void setRuleId(String ruleId) {
        this.ruleId = ruleId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAttributeName() {
        return attributeName;
    }

    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public List<Description> getDescriptions() {
        return descriptions;
    }

    public void setDescriptions(List<Description> descriptions) {
        this.descriptions = descriptions;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getQgisExpression() {
        return qgisExpression;
    }

    public void setQgisExpression(String qgisExpression) {
        this.qgisExpression = qgisExpression;
    }

    public List<String> getCheckIfChanged() {
        return checkIfChanged;
    }

    public void setCheckIfChanged(List<String> checkIfChanged) {
        this.checkIfChanged = checkIfChanged;
    }
}
