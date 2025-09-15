package com.flux.movieproject.enums;

/**
 * 定義排片狀態的列舉。
 * 每個狀態都代表了排片進度的不同階段。
 */
public enum MovieSessionStatus {
	/** 待排片 (PENDING): 表示該日期尚未完成排片，還有上印中電影未排完。 */
    PENDING,
    
    /** 已完成 (COMPLETED): 表示該日期的排片已完成，可以開始售票。 */
    COMPLETED,
    
    /** 今日排片 (TODAY): 特別標註為今天，以便前端介面高亮顯示。 */
    TODAY,
    
    /** 歷史紀錄 (HISTORY): 標註過往的排片紀錄，無法修改。 */
    HISTORY,
    
    /** 無片可排 (NONE): 表示該日期無需排片，或因特殊原因無片可排。 */
    NONE
}
