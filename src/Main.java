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

    // This will have a switch statement and depending on the role their operations vary
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

    // Set of operations for a Doctor
    public static void doctorDashboard(String[] employeeCredentials, Scanner scan, PrimaryCareDB database) {
        boolean isRunning = true;
        while(isRunning) {
            // Prompt
            System.out.println("Here is a list of your db operations Doctor");
            System.out.println("1) Register a new patient"); //
            System.out.println("2) List all your patients");
            System.out.println("3) Assign a non-primary doctor to your existing patient");
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
                            Integer.parseInt(employeeCredentials[0]),
                            patientFirstName,
                            patientLastName,
                            patientDOB,
                            emergencyContactName,
                            emergencyPhoneNumber,
                            insurancePolicyNumber
                    );

                    break;
                case "2":
                    System.out.println("Listing all your pateints\n");

                    // Invoke databse operation to list all of doctor's patients
                    break;
                case "3":
                    System.out.println("Assigning a non-primary doctor to your existing patient\n");
                    break;
                case "4":
                    System.out.println("Ordering and Perfroming treatment to your patient\n");
                case "5":
                    System.out.println("Setting discharge date of a patient\n");
                    break;
                case "q":
                    System.out.println("Logging out, have a nice day!\n");
                    isRunning = false;
                    break;
                default:
                    System.out.println("Invalid option, please try again\n");
                    break;
            }
        }
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