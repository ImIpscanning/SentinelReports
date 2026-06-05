package com.imipscanning.sentinelreports.common;

import com.imipscanning.sentinelreports.common.util.Pagination;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PaginationTest {
    @Test
    void slicesPages() {
        Pagination<Integer> page = Pagination.of(List.of(1, 2, 3, 4, 5), 2, 2);
        assertThat(page.items()).containsExactly(3, 4);
        assertThat(page.totalPages()).isEqualTo(3);
    }
}
