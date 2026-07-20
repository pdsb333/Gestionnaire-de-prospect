-- role was nullable at the DB level despite CustomUserDetails.getAuthorities() assuming it's
-- always set (a NULL role bypasses the existing CHECK constraint since NULL comparisons aren't
-- FALSE). Every write path already sets it, so this only guards against a future one that doesn't.
UPDATE users SET role = 'ROLE_USER' WHERE role IS NULL;
ALTER TABLE users ALTER COLUMN role SET NOT NULL;
