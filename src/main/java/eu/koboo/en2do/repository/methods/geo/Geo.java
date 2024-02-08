package eu.koboo.en2do.repository.methods.geo;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
@ToString
public class Geo {

    /**
     * Use this method to create a new GEo object
     *
     * @return The new created sort object.
     */
    public static Geo of() {
        return new Geo();
    }

    double latitude;
    double longitude;
    Double maxDistance;
    Double minDistance;
    GeoType type;

    private Geo() {
        type = GeoType.NEAR;
    }

    public Geo latitude(double latitude) {
        this.latitude = latitude;
        return this;
    }

    public Geo longitude(double longitude) {
        this.longitude = longitude;
        return this;
    }

    public Geo coordinates(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
        return this;
    }

    public Geo maxDistance(double maxDistance) {
        this.maxDistance = maxDistance;
        return this;
    }

    public Geo minDistance(double minDistance) {
        this.minDistance = minDistance;
        return this;
    }

    public Geo near() {
        this.type = GeoType.NEAR;
        return this;
    }

    public Geo nearSphere() {
        this.type = GeoType.NEAR_SPHERE;
        return this;
    }
}