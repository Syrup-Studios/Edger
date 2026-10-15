# Edger 0.2.0

- Fixed log record loss after database write failures and during normal shutdown. Records stay queued until the write succeeds, failed writes are retried, and shutdown waits for the active write before saving remaining records. A forced stop or shutdown timeout can still lose pending records.
