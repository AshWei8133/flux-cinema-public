package com.flux.movieproject.repository.admin;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.flux.movieproject.model.entity.admin.Admin;


public interface AdminRepository extends JpaRepository<Admin, Integer> {
	/**
	 * 透過管理員帳號及密碼查找管理員
	 * @param adminName 管理員帳號
	 * @param password 管理員密碼
	 * @return 管理員資訊
	 */
	Optional<Admin> findByAdminNameAndPassword(String adminName, String password);
}
