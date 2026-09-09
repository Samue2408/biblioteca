package com.ezertech.backend.service;

import com.ezertech.backend.config.MailProperties;
import com.ezertech.backend.entity.AppUser;
import com.ezertech.backend.entity.Loan;
import com.ezertech.backend.entity.Reservation;
import com.ezertech.backend.exception.ResourceNotFoundException;
import com.ezertech.backend.repository.AppUserRepository;
import com.ezertech.backend.repository.LoanRepository;
import com.ezertech.backend.repository.ReservationRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
public class NotificationService {
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final LoanRepository loanRepository;
    private final AppUserRepository appUserRepository;
    private final ReservationRepository reservationRepository;
    private final MailProperties mailProperties;

    public NotificationService(JavaMailSender mailSender,
                               TemplateEngine templateEngine,
                               LoanRepository loanRepository,
                               AppUserRepository appUserRepository,
                               ReservationRepository reservationRepository,
                               MailProperties mailProperties) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
        this.loanRepository = loanRepository;
        this.appUserRepository = appUserRepository;
        this.reservationRepository = reservationRepository;
        this.mailProperties = mailProperties;
    }

    public void sendLoanConfirmation(Long loanId) {
        Loan loan = loanRepository.findWithBookAndBorrowerById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Préstamo no encontrado"));

        Context context = new Context();
        context.setVariable("borrowerName", loan.getBorrower().getName());
        context.setVariable("bookTitle", loan.getBook().getTitle());
        context.setVariable("loanDate", loan.getLoanDate());
        context.setVariable("dueDate", loan.getDueDate());

        send(loan.getBorrower().getEmail(), "Confirmación de préstamo", "email/loan-confirmation", context);
    }

    public void sendDueSoonReminder(Loan loan) {
        Context context = new Context();
        context.setVariable("borrowerName", loan.getBorrower().getName());
        context.setVariable("bookTitle", loan.getBook().getTitle());
        context.setVariable("dueDate", loan.getDueDate());

        send(loan.getBorrower().getEmail(), "Tu préstamo está por vencer", "email/due-soon-reminder", context);
    }

    public void sendAccountBlocked(Long userId) {
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        Context context = new Context();
        context.setVariable("name", user.getName());
        context.setVariable("blockedUntil", user.getBlockedUntil());

        send(user.getEmail(), "Tu cuenta ha sido bloqueada temporalmente", "email/account-blocked", context);
    }

    public void sendBookAvailable(Long reservationId) {
        Reservation reservation = reservationRepository.findWithBookAndBorrowerById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada"));

        Context context = new Context();
        context.setVariable("name", reservation.getBorrower().getName());
        context.setVariable("bookTitle", reservation.getBook().getTitle());

        send(reservation.getBorrower().getEmail(), "El libro que esperabas ya está disponible",
                "email/book-available", context);
    }

    private void send(String to, String subject, String template, Context context) {
        try {
            String html = templateEngine.process(template, context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
            helper.setTo(to);
            helper.setFrom(mailProperties.from());
            helper.setSubject(subject);
            helper.setText(html, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("No se pudo enviar el correo a " + to, e);
        }
    }
}
