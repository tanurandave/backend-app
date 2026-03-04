package com.training.backend_app.service;

import com.training.backend_app.dto.OtpRequest;
import com.training.backend_app.dto.OtpResponse;
import com.training.backend_app.dto.OtpVerifyRequest;
import com.training.backend_app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class OtpService {

    private static final Logger logger = LoggerFactory.getLogger(OtpService.class);

    private final JavaMailSender mailSender;
    private final UserRepository userRepository;

    @Value("${spring.mail.username:}")
    private String fromEmail;

    @Value("${otp.expiry.minutes:10}")
    private int otpExpiryMinutes;

    // In-memory OTP store: email -> OtpEntry
    private final Map<String, OtpEntry> otpStore = new ConcurrentHashMap<>();

    private static final class OtpEntry {
        final String otp;
        final LocalDateTime expiresAt;
        boolean verified;

        OtpEntry(String otp, int expiryMinutes) {
            this.otp = otp;
            this.expiresAt = LocalDateTime.now().plusMinutes(expiryMinutes);
            this.verified = false;
        }

        boolean isExpired() {
            return LocalDateTime.now().isAfter(expiresAt);
        }
    }

    /**
     * Generates a 6-digit OTP and attempts to email it.
     * If SMTP is not configured, falls back to console logging (dev mode).
     */
    public OtpResponse sendOtp(OtpRequest request) {
        String email = request.getEmail().trim().toLowerCase();

        // Verify the user exists
        userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("No account found with this email address"));

        // Generate 6-digit OTP
        String otp = generateOtp();

        // Store OTP (always, regardless of email success)
        otpStore.put(email, new OtpEntry(otp, otpExpiryMinutes));

        // Attempt to send email; fall back to console on failure
        boolean emailSent = trySendOtpEmail(email, otp);

        if (emailSent) {
            logger.info("OTP email sent successfully to {}", email);
            return OtpResponse.builder()
                    .success(true)
                    .message("OTP sent to " + email + ". Please check your inbox.")
                    .build();
        } else {
            // Dev/fallback mode: OTP is printed to the Spring Boot console
            logger.warn("================================================================");
            logger.warn("  EMAIL NOT CONFIGURED — DEV MODE FALLBACK");
            logger.warn("  OTP for {} : {}", email, otp);
            logger.warn("  (Copy this OTP from the terminal to verify)");
            logger.warn("================================================================");
            return OtpResponse.builder()
                    .success(true)
                    .message("OTP generated. (Email not configured — check the server console for the OTP)")
                    .build();
        }
    }

    /**
     * Verifies the OTP entered by the user.
     */
    public OtpResponse verifyOtp(OtpVerifyRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        String otpInput = request.getOtp().trim();

        OtpEntry entry = otpStore.get(email);

        if (entry == null) {
            throw new RuntimeException("No OTP found for this email. Please request a new one.");
        }

        if (entry.isExpired()) {
            otpStore.remove(email);
            throw new RuntimeException("OTP has expired. Please request a new one.");
        }

        if (!entry.otp.equals(otpInput)) {
            throw new RuntimeException("Invalid OTP. Please try again.");
        }

        // Mark as verified (allow password reset)
        entry.verified = true;

        return OtpResponse.builder()
                .success(true)
                .message("OTP verified successfully")
                .build();
    }

    /**
     * Checks whether OTP was verified for this email before allowing password
     * reset.
     */
    public boolean isOtpVerified(String email) {
        OtpEntry entry = otpStore.get(email.trim().toLowerCase());
        return entry != null && entry.verified && !entry.isExpired();
    }

    /**
     * Clears OTP entry after successful password reset.
     */
    public void clearOtp(String email) {
        otpStore.remove(email.trim().toLowerCase());
    }

    // ── Private helpers
    // ───────────────────────────────────────────────────────────

    private String generateOtp() {
        SecureRandom random = new SecureRandom();
        int code = 100000 + random.nextInt(900000); // always 6 digits
        return String.valueOf(code);
    }

    /**
     * Tries to send the OTP via email.
     * Returns true if sent successfully, false if SMTP is not configured or fails.
     */
    private boolean trySendOtpEmail(String toEmail, String otp) {
        // If credentials are clearly not configured, skip the attempt
        if (fromEmail == null || fromEmail.isBlank()
                || fromEmail.contains("your-email")
                || fromEmail.equals("your-email@gmail.com")) {
            logger.warn("Mail username is not configured. Skipping email send.");
            return false;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Your Password Reset OTP - Training Institute");
            message.setText(
                    "Hello,\n\n" +
                            "You requested a password reset for your Training Institute account.\n\n" +
                            "Your One-Time Password (OTP) is:\n\n" +
                            "    " + otp + "\n\n" +
                            "This OTP is valid for " + otpExpiryMinutes + " minutes.\n" +
                            "Do NOT share this OTP with anyone.\n\n" +
                            "If you did not request this, please ignore this email.\n\n" +
                            "Regards,\nTraining Institute Team");
            mailSender.send(message);
            return true;
        } catch (MailException ex) {
            logger.error("Failed to send OTP email to {}: {}", toEmail, ex.getMessage());
            return false;
        }
    }
}
