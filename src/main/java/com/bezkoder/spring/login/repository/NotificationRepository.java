package com.bezkoder.spring.login.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bezkoder.spring.login.models.Notification;
import com.bezkoder.spring.login.models.User;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByReceiverAndReadFalseOrderByTimestampDesc(User receiver);

    List<Notification> findByReceiver(String id);

    default Notification findById(String id) {
        Optional<Notification> optionalNotification = findById(Long.parseLong(id));
        return optionalNotification.orElseThrow(() -> new NoSuchElementException("Notification not found"));
    }
}
