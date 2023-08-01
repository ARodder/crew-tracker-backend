package net.aroder.TripTracker.services;


import jakarta.mail.internet.MimeMessage;
import net.aroder.TripTracker.models.Email;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@Service
public class EmailSender {

    @Autowired
    private JavaMailSender javaMailSender;

    @Autowired
    private UserService userService;

    /**
     * Sends an email asynchronously from the voicepick sender
     *
     * @param email email object that will be sent to the recipient
     * @return Success / error message after trying to send a mail
     * @throws RuntimeException if email fails to send
     */
    @Async
    public Future<String> sendMail(Email email) {
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

            helper.setFrom("CrewTracker-Norge");
            helper.setTo(email.getRecipient());
            helper.setSubject(email.getEmailSubject().getText());
            helper.setText(email.getEmailBody(), true);

            javaMailSender.send(mimeMessage);
            return CompletableFuture.completedFuture("Mail Sent Successfully...");
        } catch (jakarta.mail.MessagingException e) {
            CompletableFuture<String> future = new CompletableFuture<>();
            future.completeExceptionally(new RuntimeException("Error while Sending Mail \n" + e.getMessage()));
            return future;
        }
    }

    /**
     * Gets the success / error message as a string from the future object
     *
     * @param futureResult future string from sendmail method
     * @return String from the future object
     */
    public ResponseEntity<String> getResultFromFuture(Future<String> futureResult) {
        String errorMessage;
        try {
            return ResponseEntity.ok(futureResult.get());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            errorMessage = "Error: The email sending operation was interrupted: " + e.getMessage();
        } catch (CancellationException e) {
            errorMessage = "Error: The email sending operation was cancelled: " + e.getMessage();
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException) {
                errorMessage = "Error: " + cause.getMessage();
            } else {
                errorMessage = "Error: An unknown error occurred while sending the email: " + e.getMessage();
            }
        }
        return new ResponseEntity<>(errorMessage, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
