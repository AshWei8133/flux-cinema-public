package com.flux.movieproject.repository.event;

import org.springframework.data.jpa.repository.JpaRepository;

import com.flux.movieproject.model.entity.event.CouponCategory;

public interface CouponCategoryRepository extends JpaRepository<CouponCategory, Integer> {

}
