package com.bookstore.dto;

public class MembershipRankDTO {
    private int rankId;
    private String rankName;
    private int minPoint;
    private double discountPercent;

    public MembershipRankDTO() {}

    public MembershipRankDTO(int rankId, String rankName, int minPoint, double discountPercent) {
        this.rankId = rankId;
        this.rankName = rankName;
        this.minPoint = minPoint;
        this.discountPercent = discountPercent;
    }

    public int getRankId() {
        return rankId;
    }

    public void setRankId(int rankId) {
        this.rankId = rankId;
    }

    public String getRankName() {
        return rankName;
    }

    public void setRankName(String rankName) {
        this.rankName = rankName;
    }

    public int getMinPoint() {
        return minPoint;
    }

    public void setMinPoint(int minPoint) {
        this.minPoint = minPoint;
    }

    public double getDiscountPercent() {
        return discountPercent;
    }

    public void setDiscountPercent(double discountPercent) {
        this.discountPercent = discountPercent;
    }
}
