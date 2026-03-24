package ru.yandex.practicum.filmorate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.review.ReviewService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReviewController.class)
public class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ReviewService reviewService;

    private Review testReview;

    @BeforeEach
    public void setUp() {
        testReview = Review.builder()
                .reviewId(1L)
                .content("Тестовый отзыв")
                .isPositive(true)
                .userId(10L)
                .filmId(20L)
                .useful(0)
                .build();
    }

    @Test
    public void create_ReturnsCreatedReview_whenDataIsValid() throws Exception {
        when(reviewService.create(any(Review.class))).thenReturn(testReview);

        mockMvc.perform(post("/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testReview)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.reviewId").value(1))
                .andExpect(jsonPath("$.content").value("Тестовый отзыв"))
                .andExpect(jsonPath("$.isPositive").value(true));
    }

    @Test
    public void update_ReturnsUpdatedReview_whenDataIsValid() throws Exception {
        Review updateRequest = Review.builder()
                .reviewId(1L)
                .content("Обновлённый отзыв")
                .isPositive(false)
                .userId(10L)
                .filmId(20L)
                .build();

        Review updatedReview = Review.builder()
                .reviewId(1L)
                .content("Обновлённый отзыв")
                .isPositive(false)
                .userId(10L)
                .filmId(20L)
                .useful(0)
                .build();

        when(reviewService.update(any(Review.class))).thenReturn(updatedReview);

        mockMvc.perform(put("/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Обновлённый отзыв"))
                .andExpect(jsonPath("$.isPositive").value(false));
    }

    @Test
    public void delete_RemovesReview_whenReviewExists() throws Exception {
        doNothing().when(reviewService).delete(1L);

        mockMvc.perform(delete("/reviews/{id}", 1L))
                .andExpect(status().isNoContent());
    }

    @Test
    public void findById_ReturnsReview_whenExists() throws Exception {
        when(reviewService.findById(1L)).thenReturn(testReview);

        mockMvc.perform(get("/reviews/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reviewId").value(1))
                .andExpect(jsonPath("$.content").value("Тестовый отзыв"));
    }

    @Test
    public void findAll_ReturnsListOfReviews() throws Exception {
        List<Review> reviews = List.of(testReview);
        when(reviewService.findAll(eq(null), eq(10))).thenReturn(reviews);

        mockMvc.perform(get("/reviews")
                        .param("count", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].reviewId").value(1));
    }

    @Test
    public void addLike_ReturnsUpdatedReview_withIncreasedUseful() throws Exception {
        Review updatedReview = Review.builder()
                .reviewId(1L)
                .useful(1)
                .build();

        doNothing().when(reviewService).addLike(1L, 10L);
        when(reviewService.findById(1L)).thenReturn(updatedReview);

        mockMvc.perform(put("/reviews/{id}/like/{userId}", 1L, 10L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.useful").value(1));
    }

    @Test
    public void addDislike_ReturnsUpdatedReview_withDecreasedUseful() throws Exception {
        Review updatedReview = Review.builder()
                .reviewId(1L)
                .useful(-1)
                .build();

        doNothing().when(reviewService).addDislike(1L, 10L);
        when(reviewService.findById(1L)).thenReturn(updatedReview);

        mockMvc.perform(put("/reviews/{id}/dislike/{userId}", 1L, 10L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.useful").value(-1));
    }

    @Test
    public void removeLike_RemovesLike_ReturnsNoContent() throws Exception {
        doNothing().when(reviewService).removeLike(1L, 10L);

        mockMvc.perform(delete("/reviews/{id}/like/{userId}", 1L, 10L))
                .andExpect(status().isNoContent());
    }

    @Test
    public void removeDislike_RemovesDislike_ReturnsNoContent() throws Exception {
        doNothing().when(reviewService).removeDislike(1L, 10L);

        mockMvc.perform(delete("/reviews/{id}/dislike/{userId}", 1L, 10L))
                .andExpect(status().isNoContent());
    }

    @Test
    public void findById_ThrowsNotFoundException_whenReviewDoesNotExist() throws Exception {
        when(reviewService.findById(999L))
                .thenThrow(new NotFoundException("Отзыв с ID 999 не найден"));

        mockMvc.perform(get("/reviews/{id}", 999L))
                .andExpect(status().isNotFound());
    }
}