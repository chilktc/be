package be.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GoogleTokenResponse(
	@JsonProperty("access_token")
	String accessToken,

	@JsonProperty("expires_in")
	Integer expiresIn,

	@JsonProperty("token_type")
	String tokenType,

	@JsonProperty("id_token")
	String idToken
) {}