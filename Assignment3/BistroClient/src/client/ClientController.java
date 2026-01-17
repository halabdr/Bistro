package client;

import client.Commands;
import common.Message;
import entities.OpeningHours;
import entities.SpecialHours;
import entities.Table;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.time.YearMonth;
import entities.MonthlyReport;

/**
 * Client-side facade responsible for sending requests to the server, and
 * dispatching server responses to the currently active UI.
 */
public class ClientController {

	private final BistroClient client;
	private MessageListener listener;

	/**
	 * Creates a new facade wrapper around the OCSF client.
	 *
	 * @param client OCSF client instance
	 */
	public ClientController(BistroClient client) {
		this.client = client;
		this.client.setController(this);
	}

	/**
	 * Sets the active UI listener that will receive server responses.
	 *
	 * @param l message listener
	 */
	public void setListener(MessageListener l) {
		this.listener = l;
	}

	/**
	 * @return true if the OCSF client is currently connected to the server.
	 */
	public boolean isConnected() {
		return client.isConnected();
	}

	/**
	 * Opens an OCSF connection to the server.
	 *
	 * @throws IOException if connection fails
	 */
	public void connect() throws IOException {
		client.openConnection();
	}

	/**
	 * Closes the OCSF connection to the server.
	 *
	 * @throws IOException if closing fails
	 */
	public void disconnect() throws IOException {
		client.closeConnection();
	}

	/**
	 * Called by BistroClient when a Message arrives from the server.
	 *
	 * @param msg server message
	 */
	void deliver(Message msg) {
		if (listener != null && msg != null) {
			listener.onMessage(msg);
		}
	}

	// User

	/**
	 * Sends a LOGIN request to the server.
	 *
	 * @param email    user email
	 * @param password user password
	 * @throws IOException if sending fails
	 */
	public void login(String email, String password) throws IOException {
		Map<String, Object> data = new HashMap<>();
		data.put("email", email);
		data.put("password", password);
		client.sendToServer(new Message(Commands.LOGIN, data));
	}

	/**
	 * Sends a LOGIN_BY_SUBSCRIBER_NUMBER request to the server.
	 *
	 * @param subscriberNumber subscriber number
	 * @throws IOException if sending fails
	 */
	public void loginBySubscriberNumber(String subscriberNumber) throws IOException {
		client.sendToServer(new Message(Commands.LOGIN_BY_SUBSCRIBER_NUMBER, subscriberNumber));
	}

	/**
	 * Sends a REGISTER_SUBSCRIBER request to the server. Only staff/representatives
	 * can register new subscribers.
	 *
	 * @param name             subscriber name
	 * @param email            subscriber email
	 * @param phone            subscriber phone
	 * @param password         subscriber password
	 * @param subscriberNumber unique subscriber number
	 * @param membershipCard   QR code for membership card
	 * @throws IOException if sending fails
	 */
	public void registerSubscriber(String name, String email, String phone, String password, String subscriberNumber,
			String membershipCard) throws IOException {
		Map<String, Object> data = new HashMap<>();
		data.put("name", name);
		data.put("email", email);
		data.put("phone", phone);
		data.put("password", password);
		data.put("subscriberNumber", subscriberNumber);
		data.put("membershipCard", membershipCard);
		client.sendToServer(new Message(Commands.REGISTER_SUBSCRIBER, data));
	}

	/**
	 * Requests all subscribers (for staff view).
	 *
	 * @throws IOException if sending fails
	 */
	public void getAllSubscribers() throws IOException {
		client.sendToServer(new Message(Commands.GET_ALL_SUBSCRIBERS, null));
	}

	/**
	 * Requests a specific subscriber by their subscriber number.
	 *
	 * @param subscriberNumber the subscriber number to search for
	 * @throws IOException if sending fails
	 */
	public void getSubscriberByNumber(String subscriberNumber) throws IOException {
		client.sendToServer(new Message(Commands.GET_SUBSCRIBER_BY_NUMBER, subscriberNumber));
	}

	// Reservation (customer + staff)

	/**
	 * Requests available time slots for a specific date and guest count.
	 *
	 * @param date   booking date
	 * @param guests number of guests
	 * @throws IOException if sending fails
	 */
	public void getAvailableSlots(LocalDate date, int guests) throws IOException {
		Map<String, Object> data = new HashMap<>();
		data.put("date", date.toString());
		data.put("guestCount", guests);
		client.sendToServer(new Message(Commands.GET_AVAILABLE_SLOTS, data));
	}

	/**
	 * Requests alternative time slots when the requested date has no availability.
	 *
	 * @param date   originally requested date
	 * @param guests number of guests
	 * @throws IOException if sending fails
	 */
	public void getAlternativeSlots(LocalDate date, int guests) throws IOException {
		Map<String, Object> data = new HashMap<>();
		data.put("date", date.toString());
		data.put("guestCount", guests);
		client.sendToServer(new Message(Commands.GET_ALTERNATIVE_SLOTS, data));
	}

	/**
	 * Sends a CREATE_RESERVATION request.
	 *
	 * @param date             booking date
	 * @param time             booking time
	 * @param guestCount       number of guests
	 * @param subscriberNumber subscriber id/number
	 * @throws IOException if sending fails
	 */
	public void createReservation(LocalDate date, LocalTime time, int guestCount, String subscriberNumber)
			throws IOException {
		Map<String, Object> data = new HashMap<>();
		data.put("bookingDate", date.toString());
		data.put("bookingTime", time.toString());
		data.put("guestCount", guestCount);
		data.put("subscriberNumber", subscriberNumber);
		client.sendToServer(new Message(Commands.CREATE_RESERVATION, data));
	}

	public void createReservation(LocalDate date, LocalTime time, int guestCount, String subscriberNumber,
	        String guestPhone, String guestEmail) throws IOException {

	    Map<String, Object> data = new HashMap<>();
	    data.put("bookingDate", date.toString());     
	    data.put("bookingTime", time.toString());     
	    data.put("guestCount", guestCount);           
	    data.put("subscriberNumber", subscriberNumber);
	    data.put("guestPhone", guestPhone);
	    data.put("guestEmail", guestEmail);

	    client.sendToServer(new Message(Commands.CREATE_RESERVATION, data));
	}


	/**
	 * Sends a CANCEL_RESERVATION request.
	 *
	 * @param confirmationCode reservation confirmation code
	 * @throws IOException if sending fails
	 */
	public void cancelReservation(String confirmationCode) throws IOException {
		Map<String, Object> data = new HashMap<>();
		data.put("confirmationCode", confirmationCode);
		client.sendToServer(new Message(Commands.CANCEL_RESERVATION, data));
	}

	/**
	 * Staff-only: requests all reservations.
	 *
	 * @throws IOException if sending fails
	 */
	public void getAllReservations() throws IOException {
		client.sendToServer(new Message(Commands.GET_RESERVATIONS, null));
	}

	/**
	 * Requests reservations for a specific subscriber.
	 *
	 * @param subscriberNumber subscriber id/number
	 * @throws IOException if sending fails
	 */
	public void getUserReservations(String subscriberNumber) throws IOException {
		Map<String, Object> data = new HashMap<>();
		data.put("subscriberNumber", subscriberNumber);
		client.sendToServer(new Message(Commands.GET_USER_RESERVATIONS, data));
	}

	/**
	 * Requests the list of current diners (occupied tables with reservation info).
	 *
	 * @throws IOException if sending fails
	 */
	public void getCurrentDiners() throws IOException {
		client.sendToServer(new Message(Commands.GET_CURRENT_DINERS, null));
	}

	/**
	 * Sends a GET_BILL request by confirmation code.
	 *
	 * @param confirmationCode the confirmation code
	 * @throws IOException if sending fails
	 */
	public void getBillByCode(String confirmationCode) throws IOException {
		Map<String, Object> data = new HashMap<>();
		data.put("confirmationCode", confirmationCode);
		client.sendToServer(new Message(Commands.GET_BILL, data));
	}

	/**
	 * Sends a PAY_BILL request by confirmation code.
	 *
	 * @param confirmationCode the confirmation code
	 * @throws IOException if sending fails
	 */
	public void payBillByCode(String confirmationCode) throws IOException {
		Map<String, Object> data = new HashMap<>();
		data.put("confirmationCode", confirmationCode);
		client.sendToServer(new Message(Commands.PAY_BILL, data));
	}

	// Waitlist

	/**
	 * Requests the current waitlist.
	 *
	 * @throws IOException if sending fails
	 */
	public void getWaitlist() throws IOException {
		client.sendToServer(new Message(Commands.GET_WAITLIST, null));
	}

	/**
	 * Sends a JOIN_WAITLIST request for a subscriber.
	 *
	 * @param numberOfDiners   number of diners
	 * @param subscriberNumber subscriber number
	 * @throws IOException if sending fails
	 */
	public void joinWaitlist(int numberOfDiners, String subscriberNumber) throws IOException {
		Map<String, Object> data = new HashMap<>();
		data.put("numberOfDiners", numberOfDiners);
		data.put("subscriberNumber", subscriberNumber);
		client.sendToServer(new Message(Commands.JOIN_WAITLIST, data));
	}

	/**
	 * Sends a JOIN_WAITLIST request with support for walk-in guests.
	 *
	 * @param numberOfDiners   number of diners
	 * @param subscriberNumber subscriber number (null if walk-in guest)
	 * @param guestPhone       guest phone (null if subscriber)
	 * @param guestEmail       guest email (null if subscriber)
	 * @throws IOException if sending fails
	 */
	public void joinWaitlist(int numberOfDiners, String subscriberNumber, String guestPhone, String guestEmail)
			throws IOException {
		Map<String, Object> data = new HashMap<>();
		data.put("numberOfDiners", numberOfDiners);
		data.put("subscriberNumber", subscriberNumber);
		data.put("guestPhone", guestPhone);
		data.put("guestEmail", guestEmail);
		client.sendToServer(new Message(Commands.JOIN_WAITLIST, data));
	}

	/**
	 * Sends a LEAVE_WAITLIST request.
	 *
	 * @param entryCode waitlist entry code
	 * @throws IOException if sending fails
	 */
	public void leaveWaitlist(String entryCode) throws IOException {
		Map<String, Object> data = new HashMap<>();
		data.put("entryCode", entryCode);
		client.sendToServer(new Message(Commands.LEAVE_WAITLIST, data));
	}

	/**
	 * Checks table availability at terminal for immediate seating or waitlist.
	 *
	 * @param numberOfDiners   number of diners
	 * @param subscriberNumber subscriber number (null if guest)
	 * @param guestPhone       guest phone (null if subscriber)
	 * @param guestEmail       guest email (null if subscriber)
	 * @throws IOException if sending fails
	 */
	public void checkAvailabilityTerminal(int numberOfDiners, String subscriberNumber, String guestPhone,
			String guestEmail) throws IOException {
		Map<String, Object> data = new HashMap<>();
		data.put("numberOfDiners", numberOfDiners);
		data.put("subscriberNumber", subscriberNumber);
		data.put("guestPhone", guestPhone);
		data.put("guestEmail", guestEmail);
		client.sendToServer(new Message(Commands.CHECK_AVAILABILITY_TERMINAL, data));
	}

	// Tables

	/**
	 * Requests all tables.
	 *
	 * @throws IOException if sending fails
	 */
	public void getTables() throws IOException {
		client.sendToServer(new Message(Commands.GET_TABLES, null));
	}

	/**
	 * Sends an ADD_TABLE request.
	 *
	 * @param tableNumber   table number
	 * @param seatCapacity  number of seats
	 * @param tableLocation location string
	 * @throws IOException if sending fails
	 */
	public void addTable(int tableNumber, int seatCapacity, String tableLocation) throws IOException {
		Map<String, Object> data = new HashMap<>();
		data.put("tableNumber", tableNumber);
		data.put("seatCapacity", seatCapacity);
		data.put("tableLocation", tableLocation);
		client.sendToServer(new Message(Commands.ADD_TABLE, data));
	}

	/**
	 * Sends an UPDATE_TABLE request.
	 *
	 * @param table updated table object
	 * @throws IOException if sending fails
	 */
	public void updateTable(Table table) throws IOException {
		client.sendToServer(new Message(Commands.UPDATE_TABLE, table));
	}

	/**
	 * Sends a DELETE_TABLE request.
	 *
	 * @param tableNumber table number
	 * @throws IOException if sending fails
	 */
	public void deleteTable(int tableNumber) throws IOException {
		Map<String, Object> data = new HashMap<>();
		data.put("tableNumber", tableNumber);
		client.sendToServer(new Message(Commands.DELETE_TABLE, data));
	}

	// Opening Hours

	/**
	 * Requests weekly opening hours list.
	 *
	 * @throws IOException if sending fails
	 */
	public void getOpeningHours() throws IOException {
		client.sendToServer(new Message(Commands.GET_OPENING_HOURS, null));
	}

	/**
	 * Sends an UPDATE_OPENING_HOURS request.
	 *
	 * @param hours updated opening hours row
	 * @throws IOException if sending fails
	 */
	public void updateOpeningHours(OpeningHours hours) throws IOException {
		client.sendToServer(new Message(Commands.UPDATE_OPENING_HOURS, hours));
	}

	// Special Hours

	/**
	 * Requests special hours list.
	 *
	 * @throws IOException if sending fails
	 */
	public void getSpecialHours() throws IOException {
		client.sendToServer(new Message(Commands.GET_SPECIAL_HOURS, null));
	}

	/**
	 * Sends a LOST_CODE request to retrieve reservation confirmation code.
	 *
	 * @param identifier email or phone entered by the user
	 * @throws IOException if sending fails
	 */
	public void lostCode(String identifier) throws IOException {
		Map<String, Object> data = new HashMap<>();
		data.put("identifier", identifier);
		client.sendToServer(new Message(Commands.LOST_CODE, data));
	}

	/**
	 * Sends a LOST_CODE_WAITLIST request to retrieve waitlist entry code.
	 *
	 * @param identifier email or phone entered by the user
	 * @throws IOException if sending fails
	 */
	public void lostCodeWaitlist(String identifier) throws IOException {
		Map<String, Object> data = new HashMap<>();
		data.put("identifier", identifier);
		client.sendToServer(new Message(Commands.LOST_CODE_WAITLIST, data));
	}

	/**
	 * Sends an ADD_SPECIAL_HOURS request.
	 *
	 * @param special special hours row to add
	 * @throws IOException if sending fails
	 */
	public void addSpecialHours(SpecialHours special) throws IOException {
		client.sendToServer(new Message(Commands.ADD_SPECIAL_HOURS, special));
	}

	/**
	 * Sends a PAY_BILL request to the server.
	 *
	 * @param billNumber bill identifier
	 * @throws IOException if sending fails
	 */
	public void payBill(int billNumber) throws IOException {
		Map<String, Object> data = new HashMap<>();
		data.put("billNumber", billNumber);
		client.sendToServer(new Message(Commands.PAY_BILL, data));
	}

	/**
	 * Sends a SEAT_BY_CODE request to seat a customer by confirmation code.
	 *
	 * @param confirmationCode reservation confirmation code
	 * @throws IOException if sending fails
	 */
	public void seatByCode(String confirmationCode) throws IOException {
		Map<String, Object> data = new HashMap<>();
		data.put("confirmationCode", confirmationCode);
		client.sendToServer(new Message(Commands.SEAT_BY_CODE, data));
	}
	
	/**
	 * Gets reservations for a subscriber by their membership card code.
	 *
	 * @param membershipCard the membership card code
	 * @throws IOException if sending fails
	 */
	public void getReservationsByMembershipCard(String membershipCard) throws IOException {
	    Map<String, Object> data = new HashMap<>();
	    data.put("membershipCard", membershipCard);
	    client.sendToServer(new Message(Commands.GET_RESERVATIONS_BY_CARD, data));
	}

	/**
	 * Sends a DELETE_SPECIAL_HOURS request to the server.
	 *
	 * @param specialDate date to delete special hours for
	 * @throws IOException if sending fails
	 */
	public void deleteSpecialHours(LocalDate specialDate) throws IOException {
		Map<String, Object> data = new HashMap<>();
		data.put("specialDate", specialDate.toString());
		client.sendToServer(new Message(Commands.DELETE_SPECIAL_HOURS, data));
	}

	// Reports (Manager/Staff)

	/**
	 * Requests notification log report for a specific month.
	 *
	 * @param year  report year
	 * @param month report month (1-12)
	 * @throws IOException if sending fails
	 */
	public void getNotificationLogReport(int year, int month) throws IOException {
		MonthlyReport report = MonthlyReport.createForMonth(year, month);
		client.sendToServer(new Message(Commands.GET_NOTIFICATION_LOG, report));
	}

	/**
	 * Requests time report for a specific month.
	 *
	 * @param year  report year
	 * @param month report month (1-12)
	 * @throws IOException if sending fails
	 */
	public void getTimeReport(int year, int month) throws IOException {
		MonthlyReport report = MonthlyReport.createForMonth(year, month);
		client.sendToServer(new Message(Commands.GET_TIME_REPORT, report));
	}

	/**
	 * Requests subscribers report for a specific month.
	 *
	 * @param year  report year
	 * @param month report month (1-12)
	 * @throws IOException if sending fails
	 */
	public void getSubscribersReport(int year, int month) throws IOException {
		MonthlyReport report = MonthlyReport.createForMonth(year, month);
		client.sendToServer(new Message(Commands.GET_SUBSCRIBERS_REPORT, report));
	}

	/**
	 * Requests a list of available months that have reports.
	 *
	 * @throws IOException if sending fails
	 */
	public void getMonthlyReportsList() throws IOException {
		client.sendToServer(new Message(Commands.GET_MONTHLY_REPORTS_LIST, null));
	}

	/**
	 * Triggers report generation for a specific month.
	 *
	 * @param year  report year
	 * @param month report month (1-12)
	 * @throws IOException if sending fails
	 */
	public void generateReports(int year, int month) throws IOException {
		MonthlyReport report = MonthlyReport.createForMonth(year, month);
		client.sendToServer(new Message(Commands.GENERATE_REPORTS, report));
	}

	/**
	 * Sends a generic message to the server.
	 *
	 * @param message the message to send
	 * @throws IOException if sending fails
	 */
	public void sendToServer(Message message) throws IOException {
		client.sendToServer(message);
	}
}