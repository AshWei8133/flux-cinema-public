package com.flux.movieproject.controller.admin.theater;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.flux.movieproject.exception.TheaterTypeCommitException;
import com.flux.movieproject.model.dto.theater.CreateTheaterRequestDTO;
import com.flux.movieproject.model.dto.theater.ShowTheaterTypesResponseDTO;
import com.flux.movieproject.model.dto.theater.ShowTheatersDetailResponseDTO;
import com.flux.movieproject.model.dto.theater.ShowTheatersResponseDTO;
import com.flux.movieproject.model.dto.theater.TheaterTypeChangesRequest;
import com.flux.movieproject.model.dto.theater.UpdateTheaterRequestDTO;
import com.flux.movieproject.model.dto.theater.UpdateTheaterResponseDTO;
import com.flux.movieproject.service.theater.TheaterService;

import org.springframework.web.bind.annotation.PutMapping;

/**
 * API 影廳展示與查詢： 1. 展示所有影廳列表: GET /api/admin/theater 2. 取得單一影廳詳細資訊: GET
 * /api/admin/theater/{theaterId} 3. 透過影廳類型篩選影廳: GET
 * /api/admin/theater?theaterTypeId={theaterTypeId}
 * 
 * 影廳類型篩選控制器： 1. 顯示所有影廳類型供選擇: GET /api/admin/theater/theaterTypes
 * 
 * 影廳新增 1. 新增影廳(含座位): POST /api/admin/theater
 * 
 * 更新影廳基本資訊 1. 更新影廳基本資訊: PUT /api/admin/theater/{theaterId}
 * 
 * 
 */
@RestController
@RequestMapping("/api/admin/theater")
public class AdminTheaterController {

	@Autowired
	TheaterService theaterService;

	/**
	 * 列出所有影廳資訊
	 * 
	 * @return 影廳清單
	 */
	@GetMapping
	public ResponseEntity<List<ShowTheatersResponseDTO>> getAllTheaters(@RequestParam Optional<Integer> theaterTypeId) {
		// 如果有取得theaterTypeId篩選條件
		if (theaterTypeId.isPresent()) {
			return ResponseEntity.ok(theaterService.getTheatersByTheaterType(theaterTypeId.get()));
		}
		// 沒有參數就查詢全部
		return ResponseEntity.ok(theaterService.getAllTheaters());
	}

	/**
	 * 列出特定影廳的詳細資訊
	 * 
	 * @param theaterId 影廳id
	 * @return 影廳詳細資料(含座位資訊)
	 */
	@GetMapping("/{theaterId}")
	public ResponseEntity<ShowTheatersDetailResponseDTO> getTheaterDetailById(@PathVariable Integer theaterId) {
		return ResponseEntity.ok(theaterService.getTheaterDetailById(theaterId));
	}

	/**
	 * 列出所有影廳類別資訊(提供下拉式選單擷取名字使用)
	 * 
	 * @return 影廳類別資料
	 */
	@GetMapping("/theaterTypes")
	public List<ShowTheaterTypesResponseDTO> getAllTheaterTypes() {
		return theaterService.getAllTheaterTypes();
	}
	
	/**
	 * 處理前端傳送的增、刪、改影廳類別數據
	 * @param request 前端傳來的增刪改內容(包含3個List分別為增、刪、改)
	 * @return
	 */
	@PostMapping("/theaterTypes/commitChanges")
	public ResponseEntity<UpdateTheaterResponseDTO> commitTheaterTypesChanges(@RequestBody TheaterTypeChangesRequest request){
		try {
			UpdateTheaterResponseDTO response = theaterService.processTheaterTypeChanges(request);
			return ResponseEntity.ok(response);
		} 
		catch (TheaterTypeCommitException e) {
			UpdateTheaterResponseDTO errorResponse = new UpdateTheaterResponseDTO();
			errorResponse.setSuccess(false);
			errorResponse.setMessage(e.getMessage());
			return ResponseEntity.badRequest().body(errorResponse);
		}
		catch (Exception e) {
			UpdateTheaterResponseDTO errorResponse = new UpdateTheaterResponseDTO();
			errorResponse.setSuccess(false);
			errorResponse.setMessage("伺服器發生非預期錯誤：" + e.getMessage());
			return ResponseEntity.status(500).body(errorResponse);
		}
	}
	

	/**
	 * 新增影廳操作
	 * 
	 * @param request
	 * @return
	 */
	@PostMapping
	public ResponseEntity<UpdateTheaterResponseDTO> createTheater(@RequestBody CreateTheaterRequestDTO request) {
		try {
			UpdateTheaterResponseDTO response = theaterService.createTheater(request);
			return new ResponseEntity<>(response, HttpStatus.CREATED); // 201
		} catch (RuntimeException e) {
			UpdateTheaterResponseDTO errorResponse = new UpdateTheaterResponseDTO();
			errorResponse.setSuccess(false);
			errorResponse.setMessage("新增影廳失敗");
			return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
		}
	}

	/**
	 * 更新影廳操作
	 * 
	 * @param request 前端傳送的更新數據
	 * @return
	 */
	@PutMapping("/{theaterId}")
	public ResponseEntity<UpdateTheaterResponseDTO> updateTheater(@RequestBody UpdateTheaterRequestDTO request,
			@PathVariable Integer theaterId) {
		try {
			UpdateTheaterResponseDTO response = theaterService.updateTheater(request, theaterId);
			return new ResponseEntity<>(response, HttpStatus.OK); // 200
		} catch (RuntimeException e) {
			UpdateTheaterResponseDTO errorResponse = new UpdateTheaterResponseDTO();
			errorResponse.setSuccess(false);
			errorResponse.setMessage("更新影廳失敗");
			return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
		}
	}

	@DeleteMapping("/{theaterId}")
	public ResponseEntity<UpdateTheaterResponseDTO> deleteTheater(@PathVariable Integer theaterId) {
		try {
			UpdateTheaterResponseDTO response = theaterService.deleteTheater(theaterId);
			return new ResponseEntity<>(response, HttpStatus.OK); // 200
		} catch (RuntimeException e) {
			UpdateTheaterResponseDTO errorResponse = new UpdateTheaterResponseDTO();
			errorResponse.setSuccess(false);
			errorResponse.setMessage("刪除影廳失敗");
			return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
		}
	}

}
