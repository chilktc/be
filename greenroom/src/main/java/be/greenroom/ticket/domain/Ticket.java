package be.greenroom.ticket.domain;

import java.time.LocalDateTime;
import java.util.UUID;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tickets")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Ticket {
	@Id
	@Column(nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private String situation;

    @Column(nullable = false)
    private String thought;

    @Column(nullable = false)
    private String action;

    @Column
    private String colleagueReaction;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private Ticket(UUID userId, String situation, String thought, String action, String colleagueReaction) {
		this.id = UUID.randomUUID();
        this.userId = userId;
        this.situation = situation;
        this.thought = thought;
        this.action = action;
        this.colleagueReaction = colleagueReaction;
    }

    public static Ticket create(UUID userId, String situation, String thought, String action, String colleagueReaction) {
        return new Ticket(userId, situation, thought, action, colleagueReaction);
    }

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
}
