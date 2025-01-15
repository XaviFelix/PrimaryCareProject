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
        employeeDashboard(employeeCredentials[3]);


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
    public static void employeeDashboard(String employeeRole) {
        System.out.println("\nWelcome to the White Memorial Primary Care Hospital");
        boolean isRunning = true;
        while (isRunning) {
            switch (employeeRole) {
                case "Doctor":
                    System.out.println("Here is a list of your db operations Doctor");
                    isRunning = false;
                    break;
                case "Admin":
                    System.out.println("Here is a list of your db operations Admin");
                    isRunning = false;
                    break;
                default:
                    System.out.println("Something went wrong with user's role, exiting db system");
                    isRunning = false;
            }
        }
    }

    // Set of operations for a Doctor
    public static void doctorPrompt() {
        System.out.println("Here is a list of your db operations Doctor");
        System.out.println("1) Register a new patient");
        System.out.println("2) List all your patients");
        System.out.println("3) Assign a non-primary doctor to your existing patient");
        System.out.println("4) Order and Perform treatment for your patient");
        System.out.println("5) Set discharge date of a patient");
    }

    // Set of operations for an Admin
    public static void adminPrompt() {
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