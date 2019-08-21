package cs157a_proj;

//STEP 1: import required packages
import java.sql.*;
import java.util.Scanner;

/*
 * Extends the User class, imitates the portal for an admin.
 */
public class Administrator extends User {

	/**
	 * Pass the constructor to superclass.
	 * 
	 * @param accountID provided ID of the administrator
	 */
	Administrator(int accountID) {
		super(accountID);
	}

	/**
	 * Prints the menu options, including Admin actions, for terminal commands.
	 */
	@Override
	public void printMenu() {
		System.out.println("Functions available to bank users and administrators:\n"
				+ "Sign-out: sign-out\n"
				+ "View your Balance: balance\n"
				+ "Depositing to your account: deposit amount\n"
				+ "Withdrawing from your account: withdraw amount\n"
				+ "Transferring Money to Another Account: transfer amount destinationAccountID\n"
				+ "View your Last Log-In: lastLogIn\n"
				+ "Changing your Contact Email: changeEmail newEmail\n"
				+ "Changing your Contact Phone Number: changePhoneNumber newPhoneNumber\n"
				+ "Changing your password: changePassword newPassword\n"
				+ "Viewing your last activity: lastActivity\n"
				+ "View Number of Bank Users: viewnumberofbankusers\n"
				+ "View total money in the Bank: totalmoney\n"
				+ "Upgrade an Account to an Admin: upgrade usernameToBeUpgraded\n"
				+ "Downgrade an Account from an Admin: downgrade usernameToBeDowngraded\n"
				+ "Show all Admins: showadmins\n"
				+ "Delete an Account: deleteAccount userNameToBeDeleted\n"
				+ "Return Customer Email List: listofemails\n"
				+ "Return Customer Phone List: listofphonenumbers\n"
				+ "Find Higher than Average Savers: findHighSavers\n"
				+ "Find High Savers by Age Range: highSaversByAge lowAge highAge\n"
				+ "Find High Customer Usage by Age: findCusUsage numberOfCustomerThreshold\n"
				+ "Find Inactive Users: findInactiveUsers\n"
				+ "Archiving History Table (date must be in fromat YYYY-MM-DD and includes only timestamps before this day for copying over): archiveHistory YYYY-MM-DD"
				+ "Show Archived history: showArchivedHistory"
				);
	}

	/**
	 * Upgrades an account to a normal user.
	 * 
	 * @param targetUser username of the user to be upgraded.
	 */
	public void downgradeAccount(String targetUser)
	{
		int targetID = findIDByUsername(targetUser);
		try {
			String sql = "CALL downgradeAccount(?);";
			PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setInt(1, targetID);
			stmt.execute();
			
			String action = "Downgraded User with ID: " + targetID;
			updateHistoryTable(action);
			
			System.out.println("User ID: " + targetID + ", Username: " + targetUser + " was downgraded to a normal user.");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Archives from the history table to history archive table. The cut-off date is not included.
	 * If date 2019-07-27 is the cut-off, actions on that date stay in historyTable
	 * meanwhile everything done on 07-26-2019 and before gets archived and deleted.
	 * 
	 * @param date the cut-off date requested by the caller
	 */
	public void archiveHistory(String date)
	{
		try {
			String sql = "CALL archiveFromHistorytoArchiveHistoryTable(?);";
			PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setString(1, date);
			stmt.execute();
			
			System.out.println("History data on and after " + date + " have been archived.");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Downgrades an account to a normal user.
	 * 
	 * @param targetUser username of the user to be downgraded.
	 */
	public void upgradeAccount(String targetUser)
	{
		int targetID = findIDByUsername(targetUser);
		try {
			String sql = "CALL upgradeAccount(?);";
			PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setInt(1, targetID);
			stmt.execute();
			
			String action = "Upgraded User with ID: " + targetID;
			updateHistoryTable(action);
			
			System.out.println("User ID: " + targetID + ", Username: " + targetUser + " was upgraded to an admin.");
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
	 * Views the total amount of bank users which is printed in the terminal.
	 */
	public void viewNumberOfBankUsers()
	{
		try {
			String sql = "CALL findNumberOfBankUsers();";
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			rs.next();
			String num = rs.getString(1);
			System.out.println("Total number of users: " + num);
		}
		catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * View the money of all users combined money in the bank. Prints this in the terminal.
	 */
	public void viewTotalMoneyInBank()
	{
		try {
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("CALL viewTotalMoneyInBank()");
			rs.next();
			String sum = rs.getString(1);
			System.out.println("Total money in bank: $"+ sum);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Prints in the terminal all the bank's admins.
	 */
	public void showAllAdmins()
	{
		try {

			Statement stmt = conn.createStatement();

			String sql = "CALL viewAllAdmins();";
			ResultSet rs = stmt.executeQuery(sql);

			if (stmt.execute(sql)) {
				rs = stmt.getResultSet();
			}

			ResultSetMetaData rsmd = rs.getMetaData();
			int columnsNumber = rsmd.getColumnCount();
			System.out.println("Below is every administrator:");
			System.out.println("Username\tLast Name\tFirst Name");
			while (rs.next()) {
				for (int i = 1; i <= columnsNumber; i++) {
					String columnValue = rs.getString(i);
					System.out.print(columnValue + "\t\t");
				}
				System.out.println("");
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Retrieves the User's balance and prints it in the terminal.
	 */
	public int balanceInquiry(int targetID)
	{
		try {
			String sql = "CALL showBalance(?);";
			PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setInt(1, targetID);
			stmt.execute();

			int balance = -1;

			ResultSet rs = stmt.executeQuery();

			if (rs.next()) {
				balance = rs.getInt(1);
			}

			return balance;
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		return -1;
	}

	/**
	 * Deletes the specified account.
	 * 
	 * @param targetID the ID number of the user to be deleted
	 */
	public void deleteAccount(String targetUser)
	{
		int targetID = findIDByUsername(targetUser);
		//System.out.println(targetID);
		//System.out.println();
		
		if (balanceInquiry(targetID) != 0)
		{
			System.out.println("You cannot delete an account with money still in the bank. Please ask user to withdraw all funds then try again.");
			return;
		}

		try {
			String sql = "CALL testIfIDExists(" + targetID + ");";
			Statement sqlStatement = conn.createStatement();
			sqlStatement.execute(sql);
			ResultSet resultSet = sqlStatement.getResultSet();
			boolean recordFound = resultSet.next();
			if (recordFound) {
				String sql2 = "CALL deleteUserAccount(?)";
				PreparedStatement stmt = conn.prepareStatement(sql2);
				stmt.setInt(1, targetID);
				stmt.executeUpdate();
				
				String action = "Deleted User with ID: " + targetID;
				updateHistoryTable(action);
				
				System.out.println("deleting AccountID " + targetID + " was successful");
			}
			else {
				System.out.println("deleting AccountID " + targetID + "  was not successful since this accountID does not exist.");
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Prints all the names of users with their email.
	 */
	public void returnEmailList()
	{
		try {

			Statement stmt = conn.createStatement();
			String sql = "CALL viewEmailList()";
			ResultSet rs = stmt.executeQuery(sql);
			if (stmt.execute(sql)) {
				rs = stmt.getResultSet();
			}
			ResultSetMetaData rsmd = rs.getMetaData();
			int columnsNumber = rsmd.getColumnCount();
			System.out.println("Below is list of users with their email address:");
			System.out.println("FName\tLName\tEmail");
			while (rs.next()) {
				for (int i = 1; i <= columnsNumber; i++) {
					String columnValue = rs.getString(i);
					System.out.print(columnValue + "\t\t");
				}
				System.out.println(" ");
			}

		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Prints all the names of users with their phone numbers.
	 */
	public void returnPhoneList()
	{
		try {

			Statement stmt = conn.createStatement();
			String sql = "CALL viewPhoneList()";
			ResultSet rs = stmt.executeQuery(sql);

			if (stmt.execute(sql)) {
				rs = stmt.getResultSet();
			}

			ResultSetMetaData rsmd = rs.getMetaData();
			int columnsNumber = rsmd.getColumnCount();
			System.out.println("Below is list of users with their phone numbers:");
			System.out.println("FName\t\tLName\t\tPhone Number");
			while (rs.next()) {
				for (int i = 1; i <= columnsNumber; i++) {
					String columnValue = rs.getString(i);
					System.out.print(columnValue + "\t\t");
				}
				System.out.println(" ");
			}

		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Find Higher than Average Savers by Age (correlated subquery)
	 * idea is that you can offer school loans to richest people of college age, home improvement loans to richest people of middle age, etc.
	 * 
	 * @param targetAgeLow the low end of the age to target
	 * @param targetAgeHigh the low end of the age to target
	 */
	public void findHighSaversByAge(int targetAgeLow, int targetAgeHigh)
	{
		try {	
			String sql = "CALL findHighSaversByAgeRange(?, ?);";
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, targetAgeLow);
			pstmt.setInt(2, targetAgeHigh);

			ResultSet rs = pstmt.executeQuery();

			if (pstmt.execute()) {
				rs = pstmt.getResultSet();
			}

			ResultSetMetaData rsmd = rs.getMetaData();
			int columnsNumber = rsmd.getColumnCount();
			System.out.println("Below is list of high savers by specific age group:");
			System.out.println("ID\tFName\tLName\tage\tbalance");

			while (rs.next()) {
				for (int i = 1; i <= columnsNumber; i++) {
					String columnValue = rs.getString(i);
					System.out.print(columnValue + "\t");
				}

				System.out.println(" ");
			}
		}

		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Prints out details of the users who have more savings than average.
	 */
	public void findHighSavers() {
		
		try {	
			Statement stmt = conn.createStatement();

			String sql = "call findHighSavers();";
			ResultSet rs = stmt.executeQuery(sql);

			if (stmt.execute(sql)) {
				rs = stmt.getResultSet();
			}

			ResultSetMetaData rsmd = rs.getMetaData();
			int columnsNumber = rsmd.getColumnCount();
			System.out.println("Below is every user with more savings than average:");
			System.out.println("AccountID\tFirst Name\tLast Name\tBalance");
			while (rs.next()) {
				for (int i = 1; i <= columnsNumber; i++) {
					String columnValue = rs.getString(i);
					System.out.print(columnValue + "\t\t");
				}
				System.out.println("");
			}
			
			
		}

		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Prints out by age group with a number of users above the threshold.
	 */
	public void findHighCusUsageByAge(int thres) {
		try {	
			String sql = "CALL returnHighCustomerUsageByAge (?);";
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, thres);

			ResultSet rs = pstmt.executeQuery();

			if (pstmt.execute()) {
				rs = pstmt.getResultSet();
			}

			ResultSetMetaData rsmd = rs.getMetaData();
			int columnsNumber = rsmd.getColumnCount();
			System.out.println("Below is list of age groups with customers above the threshold:");
			System.out.println("Number of Users\tAge Group");

			while (rs.next()) {
				for (int i = 1; i <= columnsNumber; i++) {
					String columnValue = rs.getString(i);
					System.out.print(columnValue + "\t");
				}

				System.out.println(" ");
			}
		}

		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Prints out users who made an account but never used it.
	 * 
	 * Uses the set operation of difference.
	 */
	public void inActiveUsers()
	{
		try {

			Statement stmt = conn.createStatement();

			//difference in mySQL since minus/difference word is not supported
			String sql = "CALL findInactiveUsers();";

			ResultSet rs = stmt.executeQuery(sql);

			if (stmt.execute(sql)) {
				rs = stmt.getResultSet();
			}

			ResultSetMetaData rsmd = rs.getMetaData();
			int columnsNumber = rsmd.getColumnCount();
			System.out.println("Below is the list of inactive users:");
			while (rs.next()) {
				for (int i = 1; i <= columnsNumber; i++) {
					//if (i > 1) System.out.print(",  ");
					String columnValue = rs.getString(i);
					System.out.print(columnValue + "\t\t");
				}
				System.out.println("");
			}

		}
		catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	/**
	 * Prints out all the data in the command line from history archive table.
	 */
	public void showArchivedHistory() {
		try {

			Statement stmt = conn.createStatement();

			//difference in mySQL since minus/difference word is not supported
			String sql = "CALL findArchivedHistory();";

			ResultSet rs = stmt.executeQuery(sql);

			if (stmt.execute(sql)) {
				rs = stmt.getResultSet();
			}

			ResultSetMetaData rsmd = rs.getMetaData();
			int columnsNumber = rsmd.getColumnCount();
			System.out.println("Below is the archived history:");
			System.out.println("AccountID\t\tActionDesc\t\tTime");

			while (rs.next()) {
				for (int i = 1; i <= columnsNumber; i++) {
					String columnValue = rs.getString(i);
					System.out.print(columnValue + "\t\t");
				}

				System.out.println(" ");
			}

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
		Scanner in3 = new Scanner(System.in);

		System.out.println("Welcome to your account, remember to type 'help' to see your options");

		boolean shouldBreak = false;

		while (shouldBreak == false)
		{
			System.out.print(">");
			String inString = in3.nextLine();
			switch (parseOnSpaces(inString.toLowerCase())[0]) {
			case "help":
				printMenu();
				break;
			case "sign-out":
				//signOutProcedure();
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
			case "downgrade":
				downgradeAccount(parseOnSpaces(inString)[1]);
				break;
			case "upgrade":
				upgradeAccount(parseOnSpaces(inString)[1]);
				break;
			case "showadmins":
				showAllAdmins();
				break;
			case "viewnumberofbankusers":
				viewNumberOfBankUsers();
				//System.out.print("total number of user is "+viewNumberOfBankUsers());
				break;
			case "totalmoney":
				viewTotalMoneyInBank();
				break;
			case "deleteaccount":
				deleteAccount(parseOnSpaces(inString)[1]);
				break;
			case"listofemails":
				returnEmailList();
				break;
			case "listofphonenumbers":
				returnPhoneList();
				break;
			case  "highsaversbyage":
				findHighSaversByAge(Integer.parseInt(parseOnSpaces(inString)[1]), Integer.parseInt(parseOnSpaces(inString)[2]));
				break;
			case "findhighsavers":
				findHighSavers();
				break;
			case "findcususage":
				findHighCusUsageByAge(Integer.parseInt(parseOnSpaces(inString)[1]));
				break;
			case "findinactiveusers":
				inActiveUsers();
				break;
			case "archivehistory":
				archiveHistory(parseOnSpaces(inString)[1]);
				break;
			case "showarchivedhistory":
				showArchivedHistory();
				break;
			default: 
				System.out.println("Please check your syntax or sign-out by typing 'sign-out'.");
				System.out.println("Input received: " + inString);
				break;
			}
		}

		System.out.println("You are signed-out of your account.");
	}
}
