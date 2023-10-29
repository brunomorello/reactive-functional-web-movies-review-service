package com.bmo.moviesinforeview.domain;


import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document
@Builder
public class MovieReview {
    @Id
    private String id;
    private String moveInfoId;
    private String comment;
//    @Min(value = 0l, message = "rating.negative: rating is negative, pls provide a positive value")
    private double rating;
}
