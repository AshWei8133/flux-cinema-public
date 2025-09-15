package com.flux.movieproject.service.theater;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.flux.movieproject.exception.TheaterTypeCommitException;
import com.flux.movieproject.model.dto.theater.CreateTheaterRequestDTO;
import com.flux.movieproject.model.dto.theater.ShowSeatsResponseDTO;
import com.flux.movieproject.model.dto.theater.ShowTheaterTypesResponseDTO;
import com.flux.movieproject.model.dto.theater.ShowTheatersDetailResponseDTO;
import com.flux.movieproject.model.dto.theater.ShowTheatersResponseDTO;
import com.flux.movieproject.model.dto.theater.TheaterTypeChangesRequest;
import com.flux.movieproject.model.dto.theater.TheaterTypeCreateDto;
import com.flux.movieproject.model.dto.theater.TheaterTypeUpdateDto;
import com.flux.movieproject.model.dto.theater.UpdateTheaterRequestDTO;
import com.flux.movieproject.model.dto.theater.UpdateTheaterResponseDTO;
import com.flux.movieproject.model.entity.theater.Seat;
import com.flux.movieproject.model.entity.theater.Theater;
import com.flux.movieproject.model.entity.theater.TheaterType;
import com.flux.movieproject.repository.theater.TheaterRepository;
import com.flux.movieproject.repository.theater.TheaterTypeRepository;
import com.flux.movieproject.utils.PictureConverter;

@Service
@Transactional(readOnly = true) // 避免fetch lazy時沒有載入到TheaterType
public class TheaterService {

	@Autowired
	TheaterRepository theaterRepo;
	@Autowired
	TheaterTypeRepository theaterTypeRepo;

	/**
	 * 取得所有影廳資料
	 * 
	 * @return
	 */
	public List<ShowTheatersResponseDTO> getAllTheaters() {
		// 創建影廳列表展示物件
		List<ShowTheatersResponseDTO> theaterList = new ArrayList<>();
		// 透過dao擷取全部影廳資料
		List<Theater> theaters = theaterRepo.findAllWithTheaterType();
		// 將取得的影廳資料封裝成dto
		for (Theater theater : theaters) {
			ShowTheatersResponseDTO dto = changeTheaterToShowTheatersResponseDTO(theater);
			theaterList.add(dto);
		}

		return theaterList;
	}

	/**
	 * 透過id取得影廳詳細資料
	 * 
	 * @param theaterId 影廳id
	 * @return
	 */
	public ShowTheatersDetailResponseDTO getTheaterDetailById(Integer theaterId) {
		// 透過影廳id查詢影廳資料
		Optional<Theater> theaterOptional = theaterRepo.findById(theaterId);
		// 如果有查到相關資料，將該資料封裝成dto
		if (theaterOptional.isPresent()) {
			Theater theater = theaterOptional.get();
			ShowTheatersResponseDTO theaterBaseDto = changeTheaterToShowTheatersResponseDTO(theater);
			ShowTheatersDetailResponseDTO theaterDto = new ShowTheatersDetailResponseDTO();
			theaterDto.setShowTheatersResponseDTO(theaterBaseDto);

			List<ShowSeatsResponseDTO> seatDtos = new ArrayList<>();
			List<Seat> seats = theater.getSeats();

			for (Seat seat : seats) {
				ShowSeatsResponseDTO seatDto = new ShowSeatsResponseDTO();
				seatDto.setSeatId(seat.getSeatId());
				seatDto.setSeatType(seat.getSeatType());
				seatDto.setRowNumber(seat.getRowNumber());
				seatDto.setColumnNumber(seat.getColumnNumber());
				seatDto.setIsActive(seat.getIsActive());

				seatDtos.add(seatDto);
			}

			theaterDto.setSeats(seatDtos);

			return theaterDto;

		}
		return null;
	}

	/**
	 * 透過影廳類別id取得影廳資料
	 * 
	 * @param theaterTypeId
	 * @return
	 */
	public List<ShowTheatersResponseDTO> getTheatersByTheaterType(Integer theaterTypeId) {
		TheaterType theaterType = theaterTypeRepo.findById(theaterTypeId).get();
		List<Theater> theaters = theaterType.getTheaters();

		List<ShowTheatersResponseDTO> theaterList = new ArrayList<>();

		// 將取得的影廳資料封裝成dto
		for (Theater theater : theaters) {
			ShowTheatersResponseDTO dto = changeTheaterToShowTheatersResponseDTO(theater);
			theaterList.add(dto);
		}
		return theaterList;
	}

	/**
	 * 取得所有影廳資料(用於下拉式選單)
	 * 
	 * @return
	 */
	public List<ShowTheaterTypesResponseDTO> getAllTheaterTypes() {
		List<TheaterType> theaters = theaterTypeRepo.findAll();
		List<ShowTheaterTypesResponseDTO> dtos = new ArrayList<>();

		for (TheaterType theaterType : theaters) {
			ShowTheaterTypesResponseDTO dto = new ShowTheaterTypesResponseDTO();
			dto.setTheaterTypeId(theaterType.getTheaterTypeId());
			dto.setTheaterTypeName(theaterType.getTheaterTypeName());
			dto.setDescription(theaterType.getDescription());

			dtos.add(dto);
		}

		return dtos;
	}

	/**
	 * 新增影廳
	 * 
	 * @param request
	 * @return
	 */
	@Transactional
	public UpdateTheaterResponseDTO createTheater(CreateTheaterRequestDTO request) {
		// 取得影廳類別實體
		TheaterType theaterType = theaterTypeRepo.findById(request.getTheaterTypeId()).get();

		// 建立Theater實體並建立屬性
		Theater theater = new Theater();
		theater.setTheaterName(request.getTheaterName());
		theater.setTotalSeats(request.getTotalSeats());
		theater.setTheaterType(theaterType);

		// 處理影廳圖片
		byte[] photoBytes = PictureConverter.convertBase64ToBytes(request.getTheaterPhoto());
		theater.setTheaterPhoto(photoBytes);

		// 建立座位實體並與影廳建立關聯
		if (request.getSeats() != null && !request.getSeats().isEmpty()) {
			List<Seat> seats = request.getSeats();
			for (Seat seat : seats) {
				seat.setTheater(theater);
			}
			theater.setSeats(seats);
		}

		// 儲存 Theater 實體，JPA 會自動級聯儲存 Seats
		theaterRepo.save(theater);

		// 建立要回應的DTO

		UpdateTheaterResponseDTO response = new UpdateTheaterResponseDTO();
		response.setSuccess(true);
		response.setMessage("新增影廳成功");

		return response;
	}

	/**
	 * 更新影廳
	 * 
	 * @param request
	 * @return
	 */
	@Transactional
	public UpdateTheaterResponseDTO updateTheater(UpdateTheaterRequestDTO request, Integer theaterId) {

		// 0. 建立要回應的DTO
		UpdateTheaterResponseDTO response = new UpdateTheaterResponseDTO();

		// 1. 確認傳送資料的dto影廳id是否與路徑上的theaterId吻合
		if (request.getTheaterId() != theaterId) {
			response.setMessage("請確認要更新的內容與路徑上的影廳id為同一筆資料");
			response.setSuccess(false);
			return response;
		}

		// 2. 調用 TheaterRepository 確認要跟新的影廳編號是否存在，並取出資料
		Optional<Theater> theaterOptional = theaterRepo.findById(theaterId);

		if (theaterOptional.isEmpty()) {
			response.setMessage("要更新的影廳id不存在，請重新確認");
			response.setSuccess(false);
			return response;
		}

		// 3. 取出 dto 資料進行變更
		Theater theater = theaterOptional.get();
		// 3-1 取出對應影廳類別實體資料
		TheaterType theaterType = theaterTypeRepo.findById(request.getTheaterTypeId()).get();
		// 3-2 更新 theater 的資料
		theater.setTheaterName(request.getTheaterName());
		theater.setTotalSeats(request.getTotalSeats());
		theater.setTheaterType(theaterType);

		// 3-3 處理影廳圖片
		byte[] photoBytes = PictureConverter.convertBase64ToBytes(request.getTheaterPhoto());
		theater.setTheaterPhoto(photoBytes);

		// 3-4 操作電影座位

		List<Seat> oldSeats = theater.getSeats(); // 舊的座位
		List<Seat> newSeats = request.getSeats(); // 傳送過來的座位

		// 建立新座位的Map，方便快速查詢
		Map<Integer, Seat> newSeatMap = new HashMap<>();
		if (newSeats != null) {
			for (Seat newSeat : newSeats) {
				if (newSeat.getSeatId() != null) {
					newSeatMap.put(newSeat.getSeatId(), newSeat);
				}
			}
		}

		// 使用 Iterator 來安全地移除舊座位
		Iterator<Seat> oldSeatIterator = oldSeats.iterator();
		while (oldSeatIterator.hasNext()) {
			Seat oldSeat = oldSeatIterator.next();
			// 檢查舊座位是否還在新座位清單中 (透過 Map 快速查詢)
			if (!newSeatMap.containsKey(oldSeat.getSeatId())) {
				oldSeatIterator.remove(); // 執行刪除
			} else {
				// 如果還在新座位清單中，代表是更新操作
				Seat updatedSeatData = newSeatMap.get(oldSeat.getSeatId());
				if (updatedSeatData != null) {
					oldSeat.setSeatType(updatedSeatData.getSeatType());
					oldSeat.setRowNumber(updatedSeatData.getRowNumber());
					oldSeat.setColumnNumber(updatedSeatData.getColumnNumber());
					oldSeat.setIsActive(updatedSeatData.getIsActive());
					newSeatMap.remove(oldSeat.getSeatId()); // 從 Map 中移除，剩下的就是新增的座位
				}
			}
		}

		// 處理新增的座位（沒有 seatId 的座位）
		if (newSeats != null) {
			for (Seat newSeatToAdd : newSeats) {
				// 只有當 seatId 為 null 時才執行新增邏輯
				if (newSeatToAdd.getSeatId() == null) {
					Seat newSeatEntity = new Seat();
					newSeatEntity.setSeatType(newSeatToAdd.getSeatType());
					newSeatEntity.setRowNumber(newSeatToAdd.getRowNumber());
					newSeatEntity.setColumnNumber(newSeatToAdd.getColumnNumber());
					newSeatEntity.setIsActive(newSeatToAdd.getIsActive());
					// 處理關聯
					newSeatEntity.setTheater(theater);
					oldSeats.add(newSeatEntity);
				}
			}
		}

		// 4. 儲存 theater 實體，casecade.all 級聯操作 seat 資料
		theaterRepo.save(theater);

		// 5. 回傳成功訊息給controller
		response.setSuccess(true);
		response.setMessage("更新影廳：" + request.getTheaterName() + " , 成功");
		return response;
	}

	/**
	 * 刪除影廳
	 * 
	 * @param theaterId 影廳id
	 * @return 回應給前端的訊息
	 */
	@Transactional
	public UpdateTheaterResponseDTO deleteTheater(Integer theaterId) {
		UpdateTheaterResponseDTO response = new UpdateTheaterResponseDTO();

		Optional<Theater> theaterOptional = theaterRepo.findById(theaterId);
		if (theaterOptional.isEmpty()) {
			response.setMessage("影廳id不存在，請重新確認");
			response.setSuccess(false);
			return response;
		}

		theaterRepo.deleteById(theaterId);
		response.setMessage("刪除影廳：" + theaterOptional.get().getTheaterName() + ", 成功");
		response.setSuccess(true);
		return response;
	}

	/**
	 * 處理影廳類別確認提交變更操作
	 * 
	 * @param request
	 * @return 返回變更成功、失敗及相關訊息
	 */
	@Transactional
	public UpdateTheaterResponseDTO processTheaterTypeChanges(TheaterTypeChangesRequest request) {
		// 處理刪除
		List<Integer> deletedIdList = request.getDeleted();
		if (deletedIdList != null && !deletedIdList.isEmpty()) {
			// 使用 Set 確保 ID 不重複(雖然 JPA 內部會處理，但業務層更安全)
			Set<Integer> uniqueDeletedIds = new HashSet<>(deletedIdList);

			// 如果有影廳使用者要被刪除的類別，則不可執行刪除操作
			for (Integer deletedId : uniqueDeletedIds) {
				Optional<TheaterType> theaterTypeOptional = theaterTypeRepo.findById(deletedId);
				if (theaterTypeOptional.isEmpty()) {
					// 後端再驗證，以防併發或惡意操作
					throw new TheaterTypeCommitException("刪除失敗：刪除的影廳類別不存在。");
				}

				// 取得關聯的影廳列表
				List<Theater> checkTheaters = theaterTypeOptional.get().getTheaters();
				// 如果有影廳使用了該類別，則拋出例外
				if (checkTheaters != null && !checkTheaters.isEmpty()) {
					throw new TheaterTypeCommitException("有影廳使用了"+ theaterTypeOptional.get().getTheaterTypeName() + "影廳類別，不可執行刪除操作");
				}
			}

			// 批次執行刪除操作
			theaterTypeRepo.deleteAllByIdInBatch(uniqueDeletedIds);
		}

		// 處理新增(確認影廳名稱是否重複)
		List<TheaterTypeCreateDto> addedList = request.getAdded();
		if (addedList != null && !addedList.isEmpty()) {
			for (TheaterTypeCreateDto addDto : addedList) {
				// 不能出現重複的theaterName
				if (theaterTypeRepo.existsByTheaterTypeName(addDto.getTheaterTypeName())) {
					throw new TheaterTypeCommitException("新增失敗：影廳類別名稱'" + addDto.getTheaterTypeName() + "'已存在。");
				}
				TheaterType newTheaterType = new TheaterType();
				newTheaterType.setTheaterTypeName(addDto.getTheaterTypeName());
				newTheaterType.setDescription(addDto.getDescription());
				// 執行新增操作
				theaterTypeRepo.save(newTheaterType);
			}
		}

		// 處理更新
		List<TheaterTypeUpdateDto> updatedList = request.getUpdated();
		if (updatedList != null && !updatedList.isEmpty()) {
			for (TheaterTypeUpdateDto updateDto : updatedList) {
				Integer theaterTypeId = updateDto.getTheaterTypeId();
				// 判斷是否為存在的id
				Optional<TheaterType> theaterTypeOptional = theaterTypeRepo.findById(theaterTypeId);
				if (theaterTypeOptional.isEmpty()) {
					throw new TheaterTypeCommitException("更新失敗：影廳類別不存在");
				}
				// 取得影廳類別實體
				TheaterType existingTheaterType = theaterTypeOptional.get();

				// 檢查更新後的影廳名稱是否重複
				if (updateDto.getChanges().containsKey("theaterTypeName")) {
					String newTheaterTypeName = (String) updateDto.getChanges().get("theaterTypeName");
					// 只有在名稱變更且與其他現有影廳名稱重複時才報錯
					if (!existingTheaterType.getTheaterTypeName().equals(newTheaterTypeName)
							&& theaterTypeRepo.existsByTheaterTypeName(newTheaterTypeName)) {
						throw new TheaterTypeCommitException("更新失敗：影廳類別名稱'" + newTheaterTypeName + "'已存在");
					}
					existingTheaterType.setTheaterTypeName(newTheaterTypeName);
				}

				// 檢查是否有描述欄位的變更
				if (updateDto.getChanges().containsKey("description")) {
					String newDescription = (String) updateDto.getChanges().get("description");
					existingTheaterType.setDescription(newDescription);
				}

				// 執行更新操作
				theaterTypeRepo.save(existingTheaterType);

			}
		}

		// 所有操作都成功，返回成功訊息
		UpdateTheaterResponseDTO successResponse = new UpdateTheaterResponseDTO();
		successResponse.setSuccess(true);
		successResponse.setMessage("所有變更已成功提交。");
		return successResponse;
	}

	// 輔助方法：將Theater物件轉換為ShowTheatersResponseDTO
	private ShowTheatersResponseDTO changeTheaterToShowTheatersResponseDTO(Theater theater) {
		ShowTheatersResponseDTO dto = new ShowTheatersResponseDTO();
		dto.setTheaterId(theater.getTheaterId());
		dto.setTheaterName(theater.getTheaterName());
		dto.setTheaterTypeName(theater.getTheaterType().getTheaterTypeName());
		dto.setTheaterTypeId(theater.getTheaterType().getTheaterTypeId());
		dto.setTotalSeats(theater.getTotalSeats());
		dto.setPosterImgBase64(theater.getTheaterPhoto());
		return dto;
	}

}
