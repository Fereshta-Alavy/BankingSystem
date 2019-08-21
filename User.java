package cs157a_proj;

//STEP 1: import required packages
import java.sql.*;
import java.util.Scanner;

/*
 * Imitates the portal for a normal user.
 */
public class User {
	public int accountID;
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
	 * Constructor for the class.
	 * 
	 * @param accountID the ID for this User
	 */
	User (int accountID)
	{
		this.accountID = accountID;
	}

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
	 * Prints the menu options for terminal commands.
	 */
	public void printMenu() {
		System.out.println("Functions available to bank users:\n"
				+ "1. Sign-out: sign-out\n"
				+ "2. View your Balance: balance\n"
				+ "3. Depositing to your account: deposit amount\n"
				+ "4. Withdrawing from your account: withdraw amount\n"
				+ "5. Transferring Money to Another Account: transfer amount destinationAccountID\n"
				+ "6. View your Last Log-In: lastLogIn\n"
				+ "7. Changing your Contact Email: changeEmail newEmail\n"
				+ "8. Changing your Contact Phone Number: changePhoneNumber newPhoneNumber\n"
				+ "9. Changing your password: changePassword newPassword\n"
				+ "10. Viewing your last activity: lastActivity\n"
				);
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
	 * Retrieves the User's balance and prints it in the terminal.
	 */
	public void showBalance()
	{
		try {
			String sql = "CALL showBalance(?);";
			PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setInt(1, this.accountID);
			stmt.execute();

			int balance = -1;

			ResultSet rs = stmt.executeQuery();

			if (rs.next()) {
				balance = rs.getInt(1);
			}

			System.out.println("Your balance is $" + balance);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Deposits into this User's account
	 **/
	protected void depositToAccount(int amount)
	{
		if (amount <= 0)
		{
			System.out.println("Cannot complete deposit: Please enter an amount greater than 0.");
			return;
		}
		
		PreparedStatement pstmt = null;

		try {
			String sql = "CALL depositToAccount(?, ?);";
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, amount);
			pstmt.setInt(2, this.accountID);
			pstmt.executeUpdate();

			String action = "deposited $" + amount;
			updateHistoryTable(action);
			System.out.println("Deposit was successful!");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Updates the history table for the user with the given action description.
	 */
	public void updateHistoryTable(String actionDesc) {
		PreparedStatement pstmt = null;

		try {
			String sql = "CALL updateHistory(?, ?);";
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, this.accountID);
			pstmt.setString(2, actionDesc);
			pstmt.executeUpdate();

		}
		catch (Exception e) {
			e.printStackTrace();
		}
    }
	
	/**
	 * Updates the history table for the user with the given action description.
	 */
	public void updateHistoryTable(String actionDesc, int substituteID) {
		PreparedStatement pstmt = null;

		try {
			String sql = "CALL updateHistory(?, ?);";
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, substituteID);
			pstmt.setString(2, actionDesc);
			pstmt.executeUpdate();

		}
		catch (Exception e) {
			e.printStackTrace();
		}
    }

	/**
	 * Prints the last Timestamp log-in of this User.
	 */
	public void viewLastLogIn()
	{
		try {
			String sql = "CALL viewLastLogIn(?);";
			PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setInt(1, accountID);
			stmt.execute();

			ResultSet rs = stmt.executeQuery();

			if (rs.next()) {
				Timestamp timestamp = rs.getTimestamp(1);
				java.util.Date date = timestamp;
				System.out.println(date);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Transfers money from this account to the target account
	 * 
	 * @param targetID account that money will go to
	 */
	public void transferToOtherAccount(int amount, int targetID)
	{
		if (amount <= 0)
		{
			System.out.println("Cannot complete transfer: Please enter an amount greater than 0.");
			return;
		}
		
		PreparedStatement pstmt = null;

		try {
			String sql = "CALL testIfIDExists(" + targetID + ");";
			Statement sqlStatement = conn.createStatement();
			sqlStatement.execute(sql);
			ResultSet resultSet = sqlStatement.getResultSet();
			boolean recordFound = resultSet.next();
			if (recordFound) {

				String sql1 = "CALL withdraw(?, ?);";

				pstmt = conn.prepareStatement(sql1);
				pstmt.setInt(1, amount);
				pstmt.setInt(2, this.accountID);
				pstmt.executeUpdate();


				String sql2 = "CALL depositToAccount(?, ?);";
				pstmt = conn.prepareStatement(sql2);
				pstmt.setInt(1, amount);
				pstmt.setInt(2, targetID);
				pstmt.executeUpdate();

				String sql3 = "COMMIT;";
				pstmt = conn.prepareStatement(sql3);
				pstmt.executeUpdate();

				String action = "Transferred $" + amount + " to AccountID " + targetID;
				updateHistoryTable(action);
				//System.out.println("Deposit was successful!");
				String action2 = "Received transfer for $" + amount + " from " + this.accountID;
				updateHistoryTable(action2, targetID);
				//updateHistoryTable(action2);
				
				System.out.println("Transfer was successful! $" + amount + " was withdrawn from Account " + this.accountID
						+ " and deposited to Account " + targetID);
			}
			else {
				System.out.println("Transfer was not successful because Account " + targetID+ " does not exist");
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Changes a user's email
	 * 
	 * @param newEmail the new email to be written into the db
	 */
	public void changeEmail(String newEmail)
	{
		try {
			String sql = "CALL updateEmail(?, ?);";
			PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setString(1, newEmail);
			stmt.setInt(2, this.accountID);
			stmt.executeUpdate();

			String action = "Security Update: email was changed to " + newEmail;
			updateHistoryTable(action);
			
			System.out.println("Eamil is changed!");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Changes a user's phone number.
	 * 
	 * @param newEmail the new email to be written into the db
	 */
	public void changePhoneNumber(String newPhone)
	{
		//String[] cmds = parseOnSpaces(newPhone);
		PreparedStatement pstmt = null;

		try {
			String sql = "CALL updatePhoneNumber(?, ?);";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, newPhone);
			pstmt.setInt(2, this.accountID);
			pstmt.executeUpdate();

			String action = "Security Update: phone number was changed to " + newPhone;
			updateHistoryTable(action);
			
			System.out.println("Phone Number was changed!");
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Changes a user's password.
	 * 
	 * @param newEmail the new password to be written into the db
	 */
	public void changePassword(String newPW)
	{
		//String[] cmds = parseOnSpaces(newPW);
		PreparedStatement pstmt = null;

		try {
			String sql = "CALL changePassword(?, ?);";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, newPW);
			pstmt.setInt(2, this.accountID);
			pstmt.executeUpdate();
			
			String action = "Security Update: password was changed";
			updateHistoryTable(action);
			
			System.out.println("Password was changed!");
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Prints the user's last activity into the terminal.
	 */
	public void viewLastActivity()
	{
		try {
			String sql = "CALL viewLastActivity(?);";
			PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setInt(1, accountID);
			stmt.execute();

			ResultSet rs = stmt.executeQuery();

			//String date = null;

			if (rs.next()) {
				Timestamp timestamp = rs.getTimestamp(1);
				java.util.Date date = timestamp;
				System.out.println(date);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Withdraws from this User's account.
	 **/
	protected void withdrawFromAccount(int amount)
	{
		if (amount <= 0)
		{
			System.out.println("Cannot complete withdrawal: Please enter an amount greater than 0.");
			return;
		}
		
		PreparedStatement pstmt = null;

		try {
			String sql = "CALL withdraw(?, ?);";
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, amount);
			pstmt.setInt(2, this.accountID);
			pstmt.executeUpdate();

			String action = "withdrew $" + amount;
			updateHistoryTable(action);
			System.out.println("Withdraw was successful!");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Interactive part of the User's portal.
	 */
	public void operateBank()
	{
		openConnection();

		// Using scanner for getting input from user 
		@SuppressWarnings("resource")
		Scanner in2 = new Scanner(System.in);

		System.out.println("Welcome to your account, remember to type 'help' to see your options");

		boolean shouldBreak = false;

		while (shouldBreak == false)
		{
			System.out.print(">");
			String inString = in2.nextLine();
			switch (parseOnSpaces(inString.toLowerCase())[0]) {
			case "help":
				printMenu();
				break;
			case "sign-out":
				shouldBreak = true;
				break;
			case "deposit":
				depositToAccount(Integer.parseInt(parseOnSpaces(inString)[1]));
				break;
			case "withdraw":
				withdrawFromAccount(Integer.parseInt(parseOnSpaces(inString)[1]));
				break;
			case "balance":
				showBalance();
				break;
			case "transfer":
				transferToOtherAccount(Integer.parseInt(parseOnSpaces(inString)[1]), Integer.parseInt(parseOnSpaces(inString)[2]));
				break;
			case "lastlogin":
				viewLastLogIn();
				break;
			case "changephonenumber":
				changePhoneNumber(parseOnSpaces(inString)[1]);
				break;
			case "changeemail":
				changeEmail(parseOnSpaces(inString)[1]);
				break;
			case "changepassword":
				changePassword(parseOnSpaces(inString)[1]);
				break;
			case "lastactivity":
				viewLastActivity();
				break;
			default: 
				System.out.println("Please check your syntax or sign-out by typing sign-out.");
				System.out.println("Input received: " + inString);
				break;
			}
		}

		System.out.println("You are signed-out of your account.");
	}
}
