package com.emal.android.transport.spb.portal;

/**
 * @author alexey.emelyanenko@gmail.com
 * @since 1.5
 */
public class Stop {
    public enum Type {
        BUS(0), TROLLEY(1), TRAM(2), AQUABUS(46);

        private int id;

        private Type(int id) {
            this.id = id;
        }

        public static Type getById(int id) {
            for (Type t : Type.values()) {
                if (t.id == id) {
                    return t;
                }
            }
            return null;
        }
    }

    private int id;
    private Type type;
    private String name;
    private String nearestStreets;
    private Geometry lonLat;

    public int getId() {
        return id;
    }

    public Type getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getNearestStreets() {
        return nearestStreets;
    }

    public Geometry getLonLat() {
        return lonLat;
    }

    private Stop(StopBuilder builder) {
        this.id = builder.id;
        this.type = builder.type;
        this.name = builder.name;
        this.nearestStreets = builder.nearestStreets;
        this.lonLat = builder.lonLat;
    }

    public static class StopBuilder {
        private int id;
        private Type type;
        private String name;
        private String nearestStreets;
        private Geometry lonLat;

        public static StopBuilder getInstance() {
            return new StopBuilder();
        }

        public StopBuilder id(int id) {
            this.id = id;
            return this;
        }

        public StopBuilder type(Type type) {
            this.type = type;
            return this;
        }

        public StopBuilder name(String name) {
            this.name = name;
            return this;
        }

        public StopBuilder nearestStreets(String nearestStreets) {
            this.nearestStreets = nearestStreets;
            return this;
        }

        public StopBuilder lonLat(Geometry lonLat) {
            this.lonLat = lonLat;
            return this;
        }

        public Stop build() {
            return new Stop(this);
        }
    }
}
