import java.sql.*;
import java.util.Scanner;

public class PrimaryCareDB {

    // Database info
    private static final String URL = "jdbc:mysql://localhost:3306/primary_care_db";
    private static final String USER = "root";
    private static final String PASSWORD = "12345";

    public void insertNewPatient(int doctorID, String firstName, String lastName, String dob,
                                 String emergencyContactName, String emergencyContactPhone,
                                 String insurancePolicyNum) {

        // Check if the hospital is full
        if (isHospitalFull()) {
            System.out.println("The hospital is full. Cannot admit new patients.");
            return;
        }

        // Check if the patient already exists
        int patientID = findPatientID(firstName, lastName, dob);

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD)) {
            // Patient doesn't exists
            if (patientID == -1) {
                System.out.println("Patient does not exist. Inserting new patient...");

                // execute statement
                String insertSQL = "INSERT INTO patient (first_name, last_name, dob, emergency_contact_name, emergency_contact_phone, insurance_policy_num) "
                        + "VALUES (?, ?, ?, ?, ?, ?)";
                try (PreparedStatement statement = connection.prepareStatement(insertSQL)) {
                    statement.setString(1, firstName);
                    statement.setString(2, lastName);
                    statement.setString(3, dob);
                    statement.setString(4, emergencyContactName);
                    statement.setString(5, emergencyContactPhone);
                    statement.setString(6, insurancePolicyNum);

                    int rowsInserted = statement.executeUpdate();
                    if (rowsInserted > 0) {
                        System.out.println("A new patient was inserted successfully!");
                    }
                    patientID = getRecentPatientID(connection);
                }
            } else {
                System.out.println("Patient already exists with ID: " + patientID);
                System.out.println("Creating new admission for patient");
            }

            // assign room
            int vacantRoomNumber = getVacantRoomNumber();
            assignRoom(vacantRoomNumber);

            // assign a medical staff
            int adminID = assignMedicalStaff(connection);
            if (adminID == -1) {
                throw new IllegalArgumentException("Unable to assign an admin staff.");
            }
            // create admission
            createAdmission(connection, patientID, vacantRoomNumber, doctorID, adminID);

        } catch (SQLException e) {
            System.err.println("Error processing the patient admission: " + e.getMessage());
            e.printStackTrace();
        }
    } // End of insertNewPatient

    private int findPatientID(String firstName, String lastName, String dob) {

        // execute statement
        String findPatientIdSQL = "SELECT patient_id FROM patient WHERE first_name = ? AND last_name = ? AND dob = ?";
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(findPatientIdSQL)) {

            statement.setString(1, firstName);
            statement.setString(2, lastName);
            statement.setString(3, dob);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("patient_id");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error checking if patient exists: " + e.getMessage());
            e.printStackTrace();
        }
        return -1;

    } // End of findPatientID

    public void dischargePatient(int patientID) {
        // execute statement
        String dischargePatientSQL = "UPDATE admission " +
                                     "SET " +
                                     "discharge_date = NOW(), " +
                                     "discharged_by_employee_id = admitted_by_employee_id " +
                                     "WHERE " +
                                     "admission_id = ?";

        try(Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
            PreparedStatement statement = connection.prepareStatement(dischargePatientSQL)) {

            setRoomToVacant(patientID);

            int admissionID = getAdmissionID(connection, patientID);
            statement.setInt(1, admissionID);
            statement.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Error discharging patient: " + e.getMessage());
            e.printStackTrace();
        }
    } // End of dischargePatient

    // Test this
    public void assignSupportingDoctor(int admissionID, int doctorID) {
        String insertSupportingDoctorSQL = "INSERT INTO Supporting_Doctors (admission_id, doctor_id) VALUES (?, ?)";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(insertSupportingDoctorSQL)) {

            // Set the parameters for the insert statement
            statement.setInt(1, admissionID);
            statement.setInt(2, doctorID);

            // Execute the query
            int rowsInserted = statement.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("Successfully assigned Doctor ID " + doctorID + " to Admission ID " + admissionID);
            } else {
                System.out.println("Failed to assign Doctor ID " + doctorID + " to Admission ID " + admissionID);
            }

        } catch (SQLException e) {
            System.err.println("Error assigning supporting doctor: " + e.getMessage());
            e.printStackTrace();
        }
    } // End of assignSupportingDoctor

    public void orderTreatment(int doctorID, int patientID, String treatmentType) {
        // ask doctor for treatment notes
        String notes = getDoctorNotes();

        // execute statement
        String orderTreatmentSQL = "INSERT INTO " +
                "treatment " +
                "(treatment_type, notes, order_date, ordered_by_doctor_id, admission_id) " +
                "VALUES (?, ?, NOW(), ?, ?)";

        try(Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
            PreparedStatement statement = connection.prepareStatement(orderTreatmentSQL)) {

            int admissionID = getAdmissionID(connection, patientID);

            statement.setString(1, treatmentType);
            statement.setString(2, notes);
            statement.setInt(3, doctorID);
            statement.setInt(4, admissionID);
            statement.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Error creating admission table: " + e.getMessage());
            e.printStackTrace();
        }
    } // End of orderTreatment

    public int getAdmissionID(Connection connection, int patientID) {
        int id = -1; // id can't be negative, so error if encountered

        // execute statement
        String patientIDSQL = "SELECT admission_id " +
                "FROM admission " +
                "WHERE patient_id = ? AND discharge_date IS NULL " +
                "ORDER BY admission_date DESC " +
                "LIMIT 1";

        try (PreparedStatement statement = connection.prepareStatement(patientIDSQL)) {

            statement.setInt(1, patientID);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    id = resultSet.getInt("admission_id");

                    // debug purposes
                    System.out.println("getAdmissionID() -> Current retrieved admission ID: " + id);
                } else {
                    System.out.println("No active admission found for patient ID: " + patientID);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching admission ID: " + e.getMessage());
            e.printStackTrace();
        }
        return id;
    } // End of getAdmissionID

    public String getDoctorNotes() {
        Scanner scan = new Scanner(System.in);
        System.out.println("Enter treatment notes: ");

        return scan.nextLine();
    } // End of getDoctorNotes

    public void createAdmission(Connection connection, int patientID, int roomNumber, int primaryDocID, int adminID) {
        // execute statement
        String admissionSQL = "INSERT INTO admission (patient_id, room_number, admission_date, primary_doctor_id, initial_diagnosis, admitted_by_employee_id) "
                               + "VALUES (?, ?, NOW(), ?, ?, ?)";

        try(PreparedStatement statement = connection.prepareStatement(admissionSQL)) {
            String diagnosis = getDoctorDiagnosis();

            statement.setInt(1, patientID);
            statement.setInt(2, roomNumber);
            statement.setInt(3, primaryDocID);
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
    public int assignMedicalStaff(Connection connection) {
        int id = -1; // returning this means something went wrong with the retrieval in sql statement

        // execute statement
        String randomEmployeeSQL = "Select employee_id " +
                                    "FROM employee " +
                                    "WHERE role IN ('Doctor', 'Nurse', 'Technician') " +
                                    "ORDER BY RAND() LIMIT 1";

        try(PreparedStatement statement = connection.prepareStatement(randomEmployeeSQL);
            ResultSet resultSet = statement.executeQuery()) {

            if(resultSet.next()) {
                id = resultSet.getInt("employee_id");

                // debug purposes
                System.out.println("assignAdmin() -> This is the employee id: " + id);
            } else {
                throw new SQLException("No employee's found in the databse");
            }
        } catch (SQLException e) {
            System.err.println("Error fetching employee ID: " + e.getMessage());
            e.printStackTrace();
        }

        return id;
    } // End of assignAdmin

    // I can't forget that this mainly used for after inserting a new patient,
    // on the same connection, using it in a different context might produce logic errors beware
    public int getRecentPatientID(Connection connection) {
        int id = -1; // returning this means something went wrong with the retrieval in sql statement

        String patientIDSQL = "SELECT LAST_INSERT_ID() AS patient_id";
        try(PreparedStatement statement = connection.prepareStatement(patientIDSQL);
            ResultSet resultSet = statement.executeQuery()) {

            if(resultSet.next()) {
                id = resultSet.getInt("patient_id");

                // debug purposes
                System.out.println("getRecentPatientID() -> This is the patient's id: " + id);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching patient ID: " + e.getMessage());
            e.printStackTrace();
        }
        return id;
    } // End of getRecentPatientID

    public void showAssignedAdmissions(int doctorID) {
        // execute statement
        String assignedAdmissionsSQL = "SELECT " +
                "a.admission_id, " +
                "p.patient_id, " +
                "p.first_name AS patient_first_name, " +
                "p.last_name AS patient_last_name, " +
                "a.admission_date, " +
                "a.initial_diagnosis, " +
                "e.first_name AS doctor_first_name, " +
                "e.last_name AS doctor_last_name, " +
                "t.notes AS treatment_notes " +
                "FROM admission a " +
                "JOIN patient p ON a.patient_id = p.patient_id " +
                "JOIN employee e ON a.primary_doctor_id = e.employee_id " +
                "LEFT JOIN treatment t ON a.admission_id = t.admission_id " +
                "LEFT JOIN supporting_doctors sd ON a.admission_id = sd.admission_id " +
                "WHERE (e.employee_id = ? OR sd.doctor_id = ?) " +
                "AND a.discharge_date IS NULL";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(assignedAdmissionsSQL)) {

            statement.setInt(1, doctorID);
            statement.setInt(2, doctorID);

            try (ResultSet resultSet = statement.executeQuery()) {
                System.out.println("Patients assigned to Doctor ID " + doctorID + ":");
                while (resultSet.next()) {
                    int admissionID = resultSet.getInt("admission_id");
                    int patientId = resultSet.getInt("patient_id");
                    String patientFirstName = resultSet.getString("patient_first_name");
                    String patientLastName = resultSet.getString("patient_last_name");
                    String admissionDate = resultSet.getString("admission_date");
                    String initialDiagnosis = resultSet.getString("initial_diagnosis");
                    String doctorFirstName = resultSet.getString("doctor_first_name");
                    String doctorLastName = resultSet.getString("doctor_last_name");
                    String treatmentNotes = resultSet.getString("treatment_notes");

                    // print to terminal
                    System.out.printf("Admission ID: %d, Patient ID: %d, Name: %s %s, Admission Date: %s, Diagnosis: %s, Treatment Notes: %s, Primary Doctor: %s %s%n",
                            admissionID, patientId, patientFirstName, patientLastName, admissionDate, initialDiagnosis,
                            (treatmentNotes != null ? treatmentNotes : "No treatment notes"),
                            doctorFirstName, doctorLastName);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error retrieving doctor's patients: " + e.getMessage());
            e.printStackTrace();
        }
    } // showAssignedAdmissions

    public void setRoomToVacant(int patientID) {
        // execute statement
        String setToUnoccupied = "UPDATE room r " +
                "JOIN admission a ON r.room_number = a.room_number " +
                "SET r.is_occupied = 0 " +
                "WHERE a.patient_id = ?";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(setToUnoccupied)) {

            statement.setInt(1, patientID);

            int rowsUpdated = statement.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("Room associated with patient ID " + patientID + " is now vacated.");
            } else {
                System.out.println("No room found for patient ID " + patientID + " to vacate.");
            }

        } catch (SQLException e) {
            System.err.println("Error updating room status: " + e.getMessage());
            e.printStackTrace();
        }
    } // End of setRoomToVacant

    public boolean isHospitalFull() {
        // execute statement
        String findVacantRoomSQL = "SELECT COUNT(*) AS occupied_count FROM room WHERE is_occupied = 1";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(findVacantRoomSQL);) {

            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                int occupiedCount = resultSet.getInt("occupied_count");

                //debug purposes
                System.out.println("Number of occupied rooms: " + occupiedCount);

                return occupiedCount == 20;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    } // End of isHospitalFull

    public int getVacantRoomNumber() {
        // execute statement
        String findVacantRoomSQL = "SELECT room_number FROM room WHERE is_occupied = 0 LIMIT 1";

        try(Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
            PreparedStatement statement = connection.prepareStatement(findVacantRoomSQL)) {

            ResultSet resultSet = statement.executeQuery();
            if(resultSet.next()) {
                int vacantRoomNumber = resultSet.getInt("room_number");

                //debug purposes
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
        // execute statement
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

    public boolean verifyCredentials(String[] employeeCredentials) {
        int employee_id = Integer.parseInt(employeeCredentials[0]);

        // execute statement
        String sql = "SELECT * FROM employee WHERE employee_id = ?";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, employee_id);
            ResultSet resultSet = statement.executeQuery();

            if(resultSet.next()) {
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
}
