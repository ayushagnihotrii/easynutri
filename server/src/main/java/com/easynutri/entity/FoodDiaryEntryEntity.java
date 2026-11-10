package com.easynutri.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a daily meal entry slot (Breakfast, Lunch, Dinner, Snack) containing individually
 * logged food portions.
 */
@Entity
@Table(
    name = "food_diary_entries",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uq_diary_user_date_slot",
          columnNames = {"user_id", "diary_date", "meal_slot"})
    })
public class FoodDiaryEntryEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private UserEntity user;

  @Column(name = "diary_date", nullable = false)
  private LocalDate diaryDate;

  @Column(name = "meal_slot", nullable = false, length = 30)
  private String mealSlot;

  @Column(columnDefinition = "TEXT")
  private String notes;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt = Instant.now();

  @OneToMany(mappedBy = "entry", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<FoodDiaryItemEntity> items = new ArrayList<>();

  public FoodDiaryEntryEntity() {}

  public FoodDiaryEntryEntity(UserEntity user, LocalDate diaryDate, String mealSlot) {
    this.user = user;
    this.diaryDate = diaryDate;
    this.mealSlot = mealSlot;
    this.createdAt = Instant.now();
  }

  public Long getId() {
    return id;
  }

  public UserEntity getUser() {
    return user;
  }

  public void setUser(UserEntity user) {
    this.user = user;
  }

  public LocalDate getDiaryDate() {
    return diaryDate;
  }

  public void setDiaryDate(LocalDate diaryDate) {
    this.diaryDate = diaryDate;
  }

  public String getMealSlot() {
    return mealSlot;
  }

  public void setMealSlot(String mealSlot) {
    this.mealSlot = mealSlot;
  }

  public String getNotes() {
    return notes;
  }

  public void setNotes(String notes) {
    this.notes = notes;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }

  public List<FoodDiaryItemEntity> getItems() {
    return items;
  }

  public void setItems(List<FoodDiaryItemEntity> items) {
    this.items = items;
  }

  public void addItem(FoodDiaryItemEntity item) {
    items.add(item);
    item.setEntry(this);
  }

  public void removeItem(FoodDiaryItemEntity item) {
    items.remove(item);
    item.setEntry(null);
  }
}
