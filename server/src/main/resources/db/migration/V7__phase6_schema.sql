-- Phase 6: Community Recipe Platform, Moderation, Trust & Gamification

-- 1. Extend Users with profile & professional credentials fields
ALTER TABLE users ADD COLUMN IF NOT EXISTS bio VARCHAR(500) DEFAULT '';
ALTER TABLE users ADD COLUMN IF NOT EXISTS avatar_url VARCHAR(500) DEFAULT '';
ALTER TABLE users ADD COLUMN IF NOT EXISTS is_verified_pro BOOLEAN DEFAULT FALSE;
ALTER TABLE users ADD COLUMN IF NOT EXISTS pro_category VARCHAR(50) DEFAULT NULL;

-- 2. Community Recipes Table
CREATE TABLE IF NOT EXISTS community_recipes (
    id BIGSERIAL PRIMARY KEY,
    author_id BIGINT NOT NULL REFERENCES users(id),
    title VARCHAR(200) NOT NULL,
    description TEXT,
    cover_image_url VARCHAR(500),
    base_servings INT NOT NULL DEFAULT 2,
    prep_minutes INT NOT NULL DEFAULT 15,
    cook_minutes INT NOT NULL DEFAULT 20,
    meal_slot VARCHAR(30) NOT NULL,
    dietary_tags VARCHAR(500) DEFAULT '',
    instructions_text TEXT NOT NULL,
    calories_per_serving DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    protein_g DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    carbs_g DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    fat_g DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    fiber_g DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    sodium_mg DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    iron_mg DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    calcium_mg DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    status VARCHAR(30) NOT NULL DEFAULT 'APPROVED',
    like_count INT NOT NULL DEFAULT 0,
    comment_count INT NOT NULL DEFAULT 0,
    bookmark_count INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_community_recipes_author ON community_recipes(author_id);
CREATE INDEX IF NOT EXISTS idx_community_recipes_status ON community_recipes(status);
CREATE INDEX IF NOT EXISTS idx_community_recipes_created ON community_recipes(created_at DESC);

-- 3. Community Recipe Ingredients Table
CREATE TABLE IF NOT EXISTS community_recipe_ingredients (
    id BIGSERIAL PRIMARY KEY,
    community_recipe_id BIGINT NOT NULL REFERENCES community_recipes(id) ON DELETE CASCADE,
    food_id VARCHAR(100) REFERENCES foods(id),
    ingredient_name VARCHAR(200) NOT NULL,
    amount_g DOUBLE PRECISION NOT NULL,
    unit VARCHAR(30) NOT NULL DEFAULT 'g',
    is_verified_food BOOLEAN NOT NULL DEFAULT TRUE
);
CREATE INDEX IF NOT EXISTS idx_comm_ing_recipe ON community_recipe_ingredients(community_recipe_id);

-- 4. Community Likes Table
CREATE TABLE IF NOT EXISTS community_likes (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    community_recipe_id BIGINT NOT NULL REFERENCES community_recipes(id) ON DELETE CASCADE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_user_recipe_like UNIQUE(user_id, community_recipe_id)
);
CREATE INDEX IF NOT EXISTS idx_comm_likes_recipe ON community_likes(community_recipe_id);

-- 5. Community Comments Table
CREATE TABLE IF NOT EXISTS community_comments (
    id BIGSERIAL PRIMARY KEY,
    community_recipe_id BIGINT NOT NULL REFERENCES community_recipes(id) ON DELETE CASCADE,
    author_id BIGINT NOT NULL REFERENCES users(id),
    content TEXT NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'APPROVED',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_comm_comments_recipe ON community_comments(community_recipe_id);

-- 6. User Follows Table
CREATE TABLE IF NOT EXISTS community_follows (
    id BIGSERIAL PRIMARY KEY,
    follower_id BIGINT NOT NULL REFERENCES users(id),
    following_id BIGINT NOT NULL REFERENCES users(id),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_user_follow UNIQUE(follower_id, following_id)
);

-- 7. Content Reports Table
CREATE TABLE IF NOT EXISTS content_reports (
    id BIGSERIAL PRIMARY KEY,
    reporter_id BIGINT NOT NULL REFERENCES users(id),
    target_type VARCHAR(30) NOT NULL,
    target_id BIGINT NOT NULL,
    reason_category VARCHAR(50) NOT NULL,
    description TEXT,
    ai_screening_verdict VARCHAR(30),
    ai_screening_reason TEXT,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_content_reports_status ON content_reports(status);

-- 8. Moderation Actions Audit Table
CREATE TABLE IF NOT EXISTS moderation_actions (
    id BIGSERIAL PRIMARY KEY,
    moderator_id BIGINT NOT NULL REFERENCES users(id),
    target_type VARCHAR(30) NOT NULL,
    target_id BIGINT NOT NULL,
    action_taken VARCHAR(50) NOT NULL,
    moderator_notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- 9. Professional Verifications Table
CREATE TABLE IF NOT EXISTS professional_verifications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    professional_category VARCHAR(50) NOT NULL,
    credentials_summary TEXT NOT NULL,
    license_number VARCHAR(100),
    document_filename VARCHAR(255),
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    reviewed_by BIGINT REFERENCES users(id),
    reviewer_notes TEXT,
    reviewed_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_prof_verif_user ON professional_verifications(user_id);

-- 10. User Habit Streaks Table
CREATE TABLE IF NOT EXISTS user_streaks (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    streak_type VARCHAR(50) NOT NULL,
    current_streak_days INT NOT NULL DEFAULT 0,
    longest_streak_days INT NOT NULL DEFAULT 0,
    last_activity_date DATE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_user_streak UNIQUE(user_id, streak_type)
);

-- 11. Badge Definitions Table
CREATE TABLE IF NOT EXISTS badge_definitions (
    id VARCHAR(50) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT NOT NULL,
    icon VARCHAR(50) NOT NULL,
    category VARCHAR(50) NOT NULL,
    unlock_threshold INT NOT NULL DEFAULT 1
);

-- 12. User Badges Table
CREATE TABLE IF NOT EXISTS user_badges (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    badge_id VARCHAR(50) NOT NULL REFERENCES badge_definitions(id),
    awarded_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_user_badge UNIQUE(user_id, badge_id)
);

-- 13. Challenges Table
CREATE TABLE IF NOT EXISTS challenges (
    id VARCHAR(50) PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    icon VARCHAR(50) NOT NULL,
    category VARCHAR(50) NOT NULL,
    target_count INT NOT NULL DEFAULT 7,
    duration_days INT NOT NULL DEFAULT 7,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

-- 14. Challenge Participations Table
CREATE TABLE IF NOT EXISTS challenge_participations (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    challenge_id VARCHAR(50) NOT NULL REFERENCES challenges(id),
    progress INT NOT NULL DEFAULT 0,
    completed BOOLEAN NOT NULL DEFAULT FALSE,
    completed_at TIMESTAMP WITH TIME ZONE,
    joined_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_user_challenge UNIQUE(user_id, challenge_id)
);

-- Seed Initial Badge Definitions
INSERT INTO badge_definitions (id, name, description, icon, category, unlock_threshold) VALUES
('FIRST_MEAL_LOGGED', 'First Meal Logged', 'Started your journey by logging your first meal in the food diary.', '🥗', 'NUTRITION', 1),
('SEVEN_DAY_DIARY', '7-Day Diary Habit', 'Logged meals for 7 consecutive days in your food diary.', '📅', 'NUTRITION', 7),
('HYDRATION_HERO', 'Hydration Hero', 'Met your daily fluid intake goal 7 days in a row.', '💧', 'HYDRATION', 7),
('STEP_EXPLORER', 'Walking Vitality', 'Reached your daily step target on 5 distinct days.', '🚶', 'ACTIVITY', 5),
('HOME_CHEF', 'Home Chef', 'Prepared 3 home-cooked meals using EasyNutri interactive cooking mode.', '🍳', 'COOKING', 3),
('ZERO_PANTRY_WASTE', 'Zero Food Waste', 'Cooked meals utilizing pantry items approaching expiration.', '🥫', 'COOKING', 3),
('COMMUNITY_CREATOR', 'Community Contributor', 'Shared your first original healthy recipe with the EasyNutri community.', '🌱', 'COMMUNITY', 1)
ON CONFLICT (id) DO NOTHING;

-- Seed Initial Challenges
INSERT INTO challenges (id, title, description, icon, category, target_count, duration_days, active) VALUES
('BREAKFAST_HABIT_7D', '7-Day Nourishing Breakfast', 'Start every morning with a balanced, nutrient-rich breakfast for 7 days.', '🥞', 'NUTRITION', 7, 7, true),
('HOME_COOKING_5M', '5 Home-Cooked Meals Challenge', 'Prepare at least 5 wholesome meals at home using whole ingredients.', '🍳', 'COOKING', 5, 7, true),
('PANTRY_WASTE_REDUCTION', 'Pantry Waste Saver', 'Cook 3 meals that make use of ingredients in your pantry nearing expiration.', '🥫', 'PANTRY', 3, 7, true),
('HYDRATION_CONSISTENCY_7D', '7-Day Hydration Habit', 'Consistently hit your personalized fluid targets every day for a week.', '💧', 'HABIT', 7, 7, true)
ON CONFLICT (id) DO NOTHING;
