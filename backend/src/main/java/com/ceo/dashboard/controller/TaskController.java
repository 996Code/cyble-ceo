package com.ceo.dashboard.controller;

import com.ceo.dashboard.entity.*;
import com.ceo.dashboard.service.TaskService;
import com.ceo.dashboard.util.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/tasks")
public class TaskController {

    @Autowired
    private TaskService taskService;

    /**
     * 创建任务
     */
    @PostMapping
    public ApiResponse<?> createTask(@RequestBody Map<String, Object> request) {
        try {
            Task task = new Task();
            task.setId((String) request.get("id"));
            task.setTitle((String) request.get("title"));
            task.setDescription((String) request.get("description"));
            task.setAssignee((String) request.get("assignee"));
            task.setCreator((String) request.getOrDefault("creator", "ceo"));
            task.setSessionKey((String) request.get("sessionKey")); // 新增sessionKey字段
            
            // 获取初始状态
            String initialStatus = (String) request.get("initialStatus");
            
            Task saved = taskService.createTask(task, initialStatus);
            return ApiResponse.success(saved);
        } catch (Exception e) {
            return ApiResponse.error("创建任务失败: " + e.getMessage());
        }
    }

    /**
     * 查询任务列表（支持分页、时间范围过滤和归档状态）
     */
    @GetMapping
    public ApiResponse<?> getTasks(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Integer days,
            @RequestParam(defaultValue = "false") Boolean includeArchived) {
        try {
            // 向后兼容：如果没传分页参数，返回全部任务
            if (page == 0 && size == 20 && days == null && !includeArchived) {
                List<Task> tasks = taskService.getAllTasks(status);
                return ApiResponse.success(tasks);
            } else {
                // 使用分页和过滤
                org.springframework.data.domain.Page<Task> pagedTasks = taskService.getTasks(status, page, size, days, includeArchived);
                
                Map<String, Object> result = new HashMap<>();
                result.put("content", pagedTasks.getContent());
                result.put("totalElements", pagedTasks.getTotalElements());
                result.put("totalPages", pagedTasks.getTotalPages());
                result.put("currentPage", pagedTasks.getNumber());
                result.put("size", pagedTasks.getSize());
                
                return ApiResponse.success(result);
            }
        } catch (Exception e) {
            return ApiResponse.error("查询任务失败: " + e.getMessage());
        }
    }

    /**
     * 任务详情（含流转+子任务）
     */
    @GetMapping("/{id}")
    public ApiResponse<?> getTaskDetail(@PathVariable String id) {
        try {
            Task task = taskService.getTaskById(id);
            List<TaskFlow> flows = taskService.getTaskFlowsByTaskId(id);
            List<SubTask> subTasks = taskService.getSubTasksByTaskId(id);
            
            Map<String, Object> result = new HashMap<>();
            result.put("task", task);
            result.put("flows", flows);
            result.put("subTasks", subTasks);
            
            return ApiResponse.success(result);
        } catch (Exception e) {
            return ApiResponse.error("查询任务详情失败: " + e.getMessage());
        }
    }

    /**
     * 更新状态（带状态机校验）
     */
    @PutMapping("/{id}/state")
    public ApiResponse<?> updateState(@PathVariable String id, @RequestBody Map<String, String> request) {
        try {
            String status = request.get("status");
            String remark = request.getOrDefault("remark", "");
            Task updated = taskService.updateTaskStatus(id, status, remark);
            return ApiResponse.success(updated);
        } catch (Exception e) {
            // 异常会被GlobalExceptionHandler处理
            throw e;
        }
    }

    /**
     * 更新进展（不改变状态）
     */
    @PutMapping("/{id}/progress")
    public ApiResponse<?> updateProgress(@PathVariable String id, @RequestBody Map<String, Object> request) {
        try {
            String currentProgress = (String) request.get("currentProgress");
            Integer progressPercent = request.get("progressPercent") != null ? 
                ((Number) request.get("progressPercent")).intValue() : null;
            Task updated = taskService.updateTaskProgress(id, currentProgress, progressPercent);
            return ApiResponse.success(updated);
        } catch (Exception e) {
            return ApiResponse.error("更新进展失败: " + e.getMessage());
        }
    }

    /**
     * 添加流转记录
     */
    @PostMapping("/{id}/flow")
    public ApiResponse<?> addFlow(@PathVariable String id, @RequestBody Map<String, String> request) {
        try {
            TaskFlow flow = taskService.addTaskFlow(id, 
                request.get("fromAgent"), 
                request.get("toAgent"), 
                request.getOrDefault("remark", ""));
            return ApiResponse.success(flow);
        } catch (Exception e) {
            return ApiResponse.error("添加流转记录失败: " + e.getMessage());
        }
    }

    /**
     * 完成任务
     */
    @PutMapping("/{id}/done")
    public ApiResponse<?> done(@PathVariable String id, @RequestBody Map<String, String> request) {
        try {
            Task completed = taskService.completeTask(id, 
                request.getOrDefault("summary", ""));
            return ApiResponse.success(completed);
        } catch (Exception e) {
            return ApiResponse.error("完成任务失败: " + e.getMessage());
        }
    }

    /**
     * 标记阻塞
     */
    @PutMapping("/{id}/block")
    public ApiResponse<?> block(@PathVariable String id, @RequestBody Map<String, String> request) {
        try {
            Task blocked = taskService.blockTask(id, request.get("reason"));
            return ApiResponse.success(blocked);
        } catch (Exception e) {
            return ApiResponse.error("标记阻塞失败: " + e.getMessage());
        }
    }

    /**
     * 添加子任务
     */
    @PostMapping("/{id}/subtasks")
    public ApiResponse<?> addSubTask(@PathVariable String id, @RequestBody SubTask subTask) {
        try {
            subTask.setTaskId(id);
            SubTask created = taskService.addSubTask(id, subTask);
            return ApiResponse.success(created);
        } catch (Exception e) {
            return ApiResponse.error("添加子任务失败: " + e.getMessage());
        }
    }

    /**
     * 更新子任务
     */
    @PutMapping("/{id}/subtasks/{seq}")
    public ApiResponse<?> updateSubTask(@PathVariable String id, @PathVariable Integer seq, 
                                        @RequestBody SubTask subTask) {
        try {
            SubTask updated = taskService.updateSubTask(id, seq, subTask);
            return ApiResponse.success(updated);
        } catch (Exception e) {
            return ApiResponse.error("更新子任务失败: " + e.getMessage());
        }
    }

    /**
     * 归档任务
     */
    @PostMapping("/archive")
    public ApiResponse<?> archiveTasks(@RequestBody Map<String, Object> request) {
        try {
            Integer beforeDays = request.get("beforeDays") != null ? 
                ((Number) request.get("beforeDays")).intValue() : null;
            List<String> taskIds = (List<String>) request.get("taskIds");
            
            int archivedCount = taskService.archiveTasks(beforeDays, taskIds);
            Map<String, Object> result = new HashMap<>();
            result.put("archivedCount", archivedCount);
            
            return ApiResponse.success(result);
        } catch (Exception e) {
            return ApiResponse.error("归档任务失败: " + e.getMessage());
        }
    }

    /**
     * 清空所有任务数据（仅用于开发/测试环境）
     */
    @DeleteMapping("/clear-all")
    public ApiResponse<?> clearAllTasks() {
        try {
            taskService.clearAllTasks();
            return ApiResponse.success("所有任务数据已清空");
        } catch (Exception e) {
            return ApiResponse.error("清空任务数据失败: " + e.getMessage());
        }
    }
}
