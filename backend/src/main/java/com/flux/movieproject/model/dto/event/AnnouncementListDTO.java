package com.flux.movieproject.model.dto.event;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AnnouncementListDTO {
    private Integer id;
    private String title;
    private LocalDate publishDate;
    private String content;
}

