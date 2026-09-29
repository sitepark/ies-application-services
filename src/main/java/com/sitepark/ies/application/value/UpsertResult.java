package com.sitepark.ies.application.value;

public sealed interface UpsertResult {
  record Created(String id) implements UpsertResult {}

  record Updated(boolean updated) implements UpsertResult {}

  static UpsertResult.Created created(String id) {
    return new UpsertResult.Created(id);
  }

  static UpsertResult.Updated updated(boolean updated) {
    return new UpsertResult.Updated(updated);
  }
}
