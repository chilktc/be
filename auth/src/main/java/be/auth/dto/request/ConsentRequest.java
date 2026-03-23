package be.auth.dto.request;

public record ConsentRequest(
	boolean agreedTermsOfService,
	boolean agreedPrivacyPolicy,
	boolean agreedMarketing
) {
	public boolean hasRequiredConsents() {
		return agreedTermsOfService && agreedPrivacyPolicy;
	}
}