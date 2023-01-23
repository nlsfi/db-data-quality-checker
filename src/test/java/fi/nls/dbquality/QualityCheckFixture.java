package fi.nls.dbquality;

import java.util.*;

import fi.nls.dbquality.model.Description;

public class QualityCheckFixture {
    public static List<Description> createDescriptions() {
        List<Description> descriptions = new ArrayList<>();
        Arrays.asList("fi", "sv", "en").forEach(lang -> {
            Description description = new Description();
            description.setLang(lang);
            description.setDescription(lang + "_sääntö");
            descriptions.add(description);
        });
        return descriptions;
    }
}
