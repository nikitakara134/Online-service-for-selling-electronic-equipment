package com.diploma.backend;

import com.diploma.backend.services.MailSenderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MailSenderServiceTest {

    @Mock private JavaMailSender javaMailSender;
    @InjectMocks private MailSenderService mailSenderService;

    @Test
    void send_StandardEmail_Success() {
        mailSenderService.send("client@test.com", "Subject", "Hello World");
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(javaMailSender, times(1)).send(captor.capture());

        // Перевіряємо, що масив отримувачів не дорівнює null (щоб IDEA не сварилася)
        assertNotNull(captor.getValue().getTo());
        assertEquals("client@test.com", captor.getValue().getTo()[0]);
        assertEquals("Subject", captor.getValue().getSubject());
    }

    @Test
    void send_EmptyMessage_Success() {
        mailSenderService.send("client@test.com", "Subject", "");
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(javaMailSender, times(1)).send(captor.capture());

        assertEquals("", captor.getValue().getText());
    }

    @Test
    void send_MultipleCalls_SendsMultipleEmails() {
        mailSenderService.send("client1@test.com", "Subj1", "Msg1");
        mailSenderService.send("client2@test.com", "Subj2", "Msg2");

        verify(javaMailSender, times(2)).send(any(SimpleMailMessage.class));
    }
}