package Admin;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    // Singleton instance
    private static final DatabaseManager instance = new DatabaseManager();

    // Database connection parameters
    private static final String URL = "jdbc:mysql://localhost:3306/teneleven";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "";

    // Observers list
    private List<StatisticsObserver> observers;

    private DatabaseManager() {
        observers = new ArrayList<>();
    }

    // Singleton accessor
    public static DatabaseManager getInstance() {
        return instance;
    }

    // Method to add observer
    public void addObserver(StatisticsObserver observer) {
        observers.add(observer);
    }

    // Method to remove observer
    public void removeObserver(StatisticsObserver observer) {
        observers.remove(observer);
    }

    // Method to fetch data from the database
    public void fetchDataFromDatabase() {
        int numberOfEmployees = fetchNumberOfEmployees();
        int numberOfEmployeesInPremises = fetchNumberOfEmployeesInPremises();
        int numberOfEmployeesAbsent = fetchNumberOfEmployeesAbsent();
        int numberOfLogs = fetchNumberOfLogs();
        int numberOfCameras = fetchNumberOfCameras();

        // Notify observers
        for (StatisticsObserver observer : observers) {
            observer.updateStatistics(numberOfEmployees, numberOfEmployeesInPremises,
                                       numberOfEmployeesAbsent, numberOfCameras, numberOfLogs);
        }
    }

    // Method to fetch the total number of employees
    private int fetchNumberOfEmployees() {
        int numberOfEmployees = 0;
        try (Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD)) {
            String query = "SELECT COUNT(*) FROM employee";
            try (PreparedStatement statement = connection.prepareStatement(query);
                 ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    numberOfEmployees = resultSet.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return numberOfEmployees;
    }

    // Method to fetch the number of employees in premises
    // Method to fetch the number of employees in premises
private int fetchNumberOfEmployeesInPremises() {
    int numberOfEmployeesInPremises = 0;
    try (Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD)) {
        String query = "SELECT COUNT(l.id_emp)\n" +
"FROM employee e\n" +
"INNER JOIN log l ON e.id = l.id_emp\n" +
"WHERE l.direction = 'in'\n" +
"GROUP BY l.id_emp\n" +
"ORDER BY l.time DESC;";
        try (PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next()) {
                numberOfEmployeesInPremises = resultSet.getInt(1);
            }
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return numberOfEmployeesInPremises;
}


    // Method to fetch the number of employees absent
    private int fetchNumberOfEmployeesAbsent() {
         int numberOfEmployeesInPremises = fetchNumberOfEmployeesInPremises();
          int numberOfEmployees = fetchNumberOfEmployees();
           int result =numberOfEmployees-numberOfEmployeesInPremises;
           return result;

   
      
    }

    // Method to fetch the total number of logs
    private int fetchNumberOfLogs() {
        int numberOfLogs = 0;
        try (Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD)) {
            String query = "SELECT COUNT(*) FROM log";
            try (PreparedStatement statement = connection.prepareStatement(query);
                 ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    numberOfLogs = resultSet.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return numberOfLogs;
    }

    // Method to fetch the number of cameras using OpenCV
    private int fetchNumberOfCameras() {
        // Implement according to your OpenCV logic
        return 0;
    }
}
