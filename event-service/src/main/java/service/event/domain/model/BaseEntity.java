package service.event.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.Comment;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Setter
@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Slf4j
public abstract class BaseEntity {
    @CreatedDate
    @Column(updatable = false, columnDefinition = "TIMESTAMP", nullable = false)
    @Comment("생성일시")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(columnDefinition = "TIMESTAMP")
    @Comment("수정일시")
    private LocalDateTime updatedAt;

    @Column(columnDefinition = "TIMESTAMP")
    @Comment("삭제일시")
    private LocalDateTime deletedAt;

    @CreatedBy
    @Column(updatable = false)
    private UUID createdBy;

    @LastModifiedBy
    private UUID updatedBy;

    private UUID deletedBy;

    private boolean isDeleted = false;

    public void delete(UUID handlerId) {
        this.deletedBy = handlerId;
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
    }
}
