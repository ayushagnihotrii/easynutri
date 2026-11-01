package com.easynutri.domain.rules;

/** Outcome verdict from evaluating a candidate against dietary, allergic, or health rules. */
public enum RuleVerdict {
  ALLOW,
  WARN,
  BLOCK
}
