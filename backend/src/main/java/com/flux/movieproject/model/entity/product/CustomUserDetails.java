package com.flux.movieproject.model.entity.product;

import com.flux.movieproject.model.entity.member.Member;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class CustomUserDetails implements UserDetails {
    private final Member member;

    // 將你的 Member 實體傳入建構子
    public CustomUserDetails(Member member) {
        this.member = member;
    }


    public Integer getMemberId() {
        return member.getMemberId();
    }

    @Override
    public String getUsername() {
        return member.getUsername();
    }

    @Override
    public String getPassword() {
        return member.getPassword();
    }

    // 這些是介面必須實作的方法，通常可以先簡單返回 true 或空集合
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList(); // 如果你有角色（例如 ADMIN, USER），可以在這裡回傳
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
