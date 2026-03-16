package be.auth.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import be.auth.domain.ProfileImage;
import be.auth.domain.ProfileImageType;

public interface ProfileImageRepository extends JpaRepository<ProfileImage, Long> {
	List<ProfileImage> findTop10ByType(ProfileImageType type);
}
