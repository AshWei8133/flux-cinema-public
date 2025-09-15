package com.flux.movieproject.repository.event;

import org.springframework.data.jpa.repository.JpaRepository;

import com.flux.movieproject.model.entity.event.LotteryWinner;

public interface LotteryWinnerRepository extends JpaRepository<LotteryWinner, Integer> {

}
