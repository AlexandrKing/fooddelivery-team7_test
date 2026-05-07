package com.team7.api.error;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TestValidationController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class GlobalExceptionHandlerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  void methodArgumentNotValidReturnsUnifiedErrorWithDetails() throws Exception {
    mockMvc.perform(post("/dummy")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(Map.of("name", ""))))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.error").value("Validation Error"))
        .andExpect(jsonPath("$.message").value("Request validation failed"))
        .andExpect(jsonPath("$.details.length()").value(1))
        .andExpect(jsonPath("$.details[0]").value("name: must not be blank"));
  }

  @Test
  void constraintViolationReturnsBadRequestWithDetails() throws Exception {
    mockMvc.perform(get("/dummy/query").param("value", "0"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.error").value("Validation Error"))
        .andExpect(jsonPath("$.message").value("Constraint violation"))
        .andExpect(jsonPath("$.details[0]").value("query.value: must be greater than or equal to 1"));
  }

  @Test
  void illegalArgumentReturnsBadRequestWithMessage() throws Exception {
    mockMvc.perform(get("/dummy/illegal"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").value("Bad Request"))
        .andExpect(jsonPath("$.message").value("bad"));
  }

  @Test
  void runtimeExceptionReturnsInternalServerError() throws Exception {
    mockMvc.perform(get("/dummy/runtime"))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.error").value("Internal Server Error"))
        .andExpect(jsonPath("$.message").value("boom"));
  }

  @Test
  void checkedExceptionReturnsInternalServerErrorViaHandleAny() throws Exception {
    mockMvc.perform(get("/dummy/checked-exception"))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.error").value("Internal Server Error"))
        .andExpect(jsonPath("$.message").value("boomAny"));
  }
}

