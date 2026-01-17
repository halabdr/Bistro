package services;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Mock notification service - simulates sending SMS and Email.
 * In production, this would integrate with real SMS/Email providers.
 */
public class NotificationService {

    private static final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Simulates sending an SMS message.
     * 
     * @param phoneNumber recipient phone number
     * @param message     message content
     */
    public static void sendSMS(String phoneNumber, String message) {
        String timestamp = LocalDateTime.now().format(fmt);
        System.out.println("[" + timestamp + "] [SMS] To: " + phoneNumber);
        System.out.println("    Message: " + message);
    }

    /**
     * Simulates sending an Email message.
     * 
     * @param emailAddress recipient email address
     * @param subject      email subject
     * @param message      email body
     */
    public static void sendEmail(String emailAddress, String subject, String message) {
        String timestamp = LocalDateTime.now().format(fmt);
        System.out.println("[" + timestamp + "] [EMAIL] To: " + emailAddress);
        System.out.println("    Subject: " + subject);
        System.out.println("    Body: " + message);
    }

    /**
     * Sends notification via both SMS and Email (if available).
     * 
     * @param phone   phone number (can be null)
     * @param email   email address (can be null)
     * @param subject email subject
     * @param message message content
     */
    public static void sendNotification(String phone, String email, String subject, String message) {
        if (phone != null && !phone.isBlank()) {
            sendSMS(phone, message);
        }
        if (email != null && !email.isBlank()) {
            sendEmail(email, subject, message);
        }
    }
}