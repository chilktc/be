package be.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class InvitedEmailService {

	private final SesClient sesClient;

	@Value("${app.invite.login-url}")
	private String loginUrl;

	@Value("${app.email.from}")
	private String fromEmail;

	public void sendInviteEmail(String email) {

		String subject = "조직원 초대 안내";

		String body = """
                안녕하세요.

                Bloom 서비스의 조직원으로 초대되었습니다.
                아래 링크에서 구글 로그인을 진행해주세요.

                %s
                """.formatted(loginUrl);

		SendEmailRequest request = SendEmailRequest.builder()
			.source(fromEmail)
			.destination(
				Destination.builder()
					.toAddresses(email)
					.build()
			)
			.message(
				Message.builder()
					.subject(
						Content.builder()
							.data(subject)
							.charset("UTF-8")
							.build()
					)
					.body(
						Body.builder()
							.text(
								Content.builder()
									.data(body)
									.charset("UTF-8")
									.build()
							)
							.build()
					)
					.build()
			)
			.build();

		sesClient.sendEmail(request);

		log.info("Invite email sent via SES. email={}", email);
		// TODO: 이메일 양식 수정 필요
	}
}