/*
package com.spoons.popparazzi.email.service;

import jakarta.mail.MessagingException;
import jakarta.mail.SendFailedException;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import static kr.co.hs.util.exception.type.ErrorCode.*;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender javaMailSender;

    public void sendMail(EmailMessage emailMessage) {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false,"UTF-8");

            // 이메일 수신자 설정
            helper.setTo(emailMessage.getTo());

            // 이메일 제목 설정
            helper.setSubject(emailMessage.getSubject());

            // 본문 내용 설정, false는 HTML 형식의 메세지를 사용하지 않음을 나타냄
            helper.setText(emailMessage.getMessage(), true);

            // 이메일 발신자 설정
            // @Value("${Spring.mail.username}")
            String from = "autoreply@bookswage.com";
            helper.setFrom(new InternetAddress(from, false));

            // 이메일 보내기
            javaMailSender.send(mimeMessage);
        }  catch (AddressException e) {
            // 잘못된 이메일 주소일 때
            throw new EmailException(EMAIL_VALIDATION_ERROR, e);

        } catch (SendFailedException e) {
            // 메일 발송 실패
            throw new EmailException(ERROR_SEND_EMAIL, e);

        } catch (MailAuthenticationException e) {
            System.out.println(e.getMessage());
            // 인증 오류 (예: SMTP 인증 실패)
            throw new EmailException(EMAIL_AUTHENTICATION_ERROR, e);

        } catch (MailSendException e) {
            // SMTP 서버 문제 또는 네트워크 오류
            throw new EmailException(EMAIL_CONNECTION_ERROR, e);

        } catch (MessagingException e) {
            // 기타 메시징 관련 오류
            throw new EmailException(EMAIL_TRANSMISSION_ERROR, e);
        }
    }

}
*/
