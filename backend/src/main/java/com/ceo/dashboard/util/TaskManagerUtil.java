package com.ceo.dashboard.util;

import com.ceo.dashboard.enums.TaskStatus;
import com.ceo.dashboard.enums.SubTaskStatus;

/**
 * 任务管理工具类
 * 用于辅助CLI工具进行任务管理
 */
public class TaskManagerUtil {
    
    /**
     * 验证任务状态是否有效
     */
    public static boolean isValidTaskStatus(String status) {
        try {
            TaskStatus.valueOf(status.toUpperCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    
    /**
     * 验证子任务状态是否有效
     */
    public static boolean isValidSubTaskStatus(String status) {
        try {
            SubTaskStatus.fromValue(status);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    
    /**
     * 生成任务ID
     */
    public static String generateTaskId() {
        String dateStr = java.time.LocalDate.now().toString().replace("-", "");
        // 在实际应用中，这里应该使用数据库序列或其他方式确保唯一性
        int randomNum = (int)(Math.random() * 1000);
        return "T-" + dateStr + "-" + String.format("%03d", randomNum);
    }
}