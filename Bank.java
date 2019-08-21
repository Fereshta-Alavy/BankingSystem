package cs157a_proj;

//STEP 1: import required packages
import java.sql.*;
import java.util.Scanner;


/*
 * A banking program which relies on terminal interaction from users.
 * Uses JDBC to connect to a MySQL database to run database operations.
 * 
 * @author Team Nine, which includes Byas Dhungana, Fereshta Alavy, and Fotios Dimitropoulos
 */
public class Bank {
	//JDBC driver name and database URL
	//Added ?serverTimezone=UTC to suppress time zone exception
	static final String DB_URL = "jdbc:mysql://localhost/bankingSystem?serverTimezone=UTC";

	//Database credentials, would be different based on host machine
	static final String USER = "root";
	static final String PASS = "12345678";

	//Keeping the connections global for use by all methods
	Connection conn = null;
	Statement stmt = null;
	ResultSet rs = null;

	/**
	 * Opens the connection to the database through JDBC drivers.
	 */
	public void openConnection()
	{
		try {
			//STEP 2: Register JDBC driver (automatically done since JDBC 4.0)

			//STEP 3: Open a connection
			System.out.println("Connecting to database...");
			conn = DriverManager.getConnection(DB_URL, USER, PASS);
		}
		catch(SQLException se) {
			//Handle errors for JDBC
			se.printStackTrace();
		}catch(Exception e) {
			//Handle errors for Class.forName
			e.printStackTrace();
		}
	}

	/**
	 * Closes the connection to the database through JDBC drivers.
	 */
	public void closeConnection()
	{
		try {
			if(stmt!=null) {
				stmt.close();
			}
		}catch(SQLException se2) {
			se2.printStackTrace();
		}
		try {
			if(conn!=null) {
				conn.close();
			}
		}catch(SQLException se) {
			se.printStackTrace();
		}

		System.out.println("Disconnected from the database.");
	}

	/**
	 * Parses string on spaces and returns it in an array.
	 * Example, if you input "apple pear orange"
	 * Returns ["apple", "pear", "orange"]
	 * 
	 * @params str string you would like to be parsed
	 */

	public static String[] parseOnSpaces(String str)
	{
		String[] splited = str.split(" ");
		return splited;
	}

	/**
	 * Creates a customer's account by inserting on customerTable in MySQL.
	 * 
	 * @param str the terminal string from the user
	 */
	private void createCustomerAccount(String str)
	{
		String[] cmds = parseOnSpaces(str);

		try {
			stmt = conn.createStatement();

			String largestLookUp = "CALL largestLookUp();";
			int maxID = -1;

			ResultSet rs = stmt.executeQuery(largestLookUp);

			if (rs.next()) {
				maxID = rs.getInt(1);
			}

			String sql = " CALL createAccount (?,?,?,?,?,?);";
			PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setInt(1, maxID + 1);
			stmt.setString(2, cmds[1]);
			stmt.setString(3, cmds[2]);
			stmt.setString(4, cmds[3]);
			stmt.setString(5, cmds[4]);
			stmt.setString(6, cmds[5]);

			stmt.executeUpdate();

			System.out.println("Your account has been created, please remember your account ID: " + (maxID + 1));
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Prints a menu of hints for banking actions
	 */
	public void printHelp() {
		System.out.println("Functions available to bank users:\n"
				+ "1. Creating a user account: createAccount firstname lastname username password age\n"
				+ "2. Signing-in to your account: sign-in Username Password\n"
				+ "3. Closing the Bank program: quit\n");
	}


	public void updateLastSignIn(String username) {
		try {
			int id = findIDByUsername(username);
			String sql = "CALL updateSignIn(?);";
			PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setInt(1, id);
			stmt.execute();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Begins the sign-in procedure and calls the correct class based on admin vs. user
	 * 
	 * @param user the username provided during log-in
	 * @param pw the password provided during log-in
	 */
	public void signInProcedure(String user, String pw) {
		Boolean pwCheck = checkPassword(user, pw);
		if (pwCheck == false)
		{
			System.out.println("Username and password do not match. Sign-in has failed.");
			return;
		}


		System.out.println("Sign-in was successful. Remember to type 'help' to see the menu.");
		updateLastSignIn(user);
		Boolean admin = checkIfAdmin(user);

		if (admin)
		{
			Administrator ad = new Administrator(findIDByUsername(user));
			ad.operateBank();

		}
		else
		{
			User u = new User(findIDByUsername(user));
			u.operateBank();
		}
	}

	/**
	 * Checks if a given username is associated with admin abilities
	 * 
	 * @param user the username of target user
	 * @return true if they are an admin, otherwise false
	 */
	public boolean checkIfAdmin(String user)
	{
		int adminCheck = 0;

		try {
			int id = findIDByUsername(user);
			String sql = "CALL checkIfAdmin(?);";
			PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setInt(1, id);
			stmt.execute();
			ResultSet rs = stmt.executeQuery();

			if (rs.next()) {
				adminCheck = rs.getInt(1);
				if (adminCheck == 1)
				{
					return true;
				}					}
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	/**
	 * Returns the ID number of a user by username.
	 * 
	 * @param user the username of the target user
	 * @return an int of the accountID of a specified user
	 */
	public int findIDByUsername(String user) {
		int id = -1;
		try {
			String sql = "CALL findIDByUsername(?);";
			PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setString(1, user);
			stmt.execute();
			ResultSet rs = stmt.executeQuery();

			if (rs.next()) {
				id = rs.getInt(1);
				return id;
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		return id;
	}

	/**
	 * Checks if a password is correct according to database records.
	 * 
	 * @param user the username provided
	 * @param pw the password provided
	 * @return true if the password/username combination is correct, otherwise false
	 */
	public boolean checkPassword(String user, String pw)
	{
		String pwCheck = "";

		try {
			String sql = "CALL retrievePW(?);";
			PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setString(1, user);
			stmt.execute();
			ResultSet rs = stmt.executeQuery();

			if (rs.next()) {
				pwCheck = rs.getString(1);
				if (pwCheck.equals(pw))
				{
					return true;
				}					}
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}


	/**
	 * Main method acts like controller for program
	 * Opens the JDBC connection and reads user input
	 * 
	 * @param args command line arguments if provided by user (not used)
	 */
	public static void main(String[] args) {

		Bank bank = new Bank();
		bank.openConnection();

		// Using scanner for getting input from user 
		Scanner in = new Scanner(System.in);

		System.out.println("Welcome to the Banking System, remember to type 'help' to see your options");

		boolean shouldBreak = false;

		while (shouldBreak == false)
		{
			System.out.print(">");
			String inString = in.nextLine();
			switch (parseOnSpaces(inString.toLowerCase())[0]) {
			case "help":
				bank.printHelp();
				break;
			case "quit":
				shouldBreak = true;
				break;
			case "createaccount":
				bank.createCustomerAccount(inString);
				break;
			case "sign-in":
				bank.signInProcedure(parseOnSpaces(inString)[1], parseOnSpaces(inString)[2]);
				break;
			default: 
				System.out.println("Please check your syntax or type 'quit' to quit.");
				System.out.println("Input received: " + inString);
				break;
			}
		}
		
		in.close();
		bank.closeConnection();
		System.out.println("Bank program is terminated.");


	}//end main

}//end class Bank
