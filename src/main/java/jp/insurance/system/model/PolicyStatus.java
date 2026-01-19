package jp.insurance.system.model;

public enum PolicyStatus {
    ACTIVE("契約中"),
    CANCELLED("解約");

    private final String displayName;

    PolicyStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}