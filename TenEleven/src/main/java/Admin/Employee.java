package Admin;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Employee {
    private String id;
    private String fname;
    private String lname;
    private String role;
    private String imgpath;
    
    private Employee(EmployeeBuilder builder) {
        this.id = builder.id;
        this.fname = builder.fname;
        this.lname = builder.lname;
        this.role = builder.role;
        this.imgpath = builder.imgpath;
    }
    
    public static Connection connect() throws SQLException {
        return DriverManager.getConnection("jdbc:mysql://localhost:3306/teneleven", "root", "");
    }
  
    // Getters for fields
    
    public String getId() {
        return id;
    }

    public String getFname() {
        return fname;
    }

    public String getLname() {
        return lname;
    }

    public String getRole() {
        return role;
    }

    public String getImgpath() {
        return imgpath;
    }
    
    // Builder class
    
    public static class EmployeeBuilder {
        private String id;
        private String fname;
        private String lname;
        private String role;
        private String imgpath;
        
        public EmployeeBuilder() {
        }
        
        public EmployeeBuilder setid(String id) {
            this.id = id;
            return this;
        }

        public EmployeeBuilder setFname(String fname) {
            this.fname = fname;
            return this;
        }

        public EmployeeBuilder setLname(String lname) {
            this.lname = lname;
            return this;
        }

        public EmployeeBuilder setRole(String role) {
            this.role = role;
            return this;
        }

        public EmployeeBuilder setImgpath(String imgpath) {
            this.imgpath = imgpath;
            return this;
        }
        
        public Employee build() {
            return new Employee(this);
        }
    }
}