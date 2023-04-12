package fi.nls.dbquality;

import fi.nls.dbquality.model.QualityRule;

public class QualityRuleFixture {

    public static QualityRule createQualityRule(String ruleId, String sql) {
        QualityRule rule = new QualityRule();
        rule.setRuleUniqueId(ruleId);
        rule.setSql(sql);
        return rule;
    }
}
