package com.ticketing.userservice.domain.auditing;

import java.io.Serializable;
import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity implements Serializable {

	@CreatedDate
	@Column(updatable = false)
	private LocalDateTime createdAt;

	@CreatedBy
	private Long createdBy;

	@LastModifiedDate
	@Column
	private LocalDateTime updatedAt;

	@LastModifiedBy
	private Long updatedBy;

	@Setter
	@Column
	private LocalDateTime deletedAt;

	@Setter
	private Long deletedBy;

	@Setter
	private Boolean isDeleted = false;

	public void softDelete(Long deletedBy) {
		this.deletedAt = LocalDateTime.now();
		this.isDeleted = true;
		this.deletedBy = deletedBy;
	}

}