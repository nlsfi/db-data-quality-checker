package fi.nls.dbquality;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.introspector.PropertyUtils;

import fi.nls.dbquality.exception.QualityException;
import fi.nls.dbquality.model.QualityRules;

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
