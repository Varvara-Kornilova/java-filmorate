package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class MpaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnAllMpa() throws Exception {
        mockMvc.perform(get("/mpa"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(5)); // 5 рейтингов
    }

    @Test
    void shouldReturnMpaById() throws Exception {
        mockMvc.perform(get("/mpa/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("G")); // или "0+" для российской системы
    }

    @Test
    void shouldReturnNotFoundForInvalidMpaId() throws Exception {
        mockMvc.perform(get("/mpa/999"))
                .andExpect(status().isNotFound());
    }
}
