package com.ceo.dashboard.service;

import com.ceo.dashboard.entity.Task;
import com.ceo.dashboard.enums.TaskStatus;
import com.ceo.dashboard.repository.TaskRepository;
import com.ceo.dashboard.repository.TaskFlowRepository;
import com.ceo.dashboard.repository.SubTaskRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class TaskServiceIntegrationTest {

    @Autowired
    private TaskService taskService;

    @Autowired
    private TaskRepository taskRepository;

    @Test
    void testCreateTaskWithInitialStatus() {
        // 创建任务并设置初始状态为DOING
        Task task = new Task();
        task.setTitle("Test Task With Initial Status");
        task.setDescription("Testing initial status functionality");
        task.setCreator("test-user");

        Task createdTask = taskService.createTask(task, "DOING");

        assertNotNull(createdTask);
        assertEquals("DOING", createdTask.getStatus());
        assertNotNull(createdTask.getId());
        assertNotNull(createdTask.getStartedAt());
    }

    @Test
    void testGetTasksWithPagination() {
        // 创建多个测试任务
        for (int i = 0; i < 25; i++) {
            Task task = new Task();
            task.setTitle("Test Task " + i);
            task.setDescription("Description for task " + i);
            task.setCreator("test-user");
            task.setStatus(TaskStatus.CREATED.name());
            task.setCreatedAt(LocalDateTime.now().minusDays(i));
            taskRepository.save(task);
        }

        // 测试分页功能
        Page<Task> page1 = taskService.getTasks(null, 0, 10, null, false);
        Page<Task> page2 = taskService.getTasks(null, 1, 10, null, false);

        assertEquals(10, page1.getSize());
        assertEquals(10, page2.getSize());
        assertTrue(page1.getTotalElements() >= 25);
        assertEquals(3, page1.getTotalPages()); // ceil(25/10) = 3
    }

    @Test
    void testGetTasksWithTimeRange() {
        // 创建不同时间的任务
        Task recentTask = new Task();
        recentTask.setTitle("Recent Task");
        recentTask.setCreator("test-user");
        recentTask.setStatus(TaskStatus.CREATED.name());
        recentTask.setCreatedAt(LocalDateTime.now().minusDays(2));
        taskRepository.save(recentTask);

        Task oldTask = new Task();
        oldTask.setTitle("Old Task");
        oldTask.setCreator("test-user");
        oldTask.setStatus(TaskStatus.CREATED.name());
        oldTask.setCreatedAt(LocalDateTime.now().minusDays(10));
        taskRepository.save(oldTask);

        // 测试只获取最近7天的任务
        Page<Task> result = taskService.getTasks(null, 0, 10, 7, false);

        assertEquals(1, result.getTotalElements()); // 只应该有最近的任务
        assertEquals("Recent Task", result.getContent().get(0).getTitle());
    }

    @Test
    void testArchiveTasksByIds() {
        // 创建一些已完成的任务
        Task task1 = new Task();
        task1.setTitle("Completed Task 1");
        task1.setStatus("DONE");
        task1.setCreator("test-user");
        task1 = taskRepository.save(task1);

        Task task2 = new Task();
        task2.setTitle("Cancelled Task 2");
        task2.setStatus("CANCELLED");
        task2.setCreator("test-user");
        task2 = taskRepository.save(task2);

        Task task3 = new Task();
        task3.setTitle("Active Task 3");
        task3.setStatus("DOING");
        task3.setCreator("test-user");
        task3 = taskRepository.save(task3);

        // 归档指定ID的任务
        int archivedCount = taskService.archiveTasks(null, List.of(task1.getId(), task2.getId(), task3.getId()));

        assertEquals(2, archivedCount); // 只有DONE和CANCELLED状态的任务被归档
        
        Task archivedTask1 = taskRepository.findById(task1.getId()).orElse(null);
        Task archivedTask2 = taskRepository.findById(task2.getId()).orElse(null);
        Task activeTask3 = taskRepository.findById(task3.getId()).orElse(null);

        assertNotNull(archivedTask1);
        assertNotNull(archivedTask2);
        assertNotNull(activeTask3);

        assertTrue(archivedTask1.getArchived());
        assertTrue(archivedTask2.getArchived());
        assertFalse(activeTask3.getArchived()); // DOING状态的任务不应该被归档
    }

    @Test
    void testArchiveTasksByDays() {
        // 创建一个已完成且更新时间较早的任务
        Task oldTask = new Task();
        oldTask.setTitle("Old Completed Task");
        oldTask.setStatus("DONE");
        oldTask.setCreator("test-user");
        oldTask.setCreatedAt(LocalDateTime.now().minusDays(15));
        oldTask.setUpdatedAt(LocalDateTime.now().minusDays(15));
        oldTask = taskRepository.save(oldTask);

        // 创建一个较新的已完成任务
        Task newTask = new Task();
        newTask.setTitle("New Completed Task");
        newTask.setStatus("DONE");
        newTask.setCreator("test-user");
        newTask.setCreatedAt(LocalDateTime.now().minusDays(2));
        newTask.setUpdatedAt(LocalDateTime.now().minusDays(2));
        newTask = taskRepository.save(newTask);

        // 归档7天前的已完成任务
        int archivedCount = taskService.archiveTasks(7, null);

        assertEquals(1, archivedCount); // 只有旧任务被归档

        Task resultOldTask = taskRepository.findById(oldTask.getId()).orElse(null);
        Task resultNewTask = taskRepository.findById(newTask.getId()).orElse(null);

        assertTrue(resultOldTask.getArchived());
        assertNull(resultNewTask.getArchived()); // 新任务不应被归档
    }

    @Test
    void testGetTasksIncludeArchived() {
        // 创建一个已归档的任务
        Task archivedTask = new Task();
        archivedTask.setTitle("Archived Task");
        archivedTask.setStatus("DONE");
        archivedTask.setCreator("test-user");
        archivedTask.setArchived(true);
        taskRepository.save(archivedTask);

        // 创建一个未归档的任务
        Task activeTask = new Task();
        activeTask.setTitle("Active Task");
        activeTask.setStatus("DOING");
        activeTask.setCreator("test-user");
        activeTask.setArchived(false);
        taskRepository.save(activeTask);

        // 测试默认不返回归档任务
        Page<Task> defaultResult = taskService.getTasks(null, 0, 10, null, false);
        assertEquals(1, defaultResult.getTotalElements()); // 只有未归档的任务

        // 测试包含归档任务
        Page<Task> includeArchivedResult = taskService.getTasks(null, 0, 10, null, true);
        assertEquals(2, includeArchivedResult.getTotalElements()); // 包括归档和未归档的任务
    }
}