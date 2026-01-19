package jp.insurance.system.model;

public enum AccidentStatus {
    OPEN("受付"),
    IN_PROGRESS("対応中"),
    RESOLVED("完了");

    private final String displayName;

    AccidentStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}