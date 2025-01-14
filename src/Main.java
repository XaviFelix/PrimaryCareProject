import java.util.Scanner;

// This will be the main driver program
public class Main {
    public static void main(String[] args) {
        // Connection to the database
        PrimaryCareDB database = new PrimaryCareDB();
        primaryCareUI(database);

    }

    public static void primaryCareUI(PrimaryCareDB database) {
        boolean isAuthenticated = false;
        while(!isAuthenticated) {
            String[] employeeCredentials = getEmployeeCredentials();
            isAuthenticated = database.verifyCredentials(employeeCredentials);
            if(!isAuthenticated) {
                System.out.println("Invalid credentials. Enter them again or exit program 'ctrl + C'");
            }
        }
        System.out.println("Login successful!");
        // Here we are finally in the database, start adding functinality here
        //database.insertNewEmployee("Marcus", "Felix", "Doctor", "2025-01-15");
        database.showEmployeeByID(2);

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


}