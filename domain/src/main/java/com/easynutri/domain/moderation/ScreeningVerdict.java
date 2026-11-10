package com.easynutri.domain.moderation;

/** Outcome verdict of automated clinical and safety content screening. */
public enum ScreeningVerdict {
  APPROVE,
  REVIEW_REQUIRED,
  BLOCK
}
