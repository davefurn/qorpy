-- Migration V3: Create table for JWT Blacklisting (Logout US-003)

CREATE TABLE blacklisted_tokens (
                                    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                    token VARCHAR(512) NOT NULL UNIQUE,
                                    expiry_date TIMESTAMP NOT NULL
);

-- Add an index on the token column to make the security filter check blazing fast
CREATE INDEX idx_blacklisted_tokens_token ON blacklisted_tokens(token);