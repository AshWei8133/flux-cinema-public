package com.flux.movieproject.service.admin;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.flux.movieproject.model.entity.admin.Admin;
import com.flux.movieproject.repository.admin.AdminRepository;

@Service
public class AdminService {

	@Autowired
	AdminRepository adminRepo;

	/**
	 * 透過帳號及密碼檢查登入資訊
	 * @param admin 登入之管理員帳密
	 * @return 管理員資訊
	 */
	public Admin adminLogin(Admin admin) {
		Optional<Admin> loginAdminOptional = adminRepo.findByAdminNameAndPassword(admin.getAdminName(), admin.getPassword());
		if (loginAdminOptional.isPresent()) {
			return loginAdminOptional.get();
		}
		return null;
	}
	

}
