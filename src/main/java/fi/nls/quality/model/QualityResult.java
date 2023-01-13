/*
 * TODO Add license and copyright.
 */
package fi.nls.quality.model;

import java.util.List;
import java.util.UUID;

import org.geolatte.geom.C2D;
import org.geolatte.geom.Geometry;

public class QualityResult {
    private String category;
    private UUID targetId;
    private String relatedCategory;
    private UUID relatedId;
    private String ruleId;
    private String type;
    private String priority;
    private List<Description> descriptions;
    private String violatingAttributeName;
    // TODO tarvitaanko?
    private String violatingAttributeValue;
    private Geometry<C2D> violatingGeometry;

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public UUID getTargetId() {
        return targetId;
    }

    public void setTargetId(UUID targetId) {
        this.targetId = targetId;
    }

    public String getRelatedCategory() {
        return relatedCategory;
    }

    public void setRelatedCategory(String relatedCategory) {
        this.relatedCategory = relatedCategory;
    }

    public UUID getRelatedId() {
        return relatedId;
    }

    public void setRelatedId(UUID relatedId) {
        this.relatedId = relatedId;
    }

    public String getRuleId() {
        return ruleId;
    }

    public void setRuleId(String ruleId) {
        this.ruleId = ruleId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public List<Description> getDescriptions() {
        return descriptions;
    }

    public void setDescriptions(List<Description> descriptions) {
        this.descriptions = descriptions;
    }

    public String getViolatingAttributeName() {
        return violatingAttributeName;
    }

    public void setViolatingAttributeName(String violatingAttributeName) {
        this.violatingAttributeName = violatingAttributeName;
    }

    public String getViolatingAttributeValue() {
        return violatingAttributeValue;
    }

    public void setViolatingAttributeValue(String violatingAttributeValue) {
        this.violatingAttributeValue = violatingAttributeValue;
    }

    public Geometry<C2D> getViolatingGeometry() {
        return violatingGeometry;
    }

    public void setViolatingGeometry(Geometry<C2D> violatingGeometry) {
        this.violatingGeometry = violatingGeometry;
    }
}
