-- Default Super Admin (password is "Admin@1234" — bcrypt hashed)
INSERT INTO admin_users (id, full_name, email, password_hash, role, status)
VALUES (
  gen_random_uuid(),
  'Super Admin',
  'admin@qucoon.com',
  '$2a$12$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
  'SUPER_ADMIN',
  'ACTIVE'
);

-- Sample taxpayers
INSERT INTO taxpayers (id, name, tin, email, kyc_status, subscription_tier, account_status)
VALUES
  (gen_random_uuid(), 'Dangote Industries Ltd', 'TIN-001234', 'finance@dangote.com', 'VERIFIED', 'PRO', 'ACTIVE'),
  (gen_random_uuid(), 'MTN Nigeria Plc', 'TIN-005678', 'tax@mtn.ng', 'VERIFIED', 'PRO', 'ACTIVE'),
  (gen_random_uuid(), 'Kemi Stores Ltd', 'TIN-009101', 'kemi@kemistores.com', 'PENDING', 'BASIC', 'ACTIVE');