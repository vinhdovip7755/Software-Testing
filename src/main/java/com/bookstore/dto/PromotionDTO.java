package com.bookstore.dto;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;

public class PromotionDTO {
    private int promotionId;
    private String promotionName;
    private double percent;
    private Timestamp startDate;
    private Timestamp endDate;
    private int status;

    public PromotionDTO() {}

    public PromotionDTO(int promotionId, String promotionName, double percent, Timestamp startDate, Timestamp endDate, int status) {
        this.promotionId = promotionId;
        this.promotionName = promotionName;
        this.percent = percent;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
    }

    public int getPromotionId() {
        return promotionId;
    }

    public void setPromotionId(int promotionId) {
        this.promotionId = promotionId;
    }

    public String getPromotionName() {
        return promotionName;
    }

    public void setPromotionName(String promotionName) {
        this.promotionName = promotionName;
    }

    public double getPercent() {
        return percent;
    }

    public void setPercent(double percent) {
        this.percent = percent;
    }

    public Timestamp getStartDate() {
        return startDate;
    }

    public void setStartDate(Timestamp startDate) {
        this.startDate = startDate;
    }

    public Timestamp getEndDate() {
        return endDate;
    }

    public void setEndDate(Timestamp endDate) {
        this.endDate = endDate;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getFormattedDateRange() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        return sdf.format(startDate) + " - " + sdf.format(endDate);
    }

    @Override
    public String toString() {
        return promotionName;
    }
}
