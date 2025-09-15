package com.flux.movieproject.service.product;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Async
@Slf4j
public class NotificationService {
    private final JavaMailSender mailSender;

    public NotificationService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }

    @Async
    public void sendEmailAsync(String to, String subject, String content) {
        try {
            // 呼叫原本的 sendEmail 方法
            sendEmail(to, subject, content);
            log.info("郵件已發送至: {}", to);
        } catch (Exception e) {
            // 記錄錯誤但不拋出例外，避免影響主流程
            log.error("郵件發送失敗 - 收件人: {}, 主旨: {}", to, subject, e);
            // 可以考慮將失敗的郵件存入資料庫，稍後重試
        }
    }
}
