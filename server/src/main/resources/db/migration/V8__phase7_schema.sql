-- ==============================================================================
-- EasyNutri Flyway Migration V8 — Phase 7: Family Profiles, Household Management,
-- Household Meal Planning, and Offline Synchronization Ledger
-- ==============================================================================

-- 1. Households Table
CREATE TABLE IF NOT EXISTS households (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    owner_user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_households_owner ON households(owner_user_id);

-- 2. Household Members Table
-- Links managed users or family member profiles into a household with defined roles.
CREATE TABLE IF NOT EXISTS household_members (
    id BIGSERIAL PRIMARY KEY,
    household_id BIGINT NOT NULL REFERENCES households(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    display_name VARCHAR(100) NOT NULL,
    relationship VARCHAR(50) NOT NULL DEFAULT 'OTHER', -- SELF, SPOUSE, FATHER, MOTHER, CHILD, SIBLING, OTHER
    role VARCHAR(50) NOT NULL DEFAULT 'MEMBER',        -- HOUSEHOLD_OWNER, HOUSEHOLD_ADMIN, MEMBER, DEPENDENT
    avatar_color VARCHAR(30) DEFAULT '#237A57',
    avatar_initials VARCHAR(10) DEFAULT 'FM',
    managed_by_user_id BIGINT REFERENCES users(id) ON DELETE SET NULL, -- for DEPENDENT profiles managed by an adult
    can_manage_household BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_household_member UNIQUE(household_id, user_id)
);

CREATE INDEX IF NOT EXISTS idx_household_members_household ON household_members(household_id);
CREATE INDEX IF NOT EXISTS idx_household_members_user ON household_members(user_id);

-- 3. Household Meal Plans Table
-- Holds multi-member shared meals and aggregated grocery requirements
CREATE TABLE IF NOT EXISTS household_meal_plans (
    id BIGSERIAL PRIMARY KEY,
    household_id BIGINT NOT NULL REFERENCES households(id) ON DELETE CASCADE,
    recipe_id VARCHAR(100) NOT NULL REFERENCES recipes(id) ON DELETE CASCADE,
    plan_date DATE NOT NULL,
    meal_slot VARCHAR(30) NOT NULL, -- BREAKFAST, LUNCH, DINNER, SNACK
    participating_member_ids VARCHAR(255) NOT NULL, -- comma-separated user_ids
    total_servings DOUBLE PRECISION NOT NULL DEFAULT 2.0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_household_meal_plans_household_date ON household_meal_plans(household_id, plan_date);

-- 4. Sync Operations Table
-- Server-side ledger for offline-first synchronization with strict idempotency and conflict auditing
CREATE TABLE IF NOT EXISTS sync_operations (
    id BIGSERIAL PRIMARY KEY,
    operation_id VARCHAR(100) NOT NULL,
    idempotency_key VARCHAR(100) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    action_type VARCHAR(50) NOT NULL, -- LOG_DIARY, LOG_WATER, LOG_WORKOUT, LOG_STEPS, UPDATE_GROCERY, UPDATE_PANTRY, DELETE_DIARY
    payload_json TEXT NOT NULL,
    client_version INT NOT NULL DEFAULT 1,
    status VARCHAR(30) NOT NULL DEFAULT 'PROCESSED', -- PROCESSED, DUPLICATE_IGNORED, CONFLICT_DETECTED, REJECTED
    error_message TEXT,
    processed_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_sync_ops_user ON sync_operations(user_id);
CREATE INDEX IF NOT EXISTS idx_sync_ops_idempotency ON sync_operations(idempotency_key);
