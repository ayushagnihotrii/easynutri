-- EasyNutri — Phase 8 Production Release & Performance Optimization Migration
-- Adds index coverage for high-throughput queries, personal data deletion, and multi-user households.

CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_user_profiles_user_id ON user_profiles(user_id);

CREATE INDEX IF NOT EXISTS idx_food_diary_user_date ON food_diary_entries(user_id, entry_date);
CREATE INDEX IF NOT EXISTS idx_food_diary_items_entry ON food_diary_items(entry_id);

CREATE INDEX IF NOT EXISTS idx_water_logs_user_date ON water_logs(user_id, record_date);
CREATE INDEX IF NOT EXISTS idx_step_records_user_date ON step_records(user_id, record_date);
CREATE INDEX IF NOT EXISTS idx_workout_logs_user_date ON workout_logs(user_id, workout_date);
CREATE INDEX IF NOT EXISTS idx_weight_meas_user_date ON weight_measurements(user_id, measured_date);

CREATE INDEX IF NOT EXISTS idx_pantry_user_exp ON pantry_items(user_id, expiration_date);
CREATE INDEX IF NOT EXISTS idx_grocery_user_checked ON grocery_items(user_id, checked);
CREATE INDEX IF NOT EXISTS idx_meal_plans_user_date ON meal_plan_items(user_id, plan_date);

CREATE INDEX IF NOT EXISTS idx_community_recipes_published ON community_recipes(is_published, created_at);
CREATE INDEX IF NOT EXISTS idx_community_comments_recipe ON community_comments(recipe_id);
CREATE INDEX IF NOT EXISTS idx_content_reports_status ON content_reports(status);

CREATE INDEX IF NOT EXISTS idx_households_owner ON households(owner_user_id);
CREATE INDEX IF NOT EXISTS idx_household_members_user ON household_members(user_id);
CREATE INDEX IF NOT EXISTS idx_household_meal_plans_hh_date ON household_meal_plans(household_id, plan_date);

CREATE INDEX IF NOT EXISTS idx_sync_ops_user_status ON sync_operations(user_id, status);
CREATE INDEX IF NOT EXISTS idx_sync_ops_idemp ON sync_operations(idempotency_key);
