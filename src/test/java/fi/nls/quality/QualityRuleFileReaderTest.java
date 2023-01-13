/*  
  Copyright (C) 2022 National Land Survey of Finland
  (https://www.maanmittauslaitos.fi/en).


  This file is part of quality-service.

  quality-service is free software: you can redistribute it and/or
  modify it under the terms of the GNU General Public License as published
  by the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  quality-service is distributed in the hope that it will be
  useful, but WITHOUT ANY WARRANTY; without even the implied warranty
  of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with quality-service. If not, see <https://www.gnu.org/licenses/>.
*/
package fi.nls.quality;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import fi.nls.quality.model.QualityRule;
import fi.nls.quality.model.QualityRules;

public class QualityRuleFileReaderTest {

    @Test
    @DisplayName("Rules are read from yaml file")
    void rulesAreRead() {
        QualityRuleFileReader qualityRuleFileReader = new QualityRuleFileReader();
        ReflectionTestUtils.setField(qualityRuleFileReader, "ruleFile", "src/test/resources/quality-rules.yml");
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
