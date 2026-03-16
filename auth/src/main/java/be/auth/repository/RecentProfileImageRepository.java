package be.auth.repository;

import be.auth.domain.RecentProfileImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RecentProfileImageRepository extends JpaRepository<RecentProfileImage, Long>{
	List<RecentProfileImage> findTop10ByUserIdOrderByUsedAtDesc(UUID userId);

	List<RecentProfileImage> findTop11ByUserIdOrderByUsedAtDesc(UUID userId);

	Optional<RecentProfileImage> findByUserIdAndProfileImage_Id(UUID userId, Long imageId);

}
