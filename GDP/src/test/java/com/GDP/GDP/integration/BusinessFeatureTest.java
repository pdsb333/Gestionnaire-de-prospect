package com.GDP.GDP.integration;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.GDP.GDP.dto.business.BusinessRequest;
import com.GDP.GDP.entity.User;
import com.GDP.GDP.entity.User.Role;
import com.GDP.GDP.repository.BusinessRepository;
import com.GDP.GDP.repository.UserRepository;
import com.GDP.GDP.security.CustomUserDetails;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;


@DisplayName("Business feature integration test")
@SpringBootTest
@AutoConfigureMockMvc
public class BusinessFeatureTest extends AbstractIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BusinessRepository businessRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private User savedUser;

    @BeforeEach
    void setUp(){
        jdbcTemplate.execute("TRUNCATE TABLE businesses, users RESTART IDENTITY CASCADE;");
        savedUser = userRepository.save(
            new User("user", "user@mail.com", "password", Role.ROLE_USER)
        );
    }
    //###########################################################
    // UTILITY
    //###########################################################
    private CustomUserDetails createUser(){
        return new CustomUserDetails(savedUser);   
    }
    private BusinessRequest validRequest(){
        BusinessRequest request = new BusinessRequest("A", "A_description", "A_serviceContact");
        return request;
    }

    private BusinessRequest wrongRequest(){
        BusinessRequest request = new BusinessRequest("", "A_description", "A_serviceContact");
        return request;
    }    

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize object", e);
        }
    }

    private MockHttpServletRequestBuilder buildPost(BusinessRequest request, CustomUserDetails user) {
        return post("/api/business")
                .with(user(user))
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(request));
    }


    //###########################################################
    // CREATE BUSINESS
    //###########################################################
    @Nested
    @DisplayName("Create business")
    class CreateBusinessTest {

        @Test
        @DisplayName("Should create and return a business for current user")
        void createBusiness_shouldPersistAndReturnCorrectResponse_withAuthenticatedUser() throws Exception{

            mockMvc.perform(buildPost(validRequest(), createUser()))
                .andExpect(status().isCreated()) 
                .andExpect(jsonPath("$.name").value("A"))
                .andExpect(jsonPath("$.recruitmentServiceContact").value("A_serviceContact"))
                .andExpect(jsonPath("$.description").value("A_description"))
                .andExpect(header().string("Location", "/api/business"));
            
            var all = businessRepository.findAll();
            assertThat(all).hasSize(1);

            var saved = all.get(0);
            assertThat(saved.getUser().getId()).isEqualTo(savedUser.getId());

        }

        @Test
        @DisplayName("Should return 400 when name is blank")
        void createBusiness_shouldReturnBadRequest_whenNameIsBlank() throws Exception {
           mockMvc.perform(buildPost(wrongRequest(), createUser()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                    .andExpect(jsonPath("$.message").value("Validation failed"))
                    .andExpect(jsonPath("$.path").value("/api/business"))
                    .andExpect(jsonPath("$.validationErrors.name[0]").value("Name is required"));

            assertThat(businessRepository.findAll()).isEmpty();
        }        
    }

    //###########################################################
    // GET BUSINESS
    //###########################################################
    @Nested
    @DisplayName("Get business")
    class GetBusinessTest {
        
    }

    //###########################################################
    // UPDATE BUSINESS
    //###########################################################
    @Nested
    @DisplayName("Update business")
    class UpdateBusinessTest {
    
        
    }

    //###########################################################
    // DELETE BUSINESS
    //###########################################################
    @Nested
    @DisplayName("Delete business")
    class DeleteBusinessTest {
    
        
    }
}
