package com.cookiesstore.admin.scheduling;

import com.cookiesstore.common.repositories.AdminSourcePosConfigRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class PosDailySessionCloserJob {

    private static final Logger LOGGER = LoggerFactory.getLogger(PosDailySessionCloserJob.class);

    private final AdminSourcePosConfigRepository sourcePosConfigRepository;

    public PosDailySessionCloserJob(AdminSourcePosConfigRepository sourcePosConfigRepository) {
        this.sourcePosConfigRepository = sourcePosConfigRepository;
    }

    @Transactional
    @Scheduled(cron = "0 59 23 * * *", zone = "${app.scheduling.timezone:America/Havana}")
    public void closeOpenPosSessionsAtEndOfDay() {
        int closed = sourcePosConfigRepository.closeAllOpenSessionsForToday();
        if (closed > 0) {
            LOGGER.info("POS daily close job completed. Closed {} open session(s).", closed);
            return;
        }
        LOGGER.debug("POS daily close job completed. No open sessions were found.");
    }
}
