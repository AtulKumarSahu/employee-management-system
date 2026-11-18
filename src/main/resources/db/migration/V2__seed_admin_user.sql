-- ============================================================
-- V2__seed_admin_user.sql
-- Seed data: Create the initial ADMIN user for the system.
--
-- Password: Admin@123 (BCrypt hash below)
-- BCrypt hash generated with strength 10.
-- Generate your own at: https://bcrypt-generator.com
--
-- IMPORTANT: Change this password immediately after first login
-- in a production environment.
-- ============================================================

-- Insert the admin user
INSERT INTO users (username, email, password, is_active, is_locked, created_at)
VALUES (
    'admin',
    'admin@ems.com',
    '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.',  -- Admin@123
    TRUE,
    FALSE,
    NOW()
);

-- Assign ADMIN role to the admin user
INSERT INTO user_roles (user_id, role)
SELECT id, 'ADMIN'
FROM users
WHERE username = 'admin';

-- ============================================================
-- End of V2__seed_admin_user.sql
-- ============================================================

