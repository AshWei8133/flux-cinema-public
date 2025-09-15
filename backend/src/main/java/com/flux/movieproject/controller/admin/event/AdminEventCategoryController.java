package com.flux.movieproject.controller.admin.event; 

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.flux.movieproject.model.dto.event.EventCategoryDTO;
import com.flux.movieproject.service.event.EventCategoryService;

@RestController
@RequestMapping("/api/admin/events-categories") 
public class AdminEventCategoryController {

    @Autowired
    private EventCategoryService eventCategoryService;

    /**
     * 獲取所有活動類型
     * @return 包含所有活動類型DTO的ResponseEntity
     */
    @GetMapping
    public ResponseEntity<List<EventCategoryDTO>> getAllCategories() {
        List<EventCategoryDTO> categories = eventCategoryService.findAll();
        return ResponseEntity.ok(categories);
    }
    
    /**
     * 新增一個活動類型
     * @param categoryDTO 包含要新增的活動類型資料
     * @return 包含已新增活動類型DTO的ResponseEntity
     */
    @PostMapping
    public ResponseEntity<EventCategoryDTO> createCategory(@RequestBody EventCategoryDTO categoryDTO) {
        EventCategoryDTO createdCategory = eventCategoryService.createCategory(categoryDTO);
        return ResponseEntity.ok(createdCategory);
    }

    /**
     * 更新一個活動類型
     * @param eventCategoryId 要更新的活動類型ID
     * @param categoryDTO 包含更新後資料的活動類型DTO
     * @return 包含已更新活動類型DTO的ResponseEntity
     */
    @PutMapping("/{eventCategoryId}")
    public ResponseEntity<EventCategoryDTO> updateCategory(@PathVariable Integer eventCategoryId, @RequestBody EventCategoryDTO categoryDTO) {
        EventCategoryDTO updatedCategory = eventCategoryService.updateCategory(eventCategoryId, categoryDTO);
        return ResponseEntity.ok(updatedCategory);
    }

    /**
     * 刪除一個活動類型
     * @param eventCategoryId 要刪除的活動類型ID
     * @return 沒有內容的ResponseEntity，狀態碼為204
     */
    @DeleteMapping("/{eventCategoryId}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Integer eventCategoryId) {
        eventCategoryService.deleteCategory(eventCategoryId);
        return ResponseEntity.noContent().build();
    }
}