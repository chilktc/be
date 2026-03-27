package be.auth.event;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import be.auth.service.InvitedEmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.ses.model.SesException;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserInviteEmailListener {
	private final InvitedEmailService invitedEmailService;

	@Async
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handleUserInvited(UserInvitedEvent event) {
		try {
			invitedEmailService.sendInviteEmail(event.email());
		} catch (SesException e) {
			log.error(
				"SES email send failed. email={}, awsMessage={}, statusCode={}",
				event.email(),
				e.awsErrorDetails() != null ? e.awsErrorDetails().errorMessage() : e.getMessage(),
				e.statusCode(),
				e
			);
		} catch (Exception e) {
			log.error("Unexpected email send failed. email={}", event.email(), e);
		}
	}
}