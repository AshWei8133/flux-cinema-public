package com.flux.movieproject.controller.fluxapp;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.flux.movieproject.model.dto.theater.ShowTheaterTypesResponseDTO;
import com.flux.movieproject.service.theater.TheaterService;

@RestController
@RequestMapping("/api/theater")
public class TheaterController {

	@Autowired
	TheaterService theaterService;

	/**
	 * 列出所有影廳類別資訊(提供下拉式選單擷取名字使用)
	 * 
	 * @return 影廳類別資料
	 */
	@GetMapping("/theaterTypes")
	public List<ShowTheaterTypesResponseDTO> getAllTheaterTypes() {
		return theaterService.getAllTheaterTypes();
	}
}
