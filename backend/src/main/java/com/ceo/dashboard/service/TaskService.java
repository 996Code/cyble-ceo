package com.ceo.dashboard.service;

import com.ceo.dashboard.entity.*;
import com.ceo.dashboard.enums.TaskStatus;
import com.ceo.dashboard.exception.*;
import com.ceo.dashboard.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class TaskService {

    private final TaskRepository taskRepository;
    private final TaskFlowRepository taskFlowRepository;
    private final SubTaskRepository subTaskRepository;
    
    public TaskService(TaskRepository taskRepository, 
                       TaskFlowRepository taskFlowRepository, 
                       SubTaskRepository subTaskRepository) {
        this.taskRepository = taskRepository;
        this.taskFlowRepository = taskFlowRepository;
        this.subTaskRepository = subTaskRepository;
    }

    // 定义合法的状态转换
    public static final Map<String, Set<String>> VALID_TRANSITIONS = Map.of(
        "CREATED",   Set.of("ASSIGNED", "CANCELLED"),
        "ASSIGNED",  Set.of("DOING", "BLOCKED", "CANCELLED"),
        "DOING",     Set.of("REVIEW", "BLOCKED", "CANCELLED", "DONE"),
        "REVIEW",    Set.of("DONE", "REJECTED", "CANCELLED"),
        "BLOCKED",   Set.of("DOING", "ASSIGNED", "CANCELLED"),
        "REJECTED",  Set.of("ASSIGNED", "CANCELLED"),
        "DONE",      Set.of(),
        "CANCELLED", Set.of()
    );

    /**
     * 创建新任务
     */
    public Task createTask(Task task) {
        return createTask(task, null);
    }
    
    /**
     * 创建新任务并设置初始状态
     */
    @Transactional
    public Task createTask(Task task, String initialStatus) {
        if (task.getId() == null || task.getId().isEmpty()) {
            // 生成任务ID，格式为 "T-YYYYMMDD-NNN"
            String dateStr = LocalDateTime.now().toString().substring(0, 10).replace("-", "");
            int count = (int) (taskRepository.count() % 1000);
            task.setId("T-" + dateStr + "-" + String.format("%03d", count));
        }
        
        if (task.getStatus() == null) {
            task.setStatus(TaskStatus.CREATED.name());
        }
        
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        
        Task savedTask = taskRepository.save(task);
        
        // 如果指定了初始状态，则进行状态流转
        if (initialStatus != null && !initialStatus.equals(task.getStatus())) {
            return updateTaskStatusWithValidPath(savedTask.getId(), initialStatus, "Task created with initial status: " + initialStatus);
        }
        
        return savedTask;
    }
    
    /**
     * 按照有效路径更新任务状态
     */
    @Transactional
    public Task updateTaskStatusWithValidPath(String taskId, String targetStatus, String remark) {
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new TaskNotFoundException(taskId));

        // 如果目标状态已经是当前状态，直接返回
        if (targetStatus.equals(task.getStatus())) {
            return task;
        }

        // 计算从当前状态到目标状态的有效路径
        List<String> path = calculateValidPath(task.getStatus(), targetStatus);
        
        Task currentTask = task;
        for (String status : path) {
            currentTask = updateTaskStatus(currentTask.getId(), status, remark);
        }
        
        return currentTask;
    }
    
    /**
     * 计算从起始状态到目标状态的有效路径
     */
    private List<String> calculateValidPath(String fromStatus, String toStatus) {
        // 如果起始状态和目标状态相同，不需要转换
        if (fromStatus.equals(toStatus)) {
            return new java.util.ArrayList<>();
        }
        
        // 特殊处理：如果可以直接转换，直接返回目标状态
        Set<String> validTransitions = VALID_TRANSITIONS.get(fromStatus);
        if (validTransitions != null && validTransitions.contains(toStatus)) {
            List<String> directPath = new java.util.ArrayList<>();
            directPath.add(toStatus);
            return directPath;
        }
        
        // 使用广度优先搜索找到最短路径
        // 从起始状态出发，寻找到达目标状态的路径
        java.util.Map<String, String> predecessors = new java.util.HashMap<>();
        java.util.Queue<String> queue = new java.util.LinkedList<>();
        java.util.Set<String> visited = new java.util.HashSet<>();
        
        queue.offer(fromStatus);
        visited.add(fromStatus);
        predecessors.put(fromStatus, null);
        
        while (!queue.isEmpty()) {
            String current = queue.poll();
            
            if (current.equals(toStatus)) {
                // 找到了路径，重构路径
                List<String> path = new java.util.ArrayList<>();
                String step = toStatus;
                while (step != null) {
                    if (!step.equals(fromStatus)) {
                        path.add(0, step); // 在列表开头插入
                    }
                    step = predecessors.get(step);
                }
                return path;
            }
            
            Set<String> nextStates = VALID_TRANSITIONS.get(current);
            if (nextStates != null) {
                for (String nextState : nextStates) {
                    if (!visited.contains(nextState)) {
                        visited.add(nextState);
                        queue.offer(nextState);
                        predecessors.put(nextState, current);
                    }
                }
            }
        }
        
        // 如果找不到路径，抛出异常
        throw new InvalidTaskTransitionException(fromStatus, toStatus);
    }

    /**
     * 获取任务列表（支持分页、时间范围过滤和归档状态）
     */
    public Page<Task> getTasks(String status, int page, int size, Integer days, Boolean includeArchived) {
        // 确保页码和大小有效
        if (page < 0) page = 0;
        if (size <= 0) size = 20;
        
        LocalDateTime cutoffDate = null;
        if (days != null) {
            cutoffDate = LocalDateTime.now().minusDays(days);
        }
        
        // 如果不包含归档任务，则只查询未归档的任务
        if (includeArchived == null || !includeArchived) {
            if (cutoffDate != null) {
                // 使用时间范围过滤
                List<Task> tasks = taskRepository.findByArchivedFalseAndStatusAndCreatedAtAfter(status, cutoffDate);
                int start = page * size;
                int end = Math.min(start + size, tasks.size());
                List<Task> paginatedTasks = start < tasks.size() ? tasks.subList(start, end) : List.of();
                
                return new PageImpl<>(paginatedTasks, PageRequest.of(page, size), tasks.size());
            } else {
                // 不使用时间范围过滤
                List<Task> tasks = taskRepository.findByArchivedAndStatus(false, status);
                int start = page * size;
                int end = Math.min(start + size, tasks.size());
                List<Task> paginatedTasks = start < tasks.size() ? tasks.subList(start, end) : List.of();
                
                return new PageImpl<>(paginatedTasks, PageRequest.of(page, size), tasks.size());
            }
        } else {
            // 包含归档任务
            List<Task> allTasks = taskRepository.findAllOrderByUpdatedAtDesc();
            
            // 应用状态过滤
            String finalStatus = status; // 创建final变量供lambda使用
            LocalDateTime finalCutoffDate = cutoffDate; // 创建final变量供lambda使用
            List<Task> filteredTasks = allTasks.stream()
                .filter(t -> finalStatus == null || t.getStatus().equals(finalStatus))
                .filter(t -> finalCutoffDate == null || t.getCreatedAt().isAfter(finalCutoffDate) || t.getCreatedAt().isEqual(finalCutoffDate))
                .collect(Collectors.toList());
            
            int start = page * size;
            int end = Math.min(start + size, filteredTasks.size());
            List<Task> paginatedTasks = start < filteredTasks.size() ? filteredTasks.subList(start, end) : List.of();
            
            return new PageImpl<>(paginatedTasks, PageRequest.of(page, size), filteredTasks.size());
        }
    }

    /**
     * 获取任务列表（兼容旧版本，返回全部任务）
     */
    public List<Task> getAllTasks(String status) {
        if (status != null && !status.isEmpty()) {
            return taskRepository.findByStatus(status);
        }
        return taskRepository.findAll();
    }



    /**
     * 获取任务详情（包括流转记录和子任务）
     */
    public Task getTaskById(String taskId) {
        return taskRepository.findById(taskId)
            .orElseThrow(() -> new TaskNotFoundException(taskId));
    }

    /**
     * 更新任务状态（带状态机校验）
     */
    public Task updateTaskStatus(String taskId, String newStatus, String remark) {
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new TaskNotFoundException(taskId));

        // 检查状态转换是否合法
        if (!isValidTransition(task.getStatus(), newStatus)) {
            throw new InvalidTaskTransitionException(task.getStatus(), newStatus);
        }

        // 记录流转
        TaskFlow flow = new TaskFlow();
        flow.setTaskId(taskId);
        flow.setFromStatus(task.getStatus());
        flow.setToStatus(newStatus);
        flow.setRemark(remark);
        flow.setCreatedAt(LocalDateTime.now());
        
        // 设置时间戳
        if (newStatus.equals(TaskStatus.DOING.name()) && task.getStartedAt() == null) {
            task.setStartedAt(LocalDateTime.now());
        }
        if (newStatus.equals(TaskStatus.DONE.name()) && task.getCompletedAt() == null) {
            task.setCompletedAt(LocalDateTime.now());
        }

        task.setStatus(newStatus);
        task.setUpdatedAt(LocalDateTime.now());

        Task savedTask = taskRepository.save(task);
        taskFlowRepository.save(flow);

        return savedTask;
    }

    /**
     * 更新任务进展
     */
    public Task updateTaskProgress(String taskId, String currentProgress, Integer progressPercent) {
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new TaskNotFoundException(taskId));

        task.setCurrentProgress(currentProgress);
        if (progressPercent != null) {
            task.setProgressPercent(progressPercent);
        }
        task.setUpdatedAt(LocalDateTime.now());

        return taskRepository.save(task);
    }

    /**
     * 添加流转记录
     */
    public TaskFlow addTaskFlow(String taskId, String fromAgent, String toAgent, String remark) {
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new TaskNotFoundException(taskId));

        TaskFlow flow = new TaskFlow();
        flow.setTaskId(taskId);
        flow.setFromAgent(fromAgent);
        flow.setToAgent(toAgent);
        flow.setFromStatus(task.getStatus());  // 当前状态作为from状态
        flow.setToStatus(task.getStatus());    // to状态保持不变（不改变任务状态）
        flow.setRemark(remark);
        flow.setCreatedAt(LocalDateTime.now());

        return taskFlowRepository.save(flow);
    }

    /**
     * 完成任务
     */
    public Task completeTask(String taskId, String summary) {
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new TaskNotFoundException(taskId));

        if (!isValidTransition(task.getStatus(), TaskStatus.DONE.name())) {
            throw new InvalidTaskStateException("Cannot complete task from current status: " + task.getStatus());
        }

        task.setStatus(TaskStatus.DONE.name());
        task.setSummary(summary);
        task.setCompletedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());

        // 记录流转
        TaskFlow flow = new TaskFlow();
        flow.setTaskId(taskId);
        flow.setFromStatus(task.getStatus());
        flow.setToStatus(TaskStatus.DONE.name());
        flow.setRemark("Task completed");
        flow.setCreatedAt(LocalDateTime.now());

        Task savedTask = taskRepository.save(task);
        taskFlowRepository.save(flow);

        return savedTask;
    }

    /**
     * 标记任务为阻塞
     */
    public Task blockTask(String taskId, String reason) {
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new TaskNotFoundException(taskId));

        if (!isValidTransition(task.getStatus(), TaskStatus.BLOCKED.name())) {
            throw new InvalidTaskStateException("Cannot block task from current status: " + task.getStatus());
        }

        task.setStatus(TaskStatus.BLOCKED.name());
        task.setCurrentProgress(reason);
        task.setUpdatedAt(LocalDateTime.now());

        // 记录流转
        TaskFlow flow = new TaskFlow();
        flow.setTaskId(taskId);
        flow.setFromStatus(task.getStatus());
        flow.setToStatus(TaskStatus.BLOCKED.name());
        flow.setRemark("Task blocked: " + reason);
        flow.setCreatedAt(LocalDateTime.now());

        Task savedTask = taskRepository.save(task);
        taskFlowRepository.save(flow);

        return savedTask;
    }

    /**
     * 添加子任务
     */
    public SubTask addSubTask(String taskId, SubTask subTask) {
        // 验证父任务是否存在
        taskRepository.findById(taskId)
            .orElseThrow(() -> new TaskNotFoundException(taskId));

        subTask.setTaskId(taskId);
        subTask.setUpdatedAt(LocalDateTime.now());

        return subTaskRepository.save(subTask);
    }

    /**
     * 更新子任务
     */
    public SubTask updateSubTask(String taskId, Integer seq, SubTask subTask) {
        // 查找现有的子任务
        java.util.List<SubTask> existingSubTasks = subTaskRepository.findByTaskIdAndSeq(taskId, seq);
        if (existingSubTasks.isEmpty()) {
            throw new RuntimeException("SubTask not found: taskId=" + taskId + ", seq=" + seq);
        }

        SubTask existingSubTask = existingSubTasks.get(0);
        // 只更新允许更新的字段
        if (subTask.getTitle() != null) {
            existingSubTask.setTitle(subTask.getTitle());
        }
        if (subTask.getStatus() != null) {
            existingSubTask.setStatus(subTask.getStatus());
        }
        if (subTask.getDetail() != null) {
            existingSubTask.setDetail(subTask.getDetail());
        }
        if (subTask.getAssignee() != null) {
            existingSubTask.setAssignee(subTask.getAssignee());
        }
        existingSubTask.setUpdatedAt(LocalDateTime.now());

        return subTaskRepository.save(existingSubTask);
    }

    /**
     * 获取指定任务的流转记录
     */
    public java.util.List<TaskFlow> getTaskFlowsByTaskId(String taskId) {
        return taskFlowRepository.findByTaskId(taskId);
    }

    /**
     * 获取指定任务的子任务
     */
    public java.util.List<SubTask> getSubTasksByTaskId(String taskId) {
        return subTaskRepository.findByTaskId(taskId);
    }

    /**
     * 归档任务 - 根据条件归档已完成或取消的任务
     */
    @Transactional
    public int archiveTasks(Integer beforeDays, List<String> taskIds) {
        List<Task> tasksToArchive = new java.util.ArrayList<>();
        
        if (beforeDays != null) {
            // 归档N天前的已完成或已取消任务
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(beforeDays);
            tasksToArchive = taskRepository.findAll().stream()
                .filter(task -> task.getArchived() == null || !task.getArchived())
                .filter(task -> "DONE".equals(task.getStatus()) || "CANCELLED".equals(task.getStatus()))
                .filter(task -> task.getUpdatedAt() != null && task.getUpdatedAt().isBefore(cutoffDate))
                .collect(java.util.stream.Collectors.toList());
        } else if (taskIds != null && !taskIds.isEmpty()) {
            // 归档指定ID的任务
            tasksToArchive = taskRepository.findAllById(taskIds).stream()
                .filter(task -> "DONE".equals(task.getStatus()) || "CANCELLED".equals(task.getStatus()))
                .collect(java.util.stream.Collectors.toList());
        }
        
        // 批量更新归档状态
        for (Task task : tasksToArchive) {
            task.setArchived(true);
            task.setUpdatedAt(LocalDateTime.now());
        }
        
        List<Task> savedTasks = taskRepository.saveAll(tasksToArchive);
        return savedTasks.size();
    }

    /**
     * 清空所有任务数据（仅用于开发/测试环境）
     */
    @Transactional
    public void clearAllTasks() {
        // 检查是否在开发环境
        String env = System.getenv("ENVIRONMENT");
        if (env == null || !env.equals("production")) {
            // 先删除相关的子任务和流转记录，避免外键约束
            subTaskRepository.deleteAll();
            taskFlowRepository.deleteAll();
            // 然后删除所有任务
            taskRepository.deleteAll();
        } else {
            throw new IllegalStateException("清空任务数据功能不能在生产环境中使用！");
        }
    }

    /**
     * 验证状态转换是否合法
     */
    private boolean isValidTransition(String fromStatus, String toStatus) {
        Set<String> validTransitions = VALID_TRANSITIONS.get(fromStatus);
        if (validTransitions == null) {
            return false;
        }
        return validTransitions.contains(toStatus);
    }
}