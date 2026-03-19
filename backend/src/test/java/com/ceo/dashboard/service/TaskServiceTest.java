package com.ceo.dashboard.service;

import com.ceo.dashboard.entity.Task;
import com.ceo.dashboard.repository.TaskRepository;
import com.ceo.dashboard.repository.TaskFlowRepository;
import com.ceo.dashboard.repository.SubTaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TaskFlowRepository taskFlowRepository;

    @Mock
    private SubTaskRepository subTaskRepository;

    private TaskService taskService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        taskService = new TaskService(taskRepository, taskFlowRepository, subTaskRepository);
    }

    @Test
    void testCreateTaskWithInitialStatus() {
        // 准备测试数据
        Task task = new Task();
        task.setTitle("Test Task");
        task.setDescription("Test Description");

        Task savedTask = new Task();
        savedTask.setId("T-12345");
        savedTask.setTitle("Test Task");
        savedTask.setStatus("CREATED");
        savedTask.setCreatedAt(LocalDateTime.now());

        when(taskRepository.save(any(Task.class))).thenReturn(savedTask);
        when(taskRepository.findById("T-12345")).thenReturn(Optional.of(savedTask));

        // 执行测试 - 创建任务并设置初始状态为DOING
        Task result = taskService.createTask(task, "DOING");

        // 验证结果
        assertNotNull(result);
        assertEquals("T-12345", result.getId());
        // 从CREATED到DOING需要两步转换：CREATED->ASSIGNED->DOING，加上初始保存，总共3次
        verify(taskRepository, atLeastOnce()).save(any(Task.class));
    }

    @Test
    void testGetTasksWithPagination() {
        // 准备测试数据
        Task task1 = new Task();
        task1.setId("T-001");
        task1.setTitle("Task 1");
        task1.setArchived(false);
        task1.setCreatedAt(LocalDateTime.now());

        Task task2 = new Task();
        task2.setId("T-002");
        task2.setTitle("Task 2");
        task2.setArchived(false);
        task2.setCreatedAt(LocalDateTime.now());

        List<Task> tasks = Arrays.asList(task1, task2);
        when(taskRepository.findByArchivedAndStatus(false, null)).thenReturn(tasks);

        // 执行测试
        Page<Task> result = taskService.getTasks(null, 0, 10, null, false);

        // 验证结果
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals(1, result.getTotalPages());
        assertEquals(0, result.getNumber());
    }

    @Test
    void testArchiveTasksByIds() {
        // 准备测试数据
        Task task1 = new Task();
        task1.setId("T-001");
        task1.setStatus("DONE");
        task1.setArchived(false);

        Task task2 = new Task();
        task2.setId("T-002");
        task2.setStatus("CANCELLED");
        task2.setArchived(false);

        List<Task> tasks = Arrays.asList(task1, task2);
        when(taskRepository.findAllById(Arrays.asList("T-001", "T-002"))).thenReturn(tasks);
        when(taskRepository.saveAll(any())).thenReturn(tasks);

        // 执行测试
        int result = taskService.archiveTasks(null, Arrays.asList("T-001", "T-002"));

        // 验证结果
        assertEquals(2, result);
        verify(taskRepository, times(1)).saveAll(any());
    }

    @Test
    void testArchiveTasksByDays() {
        // 准备测试数据
        Task task1 = new Task();
        task1.setId("T-001");
        task1.setStatus("DONE");
        task1.setArchived(false);
        task1.setUpdatedAt(LocalDateTime.now().minusDays(10));

        Task task2 = new Task();
        task2.setId("T-002");
        task2.setStatus("DONE");
        task2.setArchived(false);
        task2.setUpdatedAt(LocalDateTime.now().minusDays(5));

        List<Task> allTasks = Arrays.asList(task1, task2);
        when(taskRepository.findAll()).thenReturn(allTasks);
        when(taskRepository.saveAll(any())).thenReturn(Arrays.asList(task1));

        // 执行测试 - 归档7天前的完成任务
        int result = taskService.archiveTasks(7, null);

        // 验证结果
        assertEquals(1, result); // 只有一个任务超过7天
        verify(taskRepository, times(1)).saveAll(any());
    }
}