package be.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.Body;
import software.amazon.awssdk.services.ses.model.Content;
import software.amazon.awssdk.services.ses.model.Destination;
import software.amazon.awssdk.services.ses.model.Message;
import software.amazon.awssdk.services.ses.model.SendEmailRequest;
import software.amazon.awssdk.services.ses.model.SendEmailResponse;

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
		String subject = "[Bloom] Bloom 서비스에 초대되셨습니다";

		String htmlBody = """
			<!DOCTYPE html>
			<html lang="ko">
			<head>
			  <meta charset="UTF-8" />
			  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
			  <title>Bloom 초대 메일</title>
			</head>
			<body style="margin:0; padding:0; background-color:#07090d; font-family:Arial, 'Apple SD Gothic Neo', 'Noto Sans KR', sans-serif;">
			  <table width="100%%" cellpadding="0" cellspacing="0" style="background-color:#07090d; padding:40px 16px;">
			    <tr>
			      <td align="center">
			        <table width="640" cellpadding="0" cellspacing="0" style="max-width:640px; background-color:#0d1016; border:1px solid #2a2f3a; border-radius:20px; overflow:hidden;">

			          <tr>
			            <td style="padding:28px 32px 0 32px;">
			              <div style="font-size:28px; font-weight:800; color:#ef3b8f; letter-spacing:-0.5px;">
			                Bloom
			              </div>
			            </td>
			          </tr>

			          <tr>
			            <td style="padding:20px 32px 0 32px;">
			              <table width="100%%" cellpadding="0" cellspacing="0">
			                <tr>
			                  <td style="background-color:#121722; border:1px solid #2e3442; border-radius:16px; padding:28px;">
			                    <div style="font-size:34px; line-height:1.25; font-weight:800; color:#f7f8fb; letter-spacing:-0.8px; margin-bottom:18px;">
			                      Connect, Sustain, Blossom!
			                    </div>

			                    <div style="font-size:15px; line-height:1.9; color:#c9ced8; margin-bottom:18px;">
			                      안녕하세요.<br>
			                      회원님은 <strong style="color:#ffffff;">Bloom 서비스의 조직원</strong>으로 초대되었습니다.
			                    </div>

			                    <div style="font-size:15px; line-height:1.9; color:#c9ced8; margin-bottom:18px;">
			                      Bloom은 직장인들의 건강한 메타케어를 위한 공간으로,<br>
			                      조직 안에서 더 나은 연결과 지속 가능한 회복 경험을 지원합니다.
			                    </div>

			                    <div style="font-size:15px; line-height:1.9; color:#c9ced8; margin-bottom:26px;">
			                      아래 버튼을 눌러 <strong style="color:#ffffff;">Google 계정으로 로그인</strong>하고<br>
			                      초대를 수락해 주세요.
			                    </div>

			                    <div style="margin-bottom:28px;">
			                      <a href="%s"
			                         style="display:inline-block; background-color:#ef3b8f; color:#ffffff; text-decoration:none; padding:14px 28px; border-radius:10px; font-size:15px; font-weight:700;">
			                        Google 계정으로 시작하기
			                      </a>
			                    </div>

			                    <div style="height:2px; background:linear-gradient(90deg, #ef3b8f 0%%, #ef3b8f 22%%, rgba(239,59,143,0.18) 55%%, rgba(239,59,143,0) 100%%); border-radius:999px; margin-bottom:10px;"></div>

			                    <div style="font-size:13px; line-height:1.8; color:#8c95a3;">
			                      버튼이 동작하지 않는 경우 아래 링크를 직접 열어 주세요.
			                    </div>
			                    <div style="font-size:13px; line-height:1.8; word-break:break-all; margin-top:6px;">
			                      <a href="%s" style="color:#ef3b8f; text-decoration:none;">%s</a>
			                    </div>
			                  </td>
			                </tr>
			              </table>
			            </td>
			          </tr>

			          <tr>
			            <td style="padding:18px 32px 10px 32px;">
			              <div style="font-size:12px; line-height:1.8; color:#7d8694;">
			                본 메일은 조직 참여를 위한 안내 메일입니다.<br>
			                본인이 요청하지 않은 경우 이 메일을 무시하셔도 됩니다.
			              </div>
			            </td>
			          </tr>

			          <tr>
			            <td style="padding:0 32px 28px 32px;">
			              <div style="font-size:12px; color:#5f6673;">
			                © Bloom
			              </div>
			            </td>
			          </tr>

			        </table>
			      </td>
			    </tr>
			  </table>
			</body>
			</html>
			""".formatted(loginUrl, loginUrl, loginUrl);

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
							.html(
								Content.builder()
									.data(htmlBody)
									.charset("UTF-8")
									.build()
							)
							.build()
					)
					.build()
			)
			.build();

		SendEmailResponse response = sesClient.sendEmail(request);
		log.info("Invite email sent via SES. email={}, messageId={}", email, response.messageId());
	}
}