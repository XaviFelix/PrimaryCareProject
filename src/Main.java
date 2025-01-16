import java.util.Scanner;

// This will be the main driver program
public class Main {
    public static void main(String[] args) {
        // Connection to the database
        PrimaryCareDB database = new PrimaryCareDB();
        primaryCareUI(database);

    }

    public static void primaryCareUI(PrimaryCareDB database) {
        // Login the user (employee)
        boolean isAuthenticated = false;
        String[] employeeCredentials = new String[4];
        while(!isAuthenticated) {
            employeeCredentials = getEmployeeCredentials(); // THis needs to be outside so that i can pass it to the dashboard
            isAuthenticated = database.verifyCredentials(employeeCredentials);
            if(!isAuthenticated) {
                System.out.println("Invalid credentials. Enter them again or exit program 'ctrl + C'");
            }
        }
        System.out.println("Login successful!");

        // Route user (employee) to their dashboard
        employeeDashboard(employeeCredentials, database);


    } // End of primaryCareUI()

    // Purpose: returns an array string containing employee credentials
    public static String[] getEmployeeCredentials() {
        // Welcome prompt for employee
        Scanner scan = new Scanner(System.in);
        System.out.println("Welcome to the Primary Care interface\n");

        // Get ID
        System.out.print("Enter Employee ID: ");
        String id = scan.nextLine();

        // Get First Name
        System.out.print("Enter your first name: ");
        String firstName = scan.nextLine();

        // Get Last Name
        System.out.print("Enter your last name: ");
        String lastName = scan.nextLine();

        // Get Employee Role
        System.out.print("Enter your Role: ");
        String role = scan.nextLine();

        return new String[] {id, firstName, lastName, role};

    } // End of getEmployeeCredentials

    public static void employeeDashboard(String[] employeeCredentials, PrimaryCareDB database) {
        System.out.println("\nWelcome to the White Memorial Primary Care Hospital");
        boolean isRunning = true;
        Scanner scan = new Scanner(System.in);
        while (isRunning) {
            switch (employeeCredentials[3]) {
                case "Doctor":
                    doctorDashboard(employeeCredentials, scan, database);
                    isRunning = false;
                    break;
                case "Admin":
                    adminDashboard(scan);
                    isRunning = false;
                    break;
                default:
                    System.out.println("Something went wrong with user's role, exiting db system");
                    isRunning = false;
            }
        }
    } // End of employeeDashboard

    public static void doctorDashboard(String[] employeeCredentials, Scanner scan, PrimaryCareDB database) {
        boolean isRunning = true;
        int currentDoctorID = Integer.parseInt(employeeCredentials[0]);
        while(isRunning) {
            // Prompt
            System.out.println("\nHere is a list of your db operations Doctor");
            System.out.println("1) Register a new patient"); //
            System.out.println("2) List all your assigned admissions");
            System.out.println("3) Assign a supporting doctor to your existing patient");
            System.out.println("4) Order and Perform treatment for your patient");
            System.out.println("5) Set discharge date of a patient");
            System.out.println("Press q to log out");

            // Operations
            String choice = scan.nextLine();
            switch (choice) {
                case "1":
                    System.out.println("Registering new patient\n");

                    System.out.println("What is your patient's first name: ");
                    String patientFirstName = scan.nextLine();

                    System.out.println("What is your patient's last name: ");
                    String patientLastName = scan.nextLine();

                    System.out.println("What is your patient's date of birth: ");
                    String patientDOB = scan.nextLine();

                    System.out.println("What is their emergency contact's full name: ");
                    String emergencyContactName = scan.nextLine();

                    System.out.println("What is their emergency phone number (FORMAT: xxx-xxx-xxxx)");
                    String emergencyPhoneNumber = scan.nextLine();

                    System.out.println("What is your patient's insurance policy number: ");
                    String insurancePolicyNumber = scan.nextLine();

                    // Invoke database operation to insert patient
                    database.insertNewPatient(
                            currentDoctorID,
                            patientFirstName,
                            patientLastName,
                            patientDOB,
                            emergencyContactName,
                            emergencyPhoneNumber,
                            insurancePolicyNumber
                    );

                    break;
                case "2":
                    System.out.println("\nListing all your pateints\n");
                    database.showAssignedAdmissions(currentDoctorID);
                    break;
                case "3":
                    // Needs testing
                    System.out.println("\nAssigning a supporting doctor, enter admission id: ");
                    int admissionID = Integer.parseInt(scan.nextLine());
                    System.out.println("Enter a doctor id to assign: ");
                    int doctorID = Integer.parseInt(scan.nextLine());
                    database.assignSupportingDoctor(admissionID, doctorID);
                    break;
                case "4":
                    System.out.println("\nOrdering and Perfroming treatment to your patient\n");
                    System.out.println("Enter the ID of the patient you want to perform treatment to: ");
                    int patientIdTreatment = choosePatientID(scan);
                    String treatmentType = getTreatmentType(scan);
                    database.orderTreatment(currentDoctorID, patientIdTreatment, treatmentType);
                    break;
                case "5":
                    System.out.println("\nSetting discharge date of a patient\n");

                    System.out.println("Enter the ID of the patient you want to discharge: ");
                    int patientIdDischarge = choosePatientID(scan);
                    database.dischargePatient(patientIdDischarge);
                    break;
                case "q":
                    System.out.println("\nLogging out, have a nice day!\n");
                    isRunning = false;
                    break;
                default:
                    System.out.println("\nInvalid option, please try again\n");
                    break;
            }
        }
    }

    public static String getTreatmentType(Scanner scan) {
        while (true) {
            System.out.println("Enter treatment type (Procedure or Medication):");
            String treatmentType = scan.nextLine().trim();

            if (treatmentType.equalsIgnoreCase("Procedure") || treatmentType.equalsIgnoreCase("Medication")) {
                return treatmentType.substring(0, 1).toUpperCase() + treatmentType.substring(1).toLowerCase();
            } else {
                System.out.println("Invalid treatment type. Please enter 'Procedure' or 'Medication'.");
            }
        }
    }

    public static int choosePatientID(Scanner scan) {
        return Integer.parseInt(scan.nextLine());
    }

    // Set of operations for an Admin
    public static void adminDashboard(Scanner scan) {
        System.out.println("Here is a list of your db operations Admin");
        System.out.println("1) Order a treatment for your patient"); // timestamp is associated with the order
        System.out.println("2) List all your patients");
        System.out.println("3) Discharge a patient");
    }

    // General set of operations for every type of employee
//    public static void generalPrompt() {
//
//    }
}