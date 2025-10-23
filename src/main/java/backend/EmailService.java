package backend;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Service for sending emails (confirmation, password reset, notifications).
 * Uses the existing ConnectToDatabase.sendEmail() method.
 */
@Service
public class EmailService {
    
    @Value("${app.base.url}")
    private String baseUrl;
    
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
        
        ConnectToDatabase.sendEmail(toEmail, subject, body);
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
            "%s\n\n" +
            "If you did not make this change, please contact support immediately.\n\n" +
            "Best regards,\n" +
            "Cinema E-Booking Team",
            firstName, changeDescription
        );
        
        sendEmail(toEmail, subject, body);
    }
    
    /**
     * Send a generic email using ConnectToDatabase.sendEmail().
     * @param to Recipient email address
     * @param subject Email subject
     * @param body Email body
     */
    private void sendEmail(String to, String subject, String body) {
        ConnectToDatabase.sendEmail(to, subject, body);
    }
}
