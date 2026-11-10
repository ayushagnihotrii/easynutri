package com.easynutri.ai.tool;

import java.util.*;
import org.springframework.stereotype.Component;

/** Central registry holding all permitted Java application tools for the AI assistant. */
@Component
public class AiToolRegistry {

  private final Map<String, AiTool> toolMap = new HashMap<>();

  public AiToolRegistry(List<AiTool> tools) {
    if (tools != null) {
      for (AiTool tool : tools) {
        toolMap.put(tool.getName().toLowerCase(Locale.ROOT), tool);
      }
    }
  }

  public Optional<AiTool> getTool(String name) {
    if (name == null) {
      return Optional.empty();
    }
    return Optional.ofNullable(toolMap.get(name.trim().toLowerCase(Locale.ROOT)));
  }

  public Collection<AiTool> getAllTools() {
    return Collections.unmodifiableCollection(toolMap.values());
  }

  public boolean hasTool(String name) {
    return name != null && toolMap.containsKey(name.trim().toLowerCase(Locale.ROOT));
  }
}
