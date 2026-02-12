package com.sitepark.ies.application.value;

import org.jetbrains.annotations.NotNull;

public sealed interface UpsertResult {
  record Created(@NotNull String id) implements UpsertResult {}

  record Updated(boolean updated) implements UpsertResult {}

  static UpsertResult.Created created(@NotNull String id) {
    return new UpsertResult.Created(id);
  }

  static UpsertResult.Updated updated(boolean updated) {
    return new UpsertResult.Updated(updated);
  }
}
