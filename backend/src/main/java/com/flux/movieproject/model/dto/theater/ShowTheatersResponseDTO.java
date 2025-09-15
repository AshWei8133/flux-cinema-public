package com.flux.movieproject.model.dto.theater;

import java.util.Base64;

import lombok.Data;

@Data
public class ShowTheatersResponseDTO {
	private Integer theaterId;
	private String theaterName;
	private Integer theaterTypeId;
	private String theaterTypeName;
	private Integer totalSeats;
	private String theaterPhotoImgBase64;

	public void setPosterImgBase64(byte[] theaterPhoto) {
		if (theaterPhoto != null && theaterPhoto.length > 0) {
			String base64String = Base64.getEncoder().encodeToString(theaterPhoto);
			String photo = "data:image/jpeg;base64," + base64String;
			this.theaterPhotoImgBase64 = photo;
		}
	}
}
