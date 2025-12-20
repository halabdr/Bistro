package entities;

import java.io.Serializable;

/**
 * WaitlistEntry represents an entry in the restaurant's waitlist. When no
 * tables are immediately available, customers join the waitlist.
 *
 */
public class WaitlistEntry implements Serializable {

	private static final long serialVersionUID = 1L;

	private int entryId;
	private int userId;
	private String timestamp;
	private int numberOfGuests;
	private String confirmationCode;
	private String status;
	private Integer assignedTableNumber;

	/**
	 * Default constructor.
	 */
	public WaitlistEntry() {
	}

	/**
	 * Constructs a WaitlistEntry with all fields specified.
	 *
	 * @param entryId             Unique entry identifier
	 * @param userId              ID of the user joining waitlist
	 * @param timestamp           Time when entry was created
	 * @param numberOfGuests      Number of people in the party
	 * @param confirmationCode    Unique confirmation code
	 * @param status              Current status of the entry
	 * @param assignedTableNumber Table number if assigned (null if waiting)
	 */
	public WaitlistEntry(int entryId, int userId, String timestamp, int numberOfGuests, String confirmationCode,
			String status, Integer assignedTableNumber) {

		this.entryId = entryId;
		this.userId = userId;
		this.timestamp = timestamp;
		this.numberOfGuests = numberOfGuests;
		this.confirmationCode = confirmationCode;
		this.status = status;
		this.assignedTableNumber = assignedTableNumber;
	}

	/**
	 * Constructor for creating a new waitlist entry without table assignment.
	 * Status defaults to "WAITING" and assignedTableNumber to null. Used when
	 * customer first joins the waitlist.
	 *
	 * @param entryId          Unique entry identifier
	 * @param userId           ID of the user joining waitlist
	 * @param timestamp        Time when entry was created
	 * @param numberOfGuests   Number of people in the party
	 * @param confirmationCode Unique confirmation code
	 */
	public WaitlistEntry(int entryId, int userId, String timestamp, int numberOfGuests, String confirmationCode) {

		this(entryId, userId, timestamp, numberOfGuests, confirmationCode, "WAITING", null);
	}

	// Getters and Setters

	/**
	 * Gets the unique entry identifier.
	 *
	 * @return The entry ID
	 */
	public int getEntryId() {

		return entryId;
	}

	/**
	 * Sets the unique entry identifier.
	 *
	 * @param entryId The entry ID to set
	 */
	public void setEntryId(int entryId) {

		this.entryId = entryId;
	}

	/**
	 * Gets the ID of the user who joined the waitlist.
	 *
	 * @return The user ID
	 */
	public int getUserId() {
		return userId;
	}

	/**
	 * Sets the ID of the user who joined the waitlist.
	 *
	 * @param userId The user ID to set
	 */
	public void setUserId(int userId) {
		this.userId = userId;
	}

	/**
	 * Gets the timestamp when the entry was created.
	 *
	 * @return The timestamp string
	 */
	public String getTimestamp() {
		return timestamp;
	}

	/**
	 * Sets the timestamp when the entry was created.
	 *
	 * @param timestamp The timestamp to set
	 */
	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	/**
	 * Gets the number of guests in the waiting party.
	 *
	 * @return The number of guests
	 */
	public int getNumberOfGuests() {
		return numberOfGuests;
	}

	/**
	 * Sets the number of guests in the waiting party.
	 *
	 * @param numberOfGuests The number of guests to set
	 */
	public void setNumberOfGuests(int numberOfGuests) {
		this.numberOfGuests = numberOfGuests;
	}

	/**
	 * Gets the confirmation code for this waitlist entry.
	 *
	 * @return The confirmation code
	 */
	public String getConfirmationCode() {
		return confirmationCode;
	}

	/**
	 * Sets the confirmation code for this waitlist entry.
	 *
	 * @param confirmationCode The confirmation code to set
	 */
	public void setConfirmationCode(String confirmationCode) {
		this.confirmationCode = confirmationCode;
	}

	/**
	 * Gets the current status of the waitlist entry.
	 *
	 * @return The status string
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * Sets the current status of the waitlist entry.
	 *
	 * @param status The status to set (should be one of: "WAITING", "NOTIFIED",
	 *               "SEATED", "CANCELLED", "NO_SHOW")
	 */
	public void setStatus(String status) {
		this.status = status;
	}

	/**
	 * Gets the assigned table number.
	 *
	 * @return The table number, or null if not yet assigned
	 */
	public Integer getAssignedTableNumber() {
		return assignedTableNumber;
	}

	/**
	 * Sets the assigned table number. Should be set when customer is notified that
	 * table is ready.
	 *
	 * @param assignedTableNumber The table number to assign
	 */
	public void setAssignedTableNumber(Integer assignedTableNumber) {
		this.assignedTableNumber = assignedTableNumber;
	}

	// Status Check Methods

	/**
	 * Checks if the entry is in waiting status.
	 *
	 * @return true if status is "WAITING", false otherwise
	 */
	public boolean isWaiting() {
		return "WAITING".equals(status);
	}

	/**
	 * Checks if the customer has been notified.
	 *
	 * @return true if status is "NOTIFIED", false otherwise
	 */
	public boolean isNotified() {
		return "NOTIFIED".equals(status);
	}

	/**
	 * Checks if the customer has been seated.
	 *
	 * @return true if status is "SEATED", false otherwise
	 */
	public boolean isSeated() {
		return "SEATED".equals(status);
	}

	/**
	 * Checks if the entry was cancelled.
	 *
	 * @return true if status is "CANCELLED", false otherwise
	 */
	public boolean isCancelled() {
		return "CANCELLED".equals(status);
	}

	/**
	 * Checks if the customer was a no-show.
	 *
	 * @return true if status is "NO_SHOW", false otherwise
	 */
	public boolean isNoShow() {
		return "NO_SHOW".equals(status);
	}

	/**
	 * Checks if the entry is still active (waiting or notified). Active entries are
	 * those that haven't been completed, cancelled, or marked as no-show.
	 *
	 * @return true if status is "WAITING" or "NOTIFIED", false otherwise
	 */
	public boolean isActive() {
		return isWaiting() || isNotified();
	}

	/**
	 * Checks if the entry has been completed (seated, cancelled, or no-show).
	 *
	 * @return true if entry is no longer active, false otherwise
	 */
	public boolean isCompleted() {
		return isSeated() || isCancelled() || isNoShow();
	}

	// Action Methods

	/**
	 * Marks the entry as notified and assigns a table. Called when a suitable table
	 * becomes available.
	 *
	 * @param tableNumber The table number to assign
	 */
	public void notifyCustomer(int tableNumber) {
		this.status = "NOTIFIED";
		this.assignedTableNumber = tableNumber;
	}

	/**
	 * Marks the entry as seated. Called when customer arrives and checks in with
	 * confirmation code.
	 */
	public void seat() {
		this.status = "SEATED";
	}

	/**
	 * Cancels the waitlist entry. Called when customer chooses to leave the
	 * waitlist.
	 */
	public void cancel() {
		this.status = "CANCELLED";
	}

	/**
	 * Marks the entry as no-show. Called when customer doesn't arrive within 15
	 * minutes of notification.
	 */
	public void markNoShow() {
		this.status = "NO_SHOW";
	}

	/**
	 * Returns a string representation of the waitlist entry. 
	 *
	 * @return A string containing entry details
	 */
	@Override
	public String toString() {
		return "WaitlistEntry{" + "entryId=" + entryId + ", userId=" + userId + ", timestamp='" + timestamp + '\''
				+ ", numberOfGuests=" + numberOfGuests + ", confirmationCode='" + confirmationCode + '\'' + ", status='"
				+ status + '\'' + ", assignedTableNumber=" + assignedTableNumber + '}';
	}

	/**
	 * Returns a formatted string suitable for display in user interfaces. 
	 * 
	 * @return  string representation
	 */
	public String getDisplayString() {
		String timeOnly = timestamp.split(" ")[1].substring(0, 5); // Extract HH:MM
		return "Entry #" + entryId + " - Party of " + numberOfGuests + " - " + status + " since " + timeOnly;
	}

}
