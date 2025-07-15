package com.edwardjones.drift.batch;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ChecksumUtilTest {

    @Test
    void calculateChecksum_GeneratesConsistentHash() {
        String input = "test-data";

        String checksum1 = ChecksumUtil.sha256Hex(input);
        String checksum2 = ChecksumUtil.sha256Hex(input);

        assertThat(checksum1).isEqualTo(checksum2);
        assertThat(checksum1).isNotEmpty();
    }

    @Test
    void calculateChecksum_DifferentInputsDifferentHashes() {
        String input1 = "test-data-1";
        String input2 = "test-data-2";

        String checksum1 = ChecksumUtil.sha256Hex(input1);
        String checksum2 = ChecksumUtil.sha256Hex(input2);

        assertThat(checksum1).isNotEqualTo(checksum2);
    }

    @Test
    void calculateChecksum_HandlesNullInput() {
        String checksum = ChecksumUtil.sha256Hex(null);

        assertThat(checksum).isNotNull();
    }

    @Test
    void calculateChecksum_HandlesEmptyInput() {
        String checksum = ChecksumUtil.sha256Hex("");

        assertThat(checksum).isNotNull();
        assertThat(checksum).isNotEmpty();
    }
}
