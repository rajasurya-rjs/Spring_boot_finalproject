package com.taskmanagement.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    public void sendWelcomeEmail(String to, String fullName) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject("Welcome to Task Management System");
            message.setText(String.format(
                    "Hi %s,\n\n" +
                            "Welcome to Task Management System! Your account has been created successfully.\n\n" +
                            "You can now start managing your tasks and projects.\n\n" +
                            "Best regards,\n" +
                            "Task Management Team",
                    fullName));

            mailSender.send(message);
            log.info("Welcome email sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send welcome email to: {}", to, e);
        }
    }

    @Async
    public void sendTaskAssignmentEmail(String to, String assigneeName, String taskTitle, String projectName) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject("New Task Assigned: " + taskTitle);
            message.setText(String.format(
                    "Hi %s,\n\n" +
                            "A new task has been assigned to you:\n\n" +
                            "Task: %s\n" +
                            "Project: %s\n\n" +
                            "Please login to the Task Management System to view details.\n\n" +
                            "Best regards,\n" +
                            "Task Management Team",
                    assigneeName,
                    taskTitle,
                    projectName));

            mailSender.send(message);
            log.info("Task assignment email sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send task assignment email to: {}", to, e);
        }
    }
}
