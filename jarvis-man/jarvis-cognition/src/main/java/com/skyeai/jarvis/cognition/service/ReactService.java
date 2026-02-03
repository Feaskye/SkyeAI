package com.skyeai.jarvis.cognition.service;

import java.util.List;
import java.util.Map;

public interface ReactService {

    /**
     * 执行ReAct决策过程
     */
    ReactResult executeReact(String goal, List<String> observations);

    /**
     * 执行ReAct决策过程（带上下文）
     */
    ReactResult executeReact(String goal, List<String> observations, Map<String, Object> context);

    /**
     * 执行单步ReAct决策
     */
    ReactStepResult executeReactStep(String goal, List<String> observations, int stepNumber, Map<String, Object> context);

    /**
     * 验证ReAct决策结果
     */
    boolean validateReactResult(ReactResult result, String goal);

    /**
     * 优化ReAct提示
     */
    String optimizeReactPrompt(String goal, List<String> observations);

    /**
     * 分析ReAct决策过程
     */
    Map<String, Object> analyzeReactProcess(ReactResult result);

    /**
     * 数据模型：ReAct决策结果
     */
    class ReactResult {
        private String goal;
        private List<ReactStep> steps;
        private String finalAnswer;
        private boolean success;
        private String errorMessage;
        private long executionTimeMs;
        private int totalSteps;

        // Getters and Setters
        public String getGoal() {
            return goal;
        }

        public void setGoal(String goal) {
            this.goal = goal;
        }

        public List<ReactStep> getSteps() {
            return steps;
        }

        public void setSteps(List<ReactStep> steps) {
            this.steps = steps;
        }

        public String getFinalAnswer() {
            return finalAnswer;
        }

        public void setFinalAnswer(String finalAnswer) {
            this.finalAnswer = finalAnswer;
        }

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }

        public long getExecutionTimeMs() {
            return executionTimeMs;
        }

        public void setExecutionTimeMs(long executionTimeMs) {
            this.executionTimeMs = executionTimeMs;
        }

        public int getTotalSteps() {
            return totalSteps;
        }

        public void setTotalSteps(int totalSteps) {
            this.totalSteps = totalSteps;
        }
    }

    /**
     * 数据模型：ReAct步骤
     */
    class ReactStep {
        private int stepNumber;
        private String thought;
        private String action;
        private String actionInput;
        private String observation;
        private long stepTimeMs;

        // Getters and Setters
        public int getStepNumber() {
            return stepNumber;
        }

        public void setStepNumber(int stepNumber) {
            this.stepNumber = stepNumber;
        }

        public String getThought() {
            return thought;
        }

        public void setThought(String thought) {
            this.thought = thought;
        }

        public String getAction() {
            return action;
        }

        public void setAction(String action) {
            this.action = action;
        }

        public String getActionInput() {
            return actionInput;
        }

        public void setActionInput(String actionInput) {
            this.actionInput = actionInput;
        }

        public String getObservation() {
            return observation;
        }

        public void setObservation(String observation) {
            this.observation = observation;
        }

        public long getStepTimeMs() {
            return stepTimeMs;
        }

        public void setStepTimeMs(long stepTimeMs) {
            this.stepTimeMs = stepTimeMs;
        }
    }

    /**
     * 数据模型：ReAct步骤结果
     */
    class ReactStepResult {
        private int stepNumber;
        private ReactStep step;
        private boolean continueProcessing;
        private String errorMessage;

        // Getters and Setters
        public int getStepNumber() {
            return stepNumber;
        }

        public void setStepNumber(int stepNumber) {
            this.stepNumber = stepNumber;
        }

        public ReactStep getStep() {
            return step;
        }

        public void setStep(ReactStep step) {
            this.step = step;
        }

        public boolean isContinueProcessing() {
            return continueProcessing;
        }

        public void setContinueProcessing(boolean continueProcessing) {
            this.continueProcessing = continueProcessing;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }
    }
}
