package ru.practicum.ewm.mainservice.category.controller;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class CategoryAdminControllerTest {

    @Autowired
    MockMvc mockMvc;

//    @Test
//    @SneakyThrows
//    void addCategory_isValid_ReturnsCreatedCategory() {
//        // given
//        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post("/admin/category")
//                .contentType("application/json")
//                .content("{\n" +
//                        "  \"id\": 1,\n" +
//                        "  \"name\": \"Концерты\"\n" +
//                        "}");
//
//        // when
//        mockMvc.perform(requestBuilder)
//                // then
//                .andExpectAll(
//                        status().isCreated(),
//                        openApi().isValid("/static/ewm-main-service-spec.json"),
//                        content().contentTypeCompatibleWith("application/json"),
//                        content().json("{\n" +
//                                "  \"id\": 1,\n" +
//                                "  \"name\": \"Концерты\"\n" +
//                                "}"),
//                        jsonPath("$.id").exists()
//                );
//    }
//
//    @Test
//    @SneakyThrows
//    void addCategory_isInvalid_ReturnsCreatedCategory() {
//        // given
//        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post("/admin/category")
//                .contentType("application/json")
//                .content("{\n" +
//                        "  \"id\": 1,\n" +
//                        "  \"name\": \"Концерты\"\n" +
//                        "}");
//
//        // when
//        mockMvc.perform(requestBuilder)
//                // then
//                .andExpectAll(
//                        status().isCreated(),
//                        openApi().isValid("/static/ewm-main-service-spec.json"),
//                        content().contentTypeCompatibleWith("application/json"),
//                        content().json("{\n" +
//                                "  \"id\": 1,\n" +
//                                "  \"name\": \"Концерты\"\n" +
//                                "}"),
//                        jsonPath("$.id").exists()
//                );
//    }

    @Test
    @SneakyThrows
    void deleteCategory() {
    }

    @Test
    @SneakyThrows
    void updateCategory() {
    }
}