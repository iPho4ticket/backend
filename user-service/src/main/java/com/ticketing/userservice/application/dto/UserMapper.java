package com.ticketing.userservice.application.dto;

import static com.ticketing.userservice.application.dto.UserDto.*;

import org.springframework.data.domain.Page;

import com.ticketing.userservice.domain.User;

public class UserMapper {
	public static User entityFrom(Create createDto) {
		return User.builder()
			.name(createDto.name())
			.email(createDto.email())
			.password(createDto.password())
			.role(createDto.role())
			.phoneNumber(createDto.phoneNumber())
			.build();
	}

	public static Result dtoFrom(User user) {
		return Result.builder()
			.id(user.getId())
			.name(user.getName())
			.email(user.getEmail())
			.role(user.getRole())
			.phoneNumber(user.getPhoneNumber())
			.build();
	}

	public static Delete.Result deleteDtoFrom(User user) {
		return Delete.Result.builder()
			.id(user.getId())
			.deleterId(user.getDeletedBy())
			.deletedAt(user.getDeletedAt())
			.build();
	}

	public static Page<Result> dtoFrom(Page<User> users) {
		return users.map(UserMapper::dtoFrom);
	}

}
