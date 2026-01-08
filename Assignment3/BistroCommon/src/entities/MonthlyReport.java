package entities;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

/**
 * MonthlyReport entity class represents a monthly report period in the Bistro system.
 * Each report covers a specific date range and is used to track restaurant performance.
 */
public class MonthlyReport implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private int reportId;
    private LocalDate periodStart;
    private LocalDate periodEnd;

    /**
     * Default constructor.
     */
    public MonthlyReport() {
    }

    /**
     * Full constructor.
     * 
     * @param reportId    Unique report identifier
     * @param periodStart Start date of the report period
     * @param periodEnd   End date of the report period
     */
    public MonthlyReport(int reportId, LocalDate periodStart, LocalDate periodEnd) {
        setReportId(reportId);
        setPeriodStart(periodStart);
        setPeriodEnd(periodEnd);
        validate();
    }

    /**
     * Constructor without ID (for creating new reports).
     * 
     * @param periodStart Start date of the report period
     * @param periodEnd   End date of the report period
     */
    public MonthlyReport(LocalDate periodStart, LocalDate periodEnd) {
        setPeriodStart(periodStart);
        setPeriodEnd(periodEnd);
        validate();
    }

    /**
     * Creates a monthly report for a specific month and year.
     * 
     * @param year  The year
     * @param month The month (1-12)
     * @return MonthlyReport instance for the specified month
     */
    public static MonthlyReport createForMonth(int year, int month) {
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());
        return new MonthlyReport(start, end);
    }

    /**
     * Checks if a given date falls within this report period.
     * 
     * @param date the date to check
     * @return true if the date is within the period
     */
    public boolean containsDate(LocalDate date) {
        if (date == null) {
            return false;
        }
        return !date.isBefore(periodStart) && !date.isAfter(periodEnd);
    }

    /**
     * Validates the monthly report state.
     * 
     * @throws IllegalArgumentException if the data is invalid
     */
    public void validate() {
        if (periodStart == null) {
            throw new IllegalArgumentException("Period start must not be null");
        }
        if (periodEnd == null) {
            throw new IllegalArgumentException("Period end must not be null");
        }
        if (periodEnd.isBefore(periodStart)) {
            throw new IllegalArgumentException("Period end must not be before period start");
        }
        if (reportId < 0) {
            throw new IllegalArgumentException("Report ID must not be negative");
        }
    }

    /**
     * Gets the report ID.
     * 
     * @return report ID
     */
    public int getReportId() {
        return reportId;
    }

    /**
     * Sets the report ID.
     * 
     * @param reportId report ID
     * @throws IllegalArgumentException if report ID is negative
     */
    public void setReportId(int reportId) {
        if (reportId < 0) {
            throw new IllegalArgumentException("Report ID must not be negative");
        }
        this.reportId = reportId;
    }

    /**
     * Gets the period start date.
     * 
     * @return period start date
     */
    public LocalDate getPeriodStart() {
        return periodStart;
    }

    /**
     * Sets the period start date.
     * 
     * @param periodStart period start date
     * @throws IllegalArgumentException if period start is null
     */
    public void setPeriodStart(LocalDate periodStart) {
        if (periodStart == null) {
            throw new IllegalArgumentException("Period start must not be null");
        }
        this.periodStart = periodStart;
    }

    /**
     * Gets the period end date.
     * 
     * @return period end date
     */
    public LocalDate getPeriodEnd() {
        return periodEnd;
    }

    /**
     * Sets the period end date.
     * 
     * @param periodEnd period end date
     * @throws IllegalArgumentException if period end is null or before period start
     */
    public void setPeriodEnd(LocalDate periodEnd) {
        if (periodEnd == null) {
            throw new IllegalArgumentException("Period end must not be null");
        }
        if (periodStart != null && periodEnd.isBefore(periodStart)) {
            throw new IllegalArgumentException("Period end must not be before period start");
        }
        this.periodEnd = periodEnd;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MonthlyReport)) return false;
        MonthlyReport that = (MonthlyReport) o;
        return reportId > 0 && reportId == that.reportId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(reportId);
    }

    @Override
    public String toString() {
        return "MonthlyReport{" +
                "reportId=" + reportId +
                ", periodStart=" + periodStart +
                ", periodEnd=" + periodEnd +
                '}';
    }
}