package com.flux.movieproject.service.event;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.flux.movieproject.model.dto.event.EventCategoryDTO;
import com.flux.movieproject.model.entity.event.EventCategory;
import com.flux.movieproject.repository.event.EventCategoryRepository;

@Service
@Transactional(readOnly = true)
public class EventCategoryService {

	@Autowired
	private EventCategoryRepository eventCategoryRepository;

	/**
	 * 獲取所有活動類型
	 */
	// 【修正 1】: 將方法名稱從 findAllCategories 改為 findAll，以匹配 Controller 的呼叫
	public List<EventCategoryDTO> findAll() {
		List<EventCategory> categories = eventCategoryRepository.findAll();

		List<EventCategoryDTO> dtoList = new ArrayList<>();
		for (EventCategory category : categories) {
			dtoList.add(convertToDTO(category));
		}
		return dtoList;
	}

	/**
	 * 新增一個活動類型
	 */
	@Transactional
	public EventCategoryDTO createCategory(EventCategoryDTO categoryDTO) {
		if (eventCategoryRepository.existsByEventCategoryName(categoryDTO.eventCategoryName())) {
			throw new IllegalArgumentException("活動類型名稱 '" + categoryDTO.eventCategoryName() + "' 已存在。");
		}

		EventCategory category = convertToEntity(categoryDTO);
		EventCategory savedCategory = eventCategoryRepository.save(category);
		return convertToDTO(savedCategory);
	}

	/**
	 * 更新一個活動類型
	 */
	@Transactional
	public EventCategoryDTO updateCategory(Integer eventCategoryId, EventCategoryDTO categoryDTO) {
		Optional<EventCategory> existingCategoryOptional = eventCategoryRepository.findById(eventCategoryId);
		if (existingCategoryOptional.isEmpty()) {
			throw new RuntimeException("找不到 ID 為 " + eventCategoryId + " 的活動類型。");
		}
		EventCategory existingCategory = existingCategoryOptional.get();

		if (!existingCategory.getEventCategoryName().equals(categoryDTO.eventCategoryName())
				&& eventCategoryRepository.existsByEventCategoryName(categoryDTO.eventCategoryName())) {
			throw new IllegalArgumentException("活動類型名稱 '" + categoryDTO.eventCategoryName() + "' 已存在。");
		}

		existingCategory.setEventCategoryName(categoryDTO.eventCategoryName());
		existingCategory.setDescription(categoryDTO.description());

		EventCategory updatedCategory = eventCategoryRepository.save(existingCategory);
		return convertToDTO(updatedCategory);
	}

	/**
	 * 刪除一個活動類型
	 */
	@Transactional
	public void deleteCategory(Integer eventCategoryId) {
		if (!eventCategoryRepository.existsById(eventCategoryId)) {
			throw new RuntimeException("找不到 ID 為 " + eventCategoryId + " 的活動類型，無法刪除。");
		}
		eventCategoryRepository.deleteById(eventCategoryId);
	}

	/**
	 * 將 EventCategory 實體轉換為 DTO
	 */
	private EventCategoryDTO convertToDTO(EventCategory category) {
		// 【修正 2】: 確保這裡呼叫的 getter 方法與您 EventCategory Entity 中的欄位名稱完全匹配
		// 假設您 Entity 中的 ID 欄位是 eventCategoryId，對應的 getter 就是 getEventCategoryId()
		return new EventCategoryDTO(
            category.getEventCategoryId(), 
            category.getEventCategoryName(),
			category.getDescription()
        );
	}

	/**
	 * 將 DTO 轉換為 EventCategory 實體
	 */
	private EventCategory convertToEntity(EventCategoryDTO dto) {
		EventCategory category = new EventCategory();
		category.setEventCategoryName(dto.eventCategoryName());
		category.setDescription(dto.description());
		return category;
	}
}
