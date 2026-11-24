package com.mental.util;

public class MentalLevelUtil {
    // 通用评分方法
    public static String[] getLevelAndRisk(int totalScore) {
        if (totalScore <= 30) return new String[]{"心理健康", "无风险"};
        if (totalScore <= 50) return new String[]{"基本健康", "轻微压力，建议自我调节"};
        if (totalScore <= 70) return new String[]{"轻度心理风险", "建议关注情绪，适当放松"};
        if (totalScore <= 90) return new String[]{"中度心理风险", "建议预约心理咨询"};
        return new String[]{"重度心理风险", "请立即联系辅导员或心理咨询师"};
    }

    // SAS焦虑自评量表评分
    public static String[] getSASLevelAndRisk(int totalScore) {
        double standardScore = totalScore * 1.25;
        if (standardScore < 50) return new String[]{"正常", "无焦虑"};
        if (standardScore < 60) return new String[]{"轻度焦虑", "建议自我调节"};
        if (standardScore < 70) return new String[]{"中度焦虑", "建议心理咨询"};
        return new String[]{"重度焦虑", "请立即寻求专业帮助"};
    }

    // SDS抑郁自评量表评分
    public static String[] getSDSLevelAndRisk(int totalScore) {
        double standardScore = totalScore * 1.25;
        if (standardScore < 50) return new String[]{"正常", "无抑郁"};
        if (standardScore < 60) return new String[]{"轻度抑郁", "建议自我调节"};
        if (standardScore < 70) return new String[]{"中度抑郁", "建议心理咨询"};
        return new String[]{"重度抑郁", "请立即寻求专业帮助"};
    }

    // 人际关系综合诊断评分
    public static String[] getInterpersonalLevelAndRisk(int totalScore) {
        if (totalScore <= 8) return new String[]{"人际关系良好", "无困扰"};
        if (totalScore <= 16) return new String[]{"人际关系一般", "轻微困扰"};
        if (totalScore <= 24) return new String[]{"人际关系较差", "中度困扰"};
        return new String[]{"人际关系严重困扰", "建议专业咨询"};
    }

    // 艾森克人格问卷(EPQ)评分
    public static String[] getEPQLevelAndRisk(int totalScore) {
        if (totalScore <= 20) return new String[]{"人格特征正常", "无风险"};
        if (totalScore <= 35) return new String[]{"人格特征轻微偏离", "建议关注"};
        if (totalScore <= 50) return new String[]{"人格特征中度偏离", "建议咨询"};
        return new String[]{"人格特征显著偏离", "建议专业评估"};
    }

    // 压力应对方式问卷评分
    public static String[] getStressCopingLevelAndRisk(int totalScore) {
        if (totalScore <= 20) return new String[]{"应对方式积极", "无风险"};
        if (totalScore <= 35) return new String[]{"应对方式一般", "建议调整"};
        if (totalScore <= 50) return new String[]{"应对方式消极", "建议学习"};
        return new String[]{"应对方式不良", "建议专业指导"};
    }
}