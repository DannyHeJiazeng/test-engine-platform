package com.test.annotation;

/**
 * 测试类型枚举
 */
public enum TestType {

    FUNCTIONAL("functional", "功能测试"),
    PERFORMANCE("performance", "性能测试"),
    INSPECTION("inspection", "巡检测试"),
    UNKNOWN("unknown", "未知类型");

    private final String groupName;
    private final String description;

    TestType(String groupName, String description) {
        this.groupName = groupName;
        this.description = description;
    }

    public String getGroupName() {
        return groupName;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 根据 group 名称获取测试类型
     */
    public static TestType fromGroupName(String groupName) {
        for (TestType type : values()) {
            if (type.getGroupName().equals(groupName)) {
                return type;
            }
        }
        return UNKNOWN;
    }
}
