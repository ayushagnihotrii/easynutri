package com.easynutri.service;

import com.easynutri.entity.PantryItemEntity;
import com.easynutri.entity.UserEntity;
import com.easynutri.entity.UserNotificationEntity;
import com.easynutri.repository.PantryItemRepository;
import com.easynutri.repository.UserNotificationRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PantryExpiryScheduler {

  private static final Logger log = LoggerFactory.getLogger(PantryExpiryScheduler.class);

  private final PantryItemRepository pantryItemRepository;
  private final UserNotificationRepository notificationRepository;

  public PantryExpiryScheduler(
      PantryItemRepository pantryItemRepository,
      UserNotificationRepository notificationRepository) {
    this.pantryItemRepository = pantryItemRepository;
    this.notificationRepository = notificationRepository;
  }

  @Scheduled(cron = "0 0 8 * * *")
  @Transactional
  public void checkExpiringPantryItems() {
    log.info("Running daily pantry expiry check...");
    LocalDate alertThreshold = LocalDate.now().plusDays(2);
    List<PantryItemEntity> expiring =
        pantryItemRepository.findByExpirationDateLessThanEqual(alertThreshold);

    if (expiring.isEmpty()) {
      return;
    }

    Map<UserEntity, List<PantryItemEntity>> byUser =
        expiring.stream().collect(Collectors.groupingBy(PantryItemEntity::getUser));

    for (Map.Entry<UserEntity, List<PantryItemEntity>> entry : byUser.entrySet()) {
      UserEntity user = entry.getKey();
      List<PantryItemEntity> items = entry.getValue();

      String itemSummary =
          items.stream()
              .map(p -> p.getFood().getNameEn())
              .limit(3)
              .collect(Collectors.joining(", "));

      if (items.size() > 3) {
        itemSummary += " and " + (items.size() - 3) + " more";
      }

      String message =
          items.size() == 1
              ? itemSummary + " is expiring soon! Tap to see recommended recipes."
              : items.size()
                  + " ingredients ("
                  + itemSummary
                  + ") are expiring soon. Cook them before they spoil!";

      UserNotificationEntity notification =
          new UserNotificationEntity(user, "Pantry Expiry Alert", message, "WARNING");

      notificationRepository.save(notification);
      log.info("Created expiry notification for user: {}", user.getUsername());
    }
  }
}
