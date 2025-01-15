import java.sql.*;

public class PrimaryCareDB {

    // Database info
    private static final String URL = "jdbc:mysql://localhost:3306/primary_care_db";
    private static final String USER = "root";
    private static final String PASSWORD = "12345";

    // Needs more work, This is just a test function:
    public void insertNewPatient(String firstName, String lastName, String dob,
                                 String emergencyContactName, String emergencyContactPhone,
                                 String insurancePolicyNum) {

        // CHECK IF HOSPITAL IS FULL FIRST (remove NOT operator after testing)
        // SO that it doesn't insert a new patient and just returns
        if(!isHospitalFull()){
            System.out.println("The Hospital is not full");
            //return;
        }

        // Get a vacant room number and then set it to occupied for the new patient to be inserted
        int vacantRoomNumber = getVacantRoomNumber();
        assignRoom(vacantRoomNumber);

        // insert patient sql
        String insertSQL = "INSERT INTO patient (first_name, last_name, dob, emergency_contact_name, emergency_contact_phone, insurance_policy_num) "
                         + "VALUES (?, ?, ?, ?, ?, ?)";

        // preparing and executing the statement via a successful connection
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(insertSQL)) {

            // Prepare the statement
            statement.setString(1, firstName);
            statement.setString(2, lastName);
            statement.setString(3, dob);
            statement.setString(4, emergencyContactName);
            statement.setString(5, emergencyContactPhone);
            statement.setString(6, insurancePolicyNum);

            // executing operation
            int rowsInserted = statement.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("A new patient was inserted successfully!");
            }



        } catch (SQLException e) {
            System.err.println("Error creating a new patient: " + e.getMessage());
            e.printStackTrace();
        }
    } // End of insertNewPatient

    // Needs more testing
    public boolean isHospitalFull() {
        String findVacantRoomSQL = "SELECT COUNT(*) AS occupied_count FROM room WHERE is_occupied = 1";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(findVacantRoomSQL);) {

            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                int occupiedCount = resultSet.getInt("occupied_count");

                //Debug purposes, delete when finished testing
                System.out.println("Number of occupied rooms: " + occupiedCount);

                return occupiedCount == 20;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Needs some testing
    public int getVacantRoomNumber() {
        String findVacantRoomSQL = "SELECT room_number FROM room WHERE is_occupied = 0 LIMIT 1";

        try(Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
            PreparedStatement statement = connection.prepareStatement(findVacantRoomSQL)) {

            ResultSet resultSet = statement.executeQuery();
            if(resultSet.next()) {
                int vacantRoomNumber = resultSet.getInt("room_number");

                // Debug line, delete when finished testing;
                System.out.println("Found a vacant room number: " + vacantRoomNumber);

                return vacantRoomNumber;
            } else {
                System.out.println("No vacant rooms found!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return -1;
    }

    public void assignRoom(int vacantRoomNumber) {
        String updateRoomSQL = "UPDATE room SET is_occupied = 1 WHERE room_number = ?";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(updateRoomSQL)) {

            statement.setInt(1, vacantRoomNumber);
            int rowsUpdated = statement.executeUpdate();

            if (rowsUpdated > 0) {
                System.out.println("Room " + vacantRoomNumber + " successfully assigned.");
            } else {
                System.out.println("Error: Room " + vacantRoomNumber + " could not be assigned.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public void insertNewEmployee(String firstName, String lastName, String role,
                                  String hireDate) {
        // sql statement
        String sql = "INSERT INTO Employee (first_name, last_name, role, hire_date) "
                + "VALUES (?, ?, ?, ?)";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, firstName);
            statement.setString(2, lastName);
            statement.setString(3, role);
            statement.setString(4, hireDate);

            // executing operation
            int rowsInserted = statement.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("A new employee was inserted successfully!");
            }
        } catch (SQLException e) {
            System.err.println("Error creating a new employee: " + e.getMessage());
            e.printStackTrace();
        }
    } // End of insertNewEmployee

    public String[] getEmployeeByID(int id) {
        String sql = "SELECT * FROM employee WHERE employee_id = ?";
        int employeeID;
        String[] employeeData = new String[5];

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            // prepare the statement
            statement.setInt(1, id);

            // Execute the query
            ResultSet resultSet = statement.executeQuery();

            if(resultSet.next()) {
                // Process the result set
                employeeID = resultSet.getInt("employee_id");
                employeeData[0] = Integer.toString(employeeID);
                employeeData[1] = resultSet.getString("first_name");
                employeeData[2] = resultSet.getString("last_name");
                employeeData[3] = resultSet.getString("role");
                employeeData[4] = resultSet.getString("hire_date");

                // display to console for debug purposes: (delete when ready)
                System.out.println("Employee ID: " + employeeData[0]);
                System.out.println("First Name: " + employeeData[1]);
                System.out.println("Last Name: " + employeeData[2]);
                System.out.println("Role: " + employeeData[3]);
                System.out.println("Hire Date: " + employeeData[4]);
            }
            else {
                System.out.println("No employee found with ID: " + id);
            }
        } catch (SQLException e) {
            System.err.println("Error creating a new employee: " + e.getMessage());
            e.printStackTrace();
        }
        return employeeData;
    }

    public void showEmployeeByID(int id) {
        String sql = "SELECT * FROM employee WHERE employee_id = ?";
        int employeeID;
        String[] employeeData = new String[4];

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            // prepare the statement
            statement.setInt(1, id);

            // Execute the query
            ResultSet resultSet = statement.executeQuery();

            if(resultSet.next()) {
                // Process the result set
                employeeID = resultSet.getInt("employee_id");
                String firstName = resultSet.getString("first_name");
                String lastName = resultSet.getString("last_name");
                String role = resultSet.getString("role");
                String hireDate = resultSet.getString("hire_date");

                // display to console for debug purposes: (delete when ready)
                System.out.println("Employee ID: " + employeeID);
                System.out.println("First Name: " + firstName);
                System.out.println("Last Name: " + lastName);
                System.out.println("Role: " + role);
                System.out.println("Hire Date: " + hireDate);
            }
            else {
                System.out.println("No employee found with ID: " + id);
            }
        } catch (SQLException e) {
            System.err.println("Error creating a new employee: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Purpose: validates employee credentials ensuring they exist in the database
    public boolean verifyCredentials(String[] employeeCredentials) {
        int employee_id = Integer.parseInt(employeeCredentials[0]);
        String sql = "SELECT * FROM employee WHERE employee_id = ?";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            // prepare the statement
            statement.setInt(1, employee_id);

            // Execute the query
            ResultSet resultSet = statement.executeQuery();

            if(resultSet.next()) {
                // validate
                int employeeID = resultSet.getInt("employee_id");
                String firstName = resultSet.getString("first_name");
                String lastName = resultSet.getString("last_name");

                // credentials match
                if(employeeID == employee_id && employeeCredentials[1].equals(firstName) &&
                   employeeCredentials[2].equals(lastName)) {return true;}
            }
            else {
                System.out.println("No employee found with ID: " + employee_id);
            }
        } catch (SQLException e) {
            System.err.println("Error creating a new employee: " + e.getMessage());
            e.printStackTrace();
        }


        return false;
    } // End of checkCredentials

    // Debug Functions
    public static void printArrayData(String[] currentData) {
        for (String i : currentData) {
            System.out.println(i);
        }
    }
}
