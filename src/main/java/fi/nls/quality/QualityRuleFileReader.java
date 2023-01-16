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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.introspector.PropertyUtils;

import fi.nls.quality.exception.QualityException;
import fi.nls.quality.model.QualityRules;

public class QualityRuleFileReader {

    private String ruleFile;

    public QualityRuleFileReader(String ruleFile) {
        this.ruleFile = ruleFile;
    }

    private static class CamelCasePropertyUtils extends PropertyUtils {
        public CamelCasePropertyUtils() {
            super();
            this.setSkipMissingProperties(true);
        }

        @Override
        public Property getProperty(Class<?> type, String name) {
            return super.getProperty(type, toCamelCase(name));
        }

        private String toCamelCase(String name) {
            while (name.contains("_")) {
                name = name.replaceFirst("_[a-z]", String.valueOf(Character.toUpperCase(name.charAt(name.indexOf("_") + 1))));
            }
            return name;
        }
    }

    public void setRuleFile(String fileName) {
        this.ruleFile = fileName;
    }

    public String getRuleFile() {
        return this.ruleFile;
    }

    public QualityRules readRules() {
        try {
            Constructor constructor = new Constructor(QualityRules.class);
            constructor.setPropertyUtils(new CamelCasePropertyUtils());
            Yaml yaml = new Yaml(constructor);
            InputStream inputStream = Files.newInputStream(new File(ruleFile).toPath());
            return yaml.loadAs(inputStream, QualityRules.class);
        } catch (IOException e) {
            throw new QualityException(e);
        }
    }
}
