package data_access;
import connection.MySQLConnectionPool;
import connection.PooledConnection;
import common.Message;
import entities.OpeningHours;
import entities.SpecialHours;
import services.AvailabilityService;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Repository for managing restaurant opening hours. Handles regular weekly
 * hours and special date hours.
 */
public class OpeningHoursRepository {

	/**
	 * Gets all regular weekly opening hours.
	 *
	 * @param request Message (empty data)
	 * @return Message with List of OpeningHours objects
	 */
	public Message getOpeningHours(Message request) {
		MySQLConnectionPool pool = MySQLConnectionPool.getInstance();
		PooledConnection pConn = null;

		try {
			pConn = pool.getConnection();
			if (pConn == null) {
				return Message.fail("GET_OPENING_HOURS", "Database connection failed");
			}

			Connection conn = pConn.getConnection();

			String sql = "SELECT * FROM opening_hours ORDER BY "
					+ "FIELD(weekday, 'SUNDAY', 'MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY')";

			PreparedStatement ps = conn.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();

			List<OpeningHours> hoursList = new ArrayList<>();
			while (rs.next()) {
				hoursList.add(extractOpeningHoursFromResultSet(rs));
			}

			rs.close();
			ps.close();

			return Message.ok("GET_OPENING_HOURS", hoursList);

		} catch (SQLException e) {
			e.printStackTrace();
			return Message.fail("GET_OPENING_HOURS", "Database error: " + e.getMessage());
		} finally {
			pool.releaseConnection(pConn);
		}
	}

	/**
	 * Updates opening hours for a specific weekday.
	 *
	 * @param request Message containing OpeningHours object
	 * @return Message with success or error
	 */
	public Message updateOpeningHours(Message request) {
		MySQLConnectionPool pool = MySQLConnectionPool.getInstance();
		PooledConnection pConn = null;

		try {
			OpeningHours hours = (OpeningHours) request.getData();

			pConn = pool.getConnection();
			if (pConn == null) {
				return Message.fail("UPDATE_OPENING_HOURS", "Database connection failed");
			}

			Connection conn = pConn.getConnection();

			String sql = "UPDATE opening_hours SET opening_time = ?, closing_time = ? WHERE weekday = ?";

			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setTime(1, Time.valueOf(hours.getOpeningTime()));
			ps.setTime(2, Time.valueOf(hours.getClosingTime()));
			ps.setString(3, hours.getWeekday().name());

			int rowsAffected = ps.executeUpdate();
			ps.close();

			if (rowsAffected > 0) {
				// Check and cancel affected reservations
				AvailabilityService.handleOpeningHoursChange(
					hours.getWeekday(), 
					hours.getOpeningTime(), 
					hours.getClosingTime()
				);
				return Message.ok("UPDATE_OPENING_HOURS", "Opening hours updated successfully");
			} else {
				return Message.fail("UPDATE_OPENING_HOURS", "Weekday not found");
			}

		} catch (SQLException e) {
			e.printStackTrace();
			return Message.fail("UPDATE_OPENING_HOURS", "Database error: " + e.getMessage());
		} finally {
			pool.releaseConnection(pConn);
		}
	}

	/**
	 * Gets all special hours (holidays, events, etc.).
	 *
	 * @param request Message (empty data)
	 * @return Message with List of SpecialHours objects
	 */
	public Message getSpecialHours(Message request) {
		MySQLConnectionPool pool = MySQLConnectionPool.getInstance();
		PooledConnection pConn = null;

		try {
			pConn = pool.getConnection();
			if (pConn == null) {
				return Message.fail("GET_SPECIAL_HOURS", "Database connection failed");
			}

			Connection conn = pConn.getConnection();

			String sql = "SELECT * FROM special_hours ORDER BY special_date";

			PreparedStatement ps = conn.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();

			List<SpecialHours> specialHoursList = new ArrayList<>();
			while (rs.next()) {
				specialHoursList.add(extractSpecialHoursFromResultSet(rs));
			}

			rs.close();
			ps.close();

			return Message.ok("GET_SPECIAL_HOURS", specialHoursList);

		} catch (SQLException e) {
			e.printStackTrace();
			return Message.fail("GET_SPECIAL_HOURS", "Database error: " + e.getMessage());
		} finally {
			pool.releaseConnection(pConn);
		}
	}

	/**
	 * Adds special hours for a specific date.
	 *
	 * @param request Message containing SpecialHours object
	 * @return Message with created SpecialHours object if successful
	 */
	public Message addSpecialHours(Message request) {
		MySQLConnectionPool pool = MySQLConnectionPool.getInstance();
		PooledConnection pConn = null;

		try {
			SpecialHours specialHours = (SpecialHours) request.getData();

			pConn = pool.getConnection();
			if (pConn == null) {
				return Message.fail("ADD_SPECIAL_HOURS", "Database connection failed");
			}

			Connection conn = pConn.getConnection();

// Check if special date already exists
			if (specialDateExists(conn, specialHours.getSpecialDate())) {
				return Message.fail("ADD_SPECIAL_HOURS", "Special hours for this date already exist");
			}

			String sql = "INSERT INTO special_hours (special_date, opening_time, closing_time, closed_flag) "
					+ "VALUES (?, ?, ?, ?)";

			PreparedStatement ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
			ps.setDate(1, Date.valueOf(specialHours.getSpecialDate()));

			if (specialHours.getClosedFlag()) {
				ps.setNull(2, java.sql.Types.TIME);
				ps.setNull(3, java.sql.Types.TIME);
			} else {
				ps.setTime(2, Time.valueOf(specialHours.getOpeningTime()));
				ps.setTime(3, Time.valueOf(specialHours.getClosingTime()));
			}

			ps.setBoolean(4, specialHours.getClosedFlag());
			ps.executeUpdate();

			ResultSet rs = ps.getGeneratedKeys();
			int specialId = 0;
			if (rs.next()) {
				specialId = rs.getInt(1);
			}
			rs.close();
			ps.close();

			specialHours.setSpecialId(specialId);

			// Check and cancel affected reservations
			AvailabilityService.handleSpecialHoursChange(
				specialHours.getSpecialDate(),
				specialHours.getOpeningTime(),
				specialHours.getClosingTime(),
				specialHours.getClosedFlag()
			);

			return Message.ok("ADD_SPECIAL_HOURS", specialHours);

		} catch (SQLException e) {
			e.printStackTrace();
			return Message.fail("ADD_SPECIAL_HOURS", "Database error: " + e.getMessage());
		} finally {
			pool.releaseConnection(pConn);
		}
	}

	/**
	 * Deletes special hours for a specific date.
	 *
	 * @param request Message containing "specialDate" (LocalDate as String)
	 * @return Message with success or error
	 */
	public Message deleteSpecialHours(Message request) {
		MySQLConnectionPool pool = MySQLConnectionPool.getInstance();
		PooledConnection pConn = null;

		try {
			@SuppressWarnings("unchecked")
			Map<String, Object> data = (Map<String, Object>) request.getData();
			LocalDate specialDate = LocalDate.parse((String) data.get("specialDate"));

			pConn = pool.getConnection();
			if (pConn == null) {
				return Message.fail("DELETE_SPECIAL_HOURS", "Database connection failed");
			}

			Connection conn = pConn.getConnection();

			String sql = "DELETE FROM special_hours WHERE special_date = ?";

			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setDate(1, Date.valueOf(specialDate));

			int rowsAffected = ps.executeUpdate();
			ps.close();

			if (rowsAffected > 0) {
				return Message.ok("DELETE_SPECIAL_HOURS", "Special hours deleted successfully");
			} else {
				return Message.fail("DELETE_SPECIAL_HOURS", "Special hours not found for this date");
			}

		} catch (SQLException e) {
			e.printStackTrace();
			return Message.fail("DELETE_SPECIAL_HOURS", "Database error: " + e.getMessage());
		} finally {
			pool.releaseConnection(pConn);
		}
	}

// Helper methods 

	/**
	 * Checks if special hours already exist for a specific date.
	 */
	private boolean specialDateExists(Connection conn, LocalDate specialDate) throws SQLException {
		String sql = "SELECT COUNT(*) FROM special_hours WHERE special_date = ?";
		PreparedStatement ps = conn.prepareStatement(sql);
		ps.setDate(1, Date.valueOf(specialDate));
		ResultSet rs = ps.executeQuery();

		boolean exists = false;
		if (rs.next()) {
			exists = rs.getInt(1) > 0;
		}

		rs.close();
		ps.close();
		return exists;
	}

	/**
	 * Extracts an OpeningHours object from ResultSet.
	 */
	private OpeningHours extractOpeningHoursFromResultSet(ResultSet rs) throws SQLException {
		OpeningHours hours = new OpeningHours();
		hours.setId(rs.getInt("id"));
		hours.setWeekday(OpeningHours.Weekday.valueOf(rs.getString("weekday")));
		hours.setOpeningTime(rs.getTime("opening_time").toLocalTime());
		hours.setClosingTime(rs.getTime("closing_time").toLocalTime());
		return hours;
	}

	/**
	 * Extracts a SpecialHours object from ResultSet.
	 */
	private SpecialHours extractSpecialHoursFromResultSet(ResultSet rs) throws SQLException {
		SpecialHours specialHours = new SpecialHours();
		specialHours.setSpecialId(rs.getInt("special_id"));
		specialHours.setSpecialDate(rs.getDate("special_date").toLocalDate());
		specialHours.setClosedFlag(rs.getBoolean("closed_flag"));

		Time openingTime = rs.getTime("opening_time");
		if (openingTime != null) {
			specialHours.setOpeningTime(openingTime.toLocalTime());
		}

		Time closingTime = rs.getTime("closing_time");
		if (closingTime != null) {
			specialHours.setClosingTime(closingTime.toLocalTime());
		}

		return specialHours;
	}
}