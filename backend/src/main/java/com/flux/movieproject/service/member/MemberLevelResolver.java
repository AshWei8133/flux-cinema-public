package com.flux.movieproject.service.member;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.flux.movieproject.repository.member.MemberLevelRecordRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberLevelResolver {
	
	@Autowired
    private  MemberLevelRecordRepository memberLevelRecordRepository;

    // 暫時方案（如果 DB 還沒加 rank 欄位）
    private static final Map<Integer, Integer> LEVEL_RANK = Map.of(
        1, 1,  // 普通
        2, 2,  // 黃金
        3, 3   // 尊爵
    );

    private int toRank(Integer memberLevelId) {
        if (memberLevelId == null) return 0; // 無紀錄 = 不限
        return LEVEL_RANK.getOrDefault(memberLevelId, 0);
    }

//    /** 取得會員目前的 rank（找不到紀錄 → 0 ） */
//    public int resolveCurrentRank(Integer memberId) {
//        LocalDateTime now = LocalDateTime.now();
//        return memberLevelRecordRepository.findCurrentLevel(memberId, now)
//                .map(r -> toRank(r.getMemberLevelId()))
//                .orElse(0); // 找不到就回 0
//    }
}
