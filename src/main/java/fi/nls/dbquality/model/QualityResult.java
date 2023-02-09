package fi.nls.dbquality.model;

import java.util.List;
import java.util.UUID;

import org.geolatte.geom.C2D;
import org.geolatte.geom.Geometry;

public class QualityResult {
    private String id;
    private UUID targetId;
    private UUID relatedId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    private Geometry<C2D> violatingGeometry;

    public UUID getTargetId() {
        return targetId;
    }

    public void setTargetId(UUID targetId) {
        this.targetId = targetId;
    }

    public UUID getRelatedId() {
        return relatedId;
    }

    public void setRelatedId(UUID relatedId) {
        this.relatedId = relatedId;
    }

    public Geometry<C2D> getViolatingGeometry() {
        return violatingGeometry;
    }

    public void setViolatingGeometry(Geometry<C2D> violatingGeometry) {
        this.violatingGeometry = violatingGeometry;
    }
}
