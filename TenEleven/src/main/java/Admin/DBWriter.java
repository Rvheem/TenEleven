package Admin;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DBWriter {

    private String dbName;

    public DBWriter() {
        this.dbName = "teneleven";
    }

    private Connection connect() throws SQLException {
        return DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dbName, "root", "");
    }

  public void addLog(String employeeId, String direction, String time, String accuracy) throws SQLException {
    String lastLogDirection = null;
    // Retrieve the direction of the last log of the same employee
    String directionQuery = "SELECT direction FROM log WHERE id_emp = ? ORDER BY id DESC LIMIT 1";
    Connection conn = connect();
    try (PreparedStatement pstmt = conn.prepareStatement(directionQuery)) {
        pstmt.setString(1, employeeId);
        try (ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                lastLogDirection = rs.getString("direction");
            }
        }
    } catch (SQLException e) {
        System.out.println(e.getMessage());
    } finally {
        // Close the connection
        conn.close();
    }

    // Check if the last log is in the same direction or if there's no previous log
    if (direction.equals(lastLogDirection)) {
        // If the last log is in the same direction or there's no previous log, do not save the log
        System.out.println("Last log of the same employee is in the same direction or there's no previous log. Log not saved.");
        return;
    }
    else{
    Connection conn1 = connect();

    // Save the log if it's not in the same direction as the last log
    String sql = "INSERT INTO log (id_emp, direction, time, accuracy) VALUES (?, ?, ?, ?)";
    try (PreparedStatement pstmt2 = conn1.prepareStatement(sql)) {
        pstmt2.setString(1, employeeId);
        pstmt2.setString(2, direction);
        pstmt2.setString(3, time);
        pstmt2.setString(4, accuracy);
        pstmt2.executeUpdate();
    } catch (SQLException e) {
        System.out.println(e.getMessage());
    } finally {
        // Close the connection
        conn1.close();
    return;
    }}
    
}



  public void addEmployee(Employee employee) {
    String sql = "INSERT INTO employee (id, firstname, lastname, role) VALUES (?, ?, ?, ?)";
    try (Connection conn = connect();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setString(1, employee.getId()); // Assuming Employee class has getId() method
        pstmt.setString(2, employee.getFname());
        pstmt.setString(3, employee.getLname());
        pstmt.setString(4, employee.getRole());
        pstmt.executeUpdate();
    } catch (SQLException e) {
        System.out.println(e.getMessage());
    }
}
  
  public String generateNextId() {
        // Retrieve the last ID from the database and add 1 to it
        String sql = "SELECT MAX(id) FROM employee";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                int lastId = rs.getInt(1);
                return String.valueOf(lastId + 1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // Return null if unable to generate ID
    }

  

  public String[] getLast10Logs() throws SQLException {
        String[] logs = new String[10];
        String sql = "SELECT log.id_emp, log.direction, log.time, log.accuracy, employee.firstname, employee.lastname " +
                     "FROM log " +
                     "JOIN employee ON log.id_emp = employee.id " +
                     "ORDER BY log.id DESC " +
                     "LIMIT 10";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            int index = 0;
            while (rs.next() && index < 10) {
                String firstName = rs.getString("firstname");
                String lastName = rs.getString("lastname");
                String direction = rs.getString("direction");
                String time = rs.getString("time");
                String accuracy = rs.getString("accuracy");

                String action = direction.equalsIgnoreCase("in") ? "entered" : "left";
                logs[index++] = String.format("%s %s has %s on %s with accuracy: %s", firstName, lastName, action, time, accuracy);
            }
        }

        return logs;
    }
  
   public void deleteAllLogs() throws SQLException {
        try (Connection connection = connect();
             Statement statement = connection.createStatement()) {
            String sql = "truncate table log";
            statement.executeUpdate(sql);
        }
    }
    
    public void deleteAllEmployees() throws SQLException {
        try (Connection connection = connect();
             Statement statement = connection.createStatement()) {
            String sql = "truncate table employee";
            statement.executeUpdate(sql);
        }
    }
    
    public void editAdminLogin(int adminId, String newUsername, String newPassword) throws SQLException {
        try (Connection connection = connect();
             Statement statement = connection.createStatement()) {
            String sql = String.format("UPDATE login SET username = '%s', password = '%s' WHERE id = %d",
                    newUsername, newPassword, adminId);
            statement.executeUpdate(sql);
        }
    }
    
    public void exportAllLogsToCSV() throws SQLException, IOException {
        try (Connection connection = connect();
             Statement statement = connection.createStatement()) {
            String sql = "SELECT * FROM log";
            try (ResultSet resultSet = statement.executeQuery(sql)) {
                writeResultSetToCSV(resultSet, "all_logs.csv");
            }
        }
    }
    
    public void exportLogsOfSpecificDayToCSV(String date) throws SQLException, IOException {
        try (Connection connection = connect();
             Statement statement = connection.createStatement()) {
            String sql = "SELECT * FROM log WHERE date = '" + date + "'";
            try (ResultSet resultSet = statement.executeQuery(sql)) {
                writeResultSetToCSV(resultSet, "logs_" + date + ".csv");
            }
        }
    }
    
    public void executeSQLCommand(String sqlCommand) throws SQLException {
        try (Connection connection = connect();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(sqlCommand);
        }
    }
    
    private void writeResultSetToCSV(ResultSet resultSet, String fileName) throws SQLException, IOException {
        try (FileWriter writer = new FileWriter(fileName)) {
            int columnCount = resultSet.getMetaData().getColumnCount();
            for (int i = 1; i <= columnCount; i++) {
                writer.append(resultSet.getMetaData().getColumnName(i));
                if (i < columnCount) {
                    writer.append(",");
                }
            }
            writer.append("\n");
            
            while (resultSet.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    writer.append(resultSet.getString(i));
                    if (i < columnCount) {
                        writer.append(",");
                    }
                }
                writer.append("\n");
            }
        }
    }

}