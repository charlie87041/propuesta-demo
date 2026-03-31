package com.cookiesstore.common.services;

import java.sql.Date;

public record CustomerStatistics(
    Integer totalCustomers,
    Integer newCustomersInMonth,
    Float newCustomersInMonthPercent,
    Integer activeSessions,
    Float activeSessionsPercent,
    Date fromDate,
    Date toDate
) {
}
