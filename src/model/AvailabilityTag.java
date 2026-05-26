package model;

public enum AvailabilityTag {
    HIGH,
    MEDIUM,
    LOW;

    public static AvailabilityTag fromString(String value) {
        if (value == null) return HIGH;
        switch (value.toUpperCase()) {
            case "HIGH":   return HIGH;
            case "MEDIUM": return MEDIUM;
            case "LOW":    return LOW;
            default:       return HIGH;
        }
    }
}
