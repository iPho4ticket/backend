package com.ticketing.userservice.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ticketing.userservice.domain.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

}
