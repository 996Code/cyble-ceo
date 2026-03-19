package com.ceo.dashboard.controller;

import com.ceo.dashboard.exception.InvalidTaskTransitionException;
import com.ceo.dashboard.service.TaskService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class TaskControllerTest {

    @Mock
    private TaskService taskService;

    @InjectMocks
    private TaskController taskController;

    private MockMvc mockMvc;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(taskController).build();
    }

    @Test
    public void testUpdateStateInvalidTransitionReturns400() throws Exception {
        // 模拟服务抛出InvalidTaskTransitionException
        when(taskService.updateTaskStatus(eq("task123"), eq("DOING"), any(String.class)))
                .thenThrow(new InvalidTaskTransitionException("DONE", "DOING"));

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("status", "DOING");
        requestBody.put("remark", "");

        mockMvc.perform(put("/api/v1/tasks/task123/state")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest()) // 应该返回400，而不是500
                .andExpect(jsonPath("$.code").value(400)); // 确保错误代码是400
    }
}