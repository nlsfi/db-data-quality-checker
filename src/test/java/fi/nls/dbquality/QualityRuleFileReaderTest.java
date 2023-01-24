package fi.nls.dbquality;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import fi.nls.dbquality.model.QualityRule;
import fi.nls.dbquality.model.QualityRules;

public class QualityRuleFileReaderTest {

    @Test
    @DisplayName("Rules are read from yaml file")
    void rulesAreRead() {
        QualityRuleFileReader qualityRuleFileReader = new QualityRuleFileReader("src/test/resources/quality-rules.yml");
        QualityRules rules = qualityRuleFileReader.readRules();

        assertThat(rules.getQualityRules().size()).isEqualTo(8);
        QualityRule rule = rules.getQualityRules().get(0);
        assertThat(rule.getRuleId()).isEqualTo("valid_geometry");

        QualityRule rule2 = rules.getQualityRules().get(1);
        assertThat(rule2.getTarget()).isEqualTo("feature_type.sea_part");

        // Check rules with "check_if_changed" values
        QualityRule rule8 = rules.getQualityRules().get(7);
        assertThat(rule8.getCheckIfChanged()).isNotNull();
        assertThat(rule8.getCheckIfChanged()).anyMatch(i -> i.equals("sea_part"));
        assertThat(rule8.getCheckIfChanged()).anyMatch(i -> i.equals("lake_part"));
        assertThat(rule8.getCheckIfChanged()).anyMatch(i -> i.equals("watercourse_part_area"));
    }
}
