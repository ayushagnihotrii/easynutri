package com.easynutri.ai;

import com.easynutri.ai.tool.AiTool;
import com.easynutri.ai.tool.AiToolRegistry;
import com.easynutri.entity.AiMessageEntity;
import com.easynutri.entity.AiToolExecutionEntity;
import com.easynutri.repository.AiToolExecutionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AiToolExecutor {

  private static final Logger log = LoggerFactory.getLogger(AiToolExecutor.class);

  private final AiToolRegistry toolRegistry;
  private final AiToolExecutionRepository executionRepository;
  private final ObjectMapper objectMapper = new ObjectMapper();

  public AiToolExecutor(
      AiToolRegistry toolRegistry, AiToolExecutionRepository executionRepository) {
    this.toolRegistry = toolRegistry;
    this.executionRepository = executionRepository;
  }

  @Transactional
  public String executeTool(
      Long userId, AiMessageEntity message, String toolName, Map<String, Object> arguments) {
    Optional<AiTool> toolOpt = toolRegistry.getTool(toolName);
    if (toolOpt.isEmpty()) {
      log.warn("Attempted to execute unknown AI tool: {}", toolName);
      return "Unknown tool: " + toolName;
    }

    AiTool tool = toolOpt.get();
    long startTime = System.currentTimeMillis();
    String result;
    try {
      result = tool.execute(userId, arguments != null ? arguments : Collections.emptyMap());
    } catch (Exception e) {
      log.error("Execution error in AI tool {}: {}", toolName, e.getMessage(), e);
      result = "Tool execution failed: " + e.getMessage();
    }
    long elapsedMs = System.currentTimeMillis() - startTime;

    if (message != null) {
      try {
        String argsJson = objectMapper.writeValueAsString(arguments != null ? arguments : Map.of());
        AiToolExecutionEntity execution =
            new AiToolExecutionEntity(message, toolName, argsJson, result, elapsedMs);
        executionRepository.save(execution);
      } catch (Exception e) {
        log.warn("Failed to persist AI tool execution record: {}", e.getMessage());
      }
    }

    return result;
  }
}
