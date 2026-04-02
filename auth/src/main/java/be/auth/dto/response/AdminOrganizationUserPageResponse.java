package be.auth.dto.response;

import java.util.List;

public record AdminOrganizationUserPageResponse(
	List<AdminOrganizationUserItemResponse> items,
	int page,
	int size,
	long totalElements,
	int totalPages,
	boolean hasNext
) {
}
