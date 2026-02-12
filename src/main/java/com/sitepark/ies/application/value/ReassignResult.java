package com.sitepark.ies.application.value;

public record ReassignResult(int assigned, int unassigned) {

  private static final ReassignResult EMPTY = new ReassignResult(0, 0);

  public static ReassignResult empty() {
    return EMPTY;
  }
}
