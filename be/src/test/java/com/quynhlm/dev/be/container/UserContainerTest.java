package com.quynhlm.dev.be.container;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quynhlm.dev.be.model.entity.User;
import com.quynhlm.dev.be.service.UserService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
@AutoConfigureMockMvc
public class UserContainerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    private User userRequest;

    @BeforeEach
    void initData() {
        userRequest = User.builder()
                .email("quynhlm.dev@gmail.com")
                .password("Quynh@123")
                .fullname("Lê Mạnh Quỳnh")
                .build();
    }

    @Test
    void createUser_validRequest_success() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        String content = objectMapper.writeValueAsString(userRequest);
        mockMvc.perform(MockMvcRequestBuilders
                .post("/onboarding/register")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(content))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Create a new account successfully"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(true));
    }

    // Test email
    @Test
    void createUser_validRequest_email_not_success() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        userRequest.setEmail("quynhlm.devsks.com");
        String content = objectMapper.writeValueAsString(userRequest);
        mockMvc.perform(MockMvcRequestBuilders
                .post("/onboarding/register")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(content))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Data is invalid."))
                .andExpect(MockMvcResultMatchers.jsonPath("$.errors[0].code").value("DATA_INVALID"))
                .andExpect(
                        MockMvcResultMatchers.jsonPath("$.errors[0].message").value("Email is not in correct format"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(false));
    }

    // Test password
    @Test
    void createUser_validRequest_password_not_success() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        userRequest.setPassword("sjssf");
        String content = objectMapper.writeValueAsString(userRequest);
        mockMvc.perform(MockMvcRequestBuilders
                .post("/onboarding/register")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(content))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Data is invalid."))
                .andExpect(MockMvcResultMatchers.jsonPath("$.errors[0].code").value("DATA_INVALID"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.errors[0].message")
                        .value("Incorrect password format . Please try other password"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(false));
    }
}
