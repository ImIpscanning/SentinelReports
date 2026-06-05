package com.imipscanning.sentinelreports.common.service;

import com.imipscanning.sentinelreports.common.model.Report;
import com.imipscanning.sentinelreports.common.model.ReportFilter;
import com.imipscanning.sentinelreports.common.model.ReportQuery;
import com.imipscanning.sentinelreports.common.storage.ReportRepository;
import com.imipscanning.sentinelreports.common.util.Pagination;

import java.util.List;
import java.util.Optional;

public final class ReportSearchService {
    private final ReportRepository reportRepository;

    public ReportSearchService(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    public Optional<Report> find(long id) {
        return reportRepository.findById(id);
    }

    public Pagination<Report> query(ReportQuery query) {
        List<Report> reports = reportRepository.list(query);
        long total = reportRepository.count(query.filter());
        return new Pagination<>(reports, query.page(), query.pageSize(), total);
    }

    public Pagination<Report> firstPage(ReportFilter filter) {
        return query(ReportQuery.firstPage(filter));
    }
}
