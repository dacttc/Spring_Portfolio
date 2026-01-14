package com.example.portfolio.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    @Value("${app.mail.from}")
    private String from;

    @Value("${app.mail.fromName}")
    private String fromName;

    public void sendVerificationEmail(String to, String token) {
        String link = "http://localhost:8080/verify?token=" + token;

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper =
                    new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setFrom(String.format("%s <%s>", fromName, from));
            helper.setSubject("CXCVC 이메일 인증");

            String html = """
                <div style="font-family: Arial, sans-serif; max-width: 600px;">
                  <h2>이메일 인증</h2>
                  <p>아래 버튼을 클릭해 이메일 인증을 완료하세요.</p>
                  
                  <a href="%s"
                     style="
                       display: inline-block;
                       padding: 12px 24px;
                       background-color: #4F46E5;
                       color: white;
                       text-decoration: none;
                       border-radius: 6px;
                       font-weight: bold;
                     ">
                     이메일 인증하기
                  </a>

                  <p style="margin-top: 24px; font-size: 12px; color: #888;">
                    만약 버튼이 동작하지 않으면 아래 링크를 복사하세요.<br/>
                    %s
                  </p>
                </div>
            """.formatted(link, link);

            helper.setText(html, true);
            mailSender.send(message);

            log.info("✅ HTML 인증 메일 전송 완료: {}", to);
        } catch (Exception e) {
            log.error("❌ 메일 전송 실패", e);
        }
    }
}

