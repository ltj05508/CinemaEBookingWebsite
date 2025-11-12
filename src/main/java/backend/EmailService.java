package backend;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import java.util.Properties;

/**
 * Service for sending emails (confirmation, password reset, notifications).
 * Uses the existing ConnectToDatabase.sendEmail() method.
 */
@Service
public class EmailService {
    
    //@Value("${app.base.url}")
    private String baseUrl = "http://localhost:3000";
    
    /**
     * Send verification code to new user for email confirmation.
     * @param toEmail Recipient email address
     * @param firstName User's first name
     * @param verificationCode 6-digit verification code
     */
    public void sendVerificationCodeEmail(String toEmail, String firstName, String verificationCode) {
        String subject = "Verify Your Cinema E-Booking Account";
        String body = String.format(
            "Hello %s,\n\n" +
            "Thank you for registering with Cinema E-Booking!\n\n" +
            "Your verification code is: %s\n\n" +
            "Please enter this code on the verification page to activate your account.\n" +
            "This code will expire in 24 hours.\n\n" +
            "If you did not create this account, please ignore this email.\n\n" +
            "Best regards,\n" +
            "Cinema E-Booking Team",
            firstName, verificationCode
        );
        
        sendEmail(toEmail, subject, body);
    }
    
    /**
     * Send email confirmation link to new user.
     * @param toEmail Recipient email address
     * @param firstName User's first name
     * @param confirmationToken Confirmation token for the link
     */
    public void sendConfirmationEmail(String toEmail, String firstName, String confirmationToken) {
        String subject = "Confirm Your Cinema E-Booking Account";
        String confirmUrl = baseUrl + "/api/auth/confirm?token=" + confirmationToken;
        String body = String.format(
            "Hello %s,\n\n" +
            "Thank you for registering with Cinema E-Booking!\n\n" +
            "Please confirm your email address by clicking the link below:\n" +
            "%s\n\n" +
            "This link will expire in 24 hours.\n\n" +
            "If you did not create this account, please ignore this email.\n\n" +
            "Best regards,\n" +
            "Cinema E-Booking Team",
            firstName, confirmUrl
        );
        
        sendEmail(toEmail, subject, body);
    }
    
    /**
     * Send password reset link to user.
     * @param toEmail Recipient email address
     * @param firstName User's first name
     * @param resetToken Password reset token for the link
     */
    public void sendPasswordResetEmail(String toEmail, String firstName, String resetToken) {
        String subject = "Reset Your Cinema E-Booking Password";
        String resetUrl = baseUrl + "/reset-password?token=" + resetToken;
        String body = String.format(
            "Hello %s,\n\n" +
            "Click the link below to reset your password:\n" +
            "%s\n\n" +
            "This link will expire in 1 hour.\n\n" +
            "If you did not request a password reset, please ignore this email.\n\n" +
            "Best regards,\n" +
            "Cinema E-Booking Team",
            firstName, resetUrl
        );
        
        sendEmail(toEmail, subject, body);
    }
    
    /**
     * Send notification about profile changes.
     * @param toEmail Recipient email address
     * @param firstName User's first name
     * @param changeDescription Description of what was changed
     */
    public void sendProfileChangeNotification(String toEmail, String firstName, String changeDescription) {
        String subject = "Your Profile Has Been Updated";
        String body = String.format(
            "Hello %s,\n\n" +
            "Your profile has been updated:\n" +
            "%s\n" +
            "If you did not make this change, please contact support immediately.\n\n" +
            "Best regards,\n" +
            "Cinema E-Booking Team",
            firstName, changeDescription
        );
        
        sendEmail(toEmail, subject, body);
    }
    
    /**
     * Send promotional email to subscribed users.
     * Only call this for users who have opted in to receive promotional emails (marketingOptIn = true).
     * @param toEmail Recipient email address
     * @param firstName User's first name
     * @param subject Email subject
     * @param message Promotional message body
     */
    public void sendPromotionEmail(String toEmail, String firstName, String subject, String message) {
        String body = String.format(
            "Hello %s,\n\n" +
            "%s\n\n" +
            "Thank you for being a valued customer!\n\n" +
            "Best regards,\n" +
            "Cinema E-Booking Team\n\n" +
            "---\n" +
            "You received this email because you opted in to receive promotional offers. " +
            "You can manage your email preferences in your account settings.",
            firstName, message
        );
        
        sendEmail(toEmail, subject, body);
    }
    
    /**
     * Send a generic email using Gmail services.
     * @param to Recipient email address
     * @param subject Email subject
     * @param body Email body
     */
    private void sendEmail(String to, String subject, String body) {
        System.out.println("=== Attempting to send email ===");
        System.out.println("To: " + to);
        System.out.println("Subject: " + subject);
        
        try {
            Properties props = new Properties();
            props.setProperty("mail.smtp.auth", "true");
            props.setProperty("mail.smtp.ssl.protocols", "TLSv1.2");
            props.setProperty("mail.smtp.starttls.enable", "true");
            props.setProperty("mail.smtp.host", "smtp.gmail.com");
            props.setProperty("mail.smtp.port", "587"); // SMTP port for TLS
            props.setProperty("mail.debug", "true"); // Enable debug output

            Authenticator auth = new Authenticator() {
                public PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication("noreplycinemaebooking@gmail.com", "eptp qpwv yhtm rfgc");
                }
            };

            Session session = Session.getInstance(props, auth);
            session.setDebug(true); // Enable session debugging
            String fromAddress = "noreplycinemaebooking@gmail.com";

            jakarta.mail.Message msg = new jakarta.mail.internet.MimeMessage(session);
            msg.setFrom(new InternetAddress(fromAddress));
            msg.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
            msg.setSubject(subject);
            msg.setText(body);
            
            System.out.println("Connecting to SMTP server...");
            Transport.send(msg);
            System.out.println("✅ Email sent successfully!");

        } catch (MessagingException me) {
            System.err.println("❌ Error sending email: " + me.getMessage());
            me.printStackTrace();
            // Re-throw as runtime exception so caller knows it failed
            throw new RuntimeException("Failed to send email: " + me.getMessage(), me);
        }
    }
}
