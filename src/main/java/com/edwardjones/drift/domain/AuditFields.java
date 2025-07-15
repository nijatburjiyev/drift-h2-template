package com.edwardjones.drift.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * Reconciliation helpers automatically copied into every concrete table.
 */
@MappedSuperclass
@Getter @Setter
public abstract class AuditFields {

    /** When this row was (re-)imported. */
    @Column(name = "imported_at", nullable = false)
    private Instant importedAt;

    /** SHA-256 of the JSON object as received – lets you detect real changes. */
    @Column(name = "json_checksum", length = 64)
    private String jsonChecksum;

    /** Optional batch id if you ever run multiple jobs a day. */
    @Column(name = "import_batch_id")
    private Long importBatchId;

    /* Auto-populate importedAt if callers forget */
    @PrePersist @PreUpdate
    private void touch() {
        if (importedAt == null) importedAt = Instant.now();
    }
}
