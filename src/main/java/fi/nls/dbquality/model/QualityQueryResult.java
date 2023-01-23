package fi.nls.dbquality.model;

import java.util.UUID;

import org.geolatte.geom.C2D;
import org.geolatte.geom.Geometry;

public class QualityQueryResult {
    private UUID targetId;
    private UUID relatedId;
    private Geometry<C2D> geometryError;

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

    public Geometry<C2D> getGeometryError() {
        return geometryError;
    }

    public void setGeometryError(Geometry<C2D> geometryError) {
        this.geometryError = geometryError;
    }
}
