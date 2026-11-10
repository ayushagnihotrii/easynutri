package com.easynutri.ai.tool;

import java.util.Map;

/** Controlled application tool contract that can be invoked by the AI orchestrator. */
public interface AiTool {

  /** Name of the tool as exposed to the AI model. */
  String getName();

  /** Human-readable description of what the tool executes. */
  String getDescription();

  /**
   * Executes the tool against actual Java services within the security context of the authenticated
   * user.
   *
   * @param userId authenticated user ID
   * @param arguments validated tool arguments
   * @return JSON or structured text response
   */
  String execute(Long userId, Map<String, Object> arguments);
}
