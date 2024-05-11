package Admin;

public interface StatisticsObserver {
    void updateStatistics(int numberOfEmployees, int numberOfEmployeesInPremises, int numberOfEmployeesAbsent,
                          int numberOfCameras, int numberOfLogs);
}
