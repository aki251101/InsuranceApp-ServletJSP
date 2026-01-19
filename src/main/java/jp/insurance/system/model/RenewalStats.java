package jp.insurance.system.model;

public class RenewalStats {
    private int earlyRenewalCount;
    private int totalRenewalCount;

    public RenewalStats(int earlyRenewalCount, int totalRenewalCount) {
        this.earlyRenewalCount = earlyRenewalCount;
        this.totalRenewalCount = totalRenewalCount;
    }

    public int getEarlyRenewalCount() {
        return earlyRenewalCount;
    }

    public int getTotalRenewalCount() {
        return totalRenewalCount;
    }

    public String getFormattedRate() {
        if (totalRenewalCount == 0) {
            return "— (0/0)";
        }
        double rate = (double) earlyRenewalCount / totalRenewalCount * 100;
        return String.format("%.1f%% (%d/%d)", rate, earlyRenewalCount, totalRenewalCount);
    }
}