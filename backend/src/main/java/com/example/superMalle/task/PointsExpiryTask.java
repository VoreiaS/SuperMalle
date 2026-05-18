package com.example.superMalle.task;

import com.example.superMalle.repository.UserLoyaltyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class PointsExpiryTask {

    private final UserLoyaltyRepository userLoyaltyRepository;

    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void expirePoints() {
        try {
            var allUsers = userLoyaltyRepository.findAll();
            int expiredCount = 0;
            for (var ul : allUsers) {
                if (ul.getPointsExpireAt() != null
                        && ul.getPointsExpireAt().isBefore(LocalDateTime.now())
                        && ul.getAvailablePoints() > 0) {
                    int toExpire = Math.min(ul.getAvailablePoints(), ul.getAvailablePoints());
                    ul.setPointsExpired(ul.getPointsExpired() + toExpire);
                    ul.setAvailablePoints(ul.getAvailablePoints() - toExpire);
                    ul.setPointsExpireAt(null);
                    userLoyaltyRepository.save(ul);
                    expiredCount++;
                }
            }
            if (expiredCount > 0) {
                log.info("Expired points for {} users", expiredCount);
            }
        } catch (Exception e) {
            log.error("Failed to expire points: {}", e.getMessage(), e);
        }
    }
}
