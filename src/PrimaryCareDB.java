import java.sql.*;
import java.util.Scanner;

public class PrimaryCareDB {

    // Database info
    private static final String URL = "jdbc:mysql://localhost:3306/primary_care_db";
    private static final String USER = "root";
    private static final String PASSWORD = "12345";

    // Needs more work, This is just a test function:
    public void insertNewPatient(int doctorID, String firstName, String lastName, String dob,
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
        int patientID = -2; // This means nothing was invoked
        int adminID = -2; // This means nothing was invoked
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

            // Test this: ensures both
            patientID = getRecentPatientID(connection);
            adminID = assignAdmin(connection);
            if(patientID == -1 && adminID == -1) {
                System.out.println("Patient ID: " + patientID);
                System.out.println("Admin ID: " + adminID);
                throw new IllegalArgumentException("Retrieving ids resulted in a negative number");
            }

            // Test this!!
            createAdmission(connection, patientID, vacantRoomNumber, doctorID, adminID);

        } catch (SQLException e) {
            System.err.println("Error creating a new patient: " + e.getMessage());
            e.printStackTrace();
        }

        //Debug purposes, delete when confident
        System.out.println("Current Patient ID: " + patientID);
        System.out.println("Current Admin ID: " + adminID);
        System.out.println("Current Doctor ID: " + doctorID);

        // Here is where i create the admission details
        // createAdmission() < - fill this in


    } // End of insertNewPatient

    public void createAdmission(Connection connection, int patientID, int roomNumber, int primaryDocID, int adminID) {
            String admissionSQL = "INSERT INTO admission (patient_id, room_number, admission_date, primary_doctor_id, initial_diagnosis, admitted_by_employee_id) "
                                   + "VALUES (?, ?, NOW(), ?, ?, ?)";

        try(PreparedStatement statement = connection.prepareStatement(admissionSQL)) {
            String diagnosis = getDoctorDiagnosis();

            statement.setInt(1, patientID);
            statement.setInt(2, roomNumber);
            statement.setInt(3, primaryDocID); // Test this part
            statement.setString(4, diagnosis);
            statement.setInt(5, adminID);
            statement.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Error creating admission table: " + e.getMessage());
            e.printStackTrace();
        }
    } // End of createAdmission

    public String getDoctorDiagnosis() {
        Scanner scan = new Scanner(System.in);
        System.out.println("Enter patient diagnosis: ");

        return scan.nextLine();
    } // End of getDoctorDiagnosis

    // returns an int which is the admin id that will be used to set the fk of an admission
    public int assignAdmin(Connection connection) {
        int id = -1; // Returning this means something went wrong with the retrieval in sql statement

        String adminIDSQL = "SELECT employee_id FROM employee WHERE role = 'Admin' ORDER BY RAND() LIMIT 1";
        try(PreparedStatement statement = connection.prepareStatement(adminIDSQL);
            ResultSet resultSet = statement.executeQuery()) {

            if(resultSet.next()) {
                id = resultSet.getInt("employee_id");

                //Delete this after some testing
                System.out.println("assignAdmin() -> This is the Admin id: " + id);
            } else {
                throw new SQLException("No administrators found in the databse");
            }
            // test up to here for errors

        } catch (SQLException e) {
            System.err.println("Error fetching administrator ID: " + e.getMessage());
            e.printStackTrace();
        }

        return id;
    } // End of assignAdmin

    // I can't forget that this mainly used for after inserting a new patient,
    // on the same connection, using it in a different context might produce logic errors
    // This is because of the sql statement im using, using it for convenience, it has its cons
    // finsih tihs method
    public int getRecentPatientID(Connection connection) {
        int id = -1; // Returning this means something went wrong with the retrieval in sql statement

        String patientIDSQL = "SELECT LAST_INSERT_ID() AS patient_id";
        try(PreparedStatement statement = connection.prepareStatement(patientIDSQL);
            ResultSet resultSet = statement.executeQuery()) {

            if(resultSet.next()) {
                id = resultSet.getInt("patient_id");

                //Delete this after some testing
                System.out.println("getRecentPatientID() -> This is the patient's id: " + id);
            }
            // test up to here for errors

        } catch (SQLException e) {
            System.err.println("Error fetching patient ID: " + e.getMessage());
            e.printStackTrace();
        }
        return id;
    } // End of getRecentPatientID

    public void showMyPatients(int doctorID) {
        String sql = "SELECT " +
                "    p.patient_id, " +
                "    p.first_name AS patient_first_name, " +
                "    p.last_name AS patient_last_name, " +
                "    a.admission_date, " +
                "    a.initial_diagnosis, " +
                "    e.first_name AS doctor_first_name, " +
                "    e.last_name AS doctor_last_name " +
                "FROM " +
                "    Admission a " +
                "JOIN " +
                "    Patient p ON a.patient_id = p.patient_id " +
                "JOIN " +
                "    Employee e ON a.primary_doctor_id = e.employee_id " +
                "WHERE " +
                "    e.role = 'Doctor' AND e.employee_id = ?;";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            // Set the doctor ID parameter
            statement.setInt(1, doctorID);

            // Execute the query
            try (ResultSet resultSet = statement.executeQuery()) {
                System.out.println("Patients assigned to Doctor ID " + doctorID + ":");
                while (resultSet.next()) {
                    int patientId = resultSet.getInt("patient_id");
                    String patientFirstName = resultSet.getString("patient_first_name");
                    String patientLastName = resultSet.getString("patient_last_name");
                    String admissionDate = resultSet.getString("admission_date");
                    String initialDiagnosis = resultSet.getString("initial_diagnosis");
                    String doctorFirstName = resultSet.getString("doctor_first_name");
                    String doctorLastName = resultSet.getString("doctor_last_name");

                    // Print to terminal
                    System.out.printf("Patient ID: %d, Name: %s %s, Admission Date: %s, Diagnosis: %s, Doctor: %s %s%n",
                            patientId, patientFirstName, patientLastName, admissionDate, initialDiagnosis,
                            doctorFirstName, doctorLastName);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error retrieving doctor's patients: " + e.getMessage());
            e.printStackTrace();
        }
    }

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
    } // End of isHospitalFull

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

        return -1; // Returning this means something went wrong with the retrieval in sql statement
    } // End of getVacantRoomNumber

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
    } // End of assignRoom


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
