package Model;

public class MonthlyRevenue {
    private String month;
    private float revenue;
    public MonthlyRevenue(String month, float revenue) {
        this.month = month;
        this.revenue = revenue;
    }

    public String getMonth() {
        return month;
    }

    public float getRevenue() {
        return revenue;
    }
}
