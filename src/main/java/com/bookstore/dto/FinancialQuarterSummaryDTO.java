package com.bookstore.dto;

public class FinancialQuarterSummaryDTO {
    private boolean hasData;
    private int year;
    private int highestRevenueQuarter;
    private double highestQuarterRevenue;
    private int lowestRevenueQuarter;
    private double lowestQuarterRevenue;

    public FinancialQuarterSummaryDTO() {
    }

    public FinancialQuarterSummaryDTO(
            int year,
            int highestRevenueQuarter,
            double highestQuarterRevenue,
            int lowestRevenueQuarter,
            double lowestQuarterRevenue
    ) {
        this.hasData = true;
        this.year = year;
        this.highestRevenueQuarter = highestRevenueQuarter;
        this.highestQuarterRevenue = highestQuarterRevenue;
        this.lowestRevenueQuarter = lowestRevenueQuarter;
        this.lowestQuarterRevenue = lowestQuarterRevenue;
    }

    public boolean isHasData() {
        return hasData;
    }

    public void setHasData(boolean hasData) {
        this.hasData = hasData;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getHighestRevenueQuarter() {
        return highestRevenueQuarter;
    }

    public void setHighestRevenueQuarter(int highestRevenueQuarter) {
        this.highestRevenueQuarter = highestRevenueQuarter;
    }

    public double getHighestQuarterRevenue() {
        return highestQuarterRevenue;
    }

    public void setHighestQuarterRevenue(double highestQuarterRevenue) {
        this.highestQuarterRevenue = highestQuarterRevenue;
    }

    public int getLowestRevenueQuarter() {
        return lowestRevenueQuarter;
    }

    public void setLowestRevenueQuarter(int lowestRevenueQuarter) {
        this.lowestRevenueQuarter = lowestRevenueQuarter;
    }

    public double getLowestQuarterRevenue() {
        return lowestQuarterRevenue;
    }

    public void setLowestQuarterRevenue(double lowestQuarterRevenue) {
        this.lowestQuarterRevenue = lowestQuarterRevenue;
    }
}
