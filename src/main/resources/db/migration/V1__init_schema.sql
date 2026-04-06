CREATE TYPE admin_role    AS ENUM ('SUPER_ADMIN', 'COMPLIANCE_OFFICER', 'VIEWER');
CREATE TYPE admin_status  AS ENUM ('ACTIVE', 'INACTIVE');
CREATE TYPE kyc_status    AS ENUM ('PENDING', 'VERIFIED', 'REJECTED');
CREATE TYPE account_status AS ENUM ('ACTIVE', 'SUSPENDED');
CREATE TYPE sub_tier      AS ENUM ('FREE', 'BASIC', 'PRO');
CREATE TYPE inv_status    AS ENUM ('PENDING', 'ACCEPTED', 'REJECTED');
CREATE TYPE compliance_flag AS ENUM ('COMPLIANT', 'NON_COMPLIANT');
CREATE TYPE alert_trigger AS ENUM ('COMPLIANCE_RATE_BELOW', 'INVOICE_VOLUME_EXCEEDS');
CREATE TYPE alert_severity AS ENUM ('INFO', 'WARNING', 'CRITICAL');
CREATE TYPE inv_event     AS ENUM ('SUBMITTED', 'RESUBMITTED', 'ACCEPTED', 'REJECTED');

CREATE TABLE admin_users (
  id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  full_name        VARCHAR(255) NOT NULL,
  email            VARCHAR(255) NOT NULL UNIQUE,
  password_hash    VARCHAR(255) NOT NULL,
  role             admin_role NOT NULL DEFAULT 'VIEWER',
  status           admin_status NOT NULL DEFAULT 'ACTIVE',
  failed_attempts  INT NOT NULL DEFAULT 0,
  locked_until     TIMESTAMPTZ,
  last_login_at    TIMESTAMPTZ,
  created_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at       TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE taxpayers (
  id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  name              VARCHAR(255) NOT NULL,
  tin               VARCHAR(100) NOT NULL UNIQUE,
  email             VARCHAR(255),
  phone             VARCHAR(50),
  kyc_status        kyc_status NOT NULL DEFAULT 'PENDING',
  subscription_tier sub_tier NOT NULL DEFAULT 'FREE',
  account_status    account_status NOT NULL DEFAULT 'ACTIVE',
  suspension_reason TEXT,
  suspended_by      UUID REFERENCES admin_users(id),
  registered_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at        TIMESTAMPTZ NOT NULL DEFAULT NOW()
);


CREATE TABLE invoices (
  id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  taxpayer_id           UUID NOT NULL REFERENCES taxpayers(id),
  invoice_number        VARCHAR(100) NOT NULL,
  amount                NUMERIC(15, 2) NOT NULL,
  submission_status     inv_status NOT NULL DEFAULT 'PENDING',
  compliance_flag       compliance_flag,
  payload               JSONB,
  nrs_validation_result JSONB,
  submitted_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at            TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_invoices_taxpayer ON invoices(taxpayer_id);
CREATE INDEX idx_invoices_submitted_at ON invoices(submitted_at DESC);
CREATE INDEX idx_invoices_status ON invoices(submission_status, compliance_flag);


CREATE TABLE invoice_history (
  id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  invoice_id  UUID NOT NULL REFERENCES invoices(id),
  event_type  inv_event NOT NULL,
  details     JSONB,
  created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE taxpayer_annotations (
  id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  taxpayer_id UUID NOT NULL REFERENCES taxpayers(id),
  admin_id    UUID NOT NULL REFERENCES admin_users(id),
  note        TEXT NOT NULL,
  is_flag     BOOLEAN NOT NULL DEFAULT FALSE,
  created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE audit_logs (
  id                 UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  admin_id           UUID REFERENCES admin_users(id),
  action_type        VARCHAR(100) NOT NULL,
  target_entity_type VARCHAR(100),
  target_entity_id   UUID,
  before_value       JSONB,
  after_value        JSONB,
  ip_address         VARCHAR(50),
  created_at         TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_audit_admin ON audit_logs(admin_id);
CREATE INDEX idx_audit_created ON audit_logs(created_at DESC);
CREATE INDEX idx_audit_action ON audit_logs(action_type);


CREATE TABLE alert_rules (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  created_by      UUID NOT NULL REFERENCES admin_users(id),
  name            VARCHAR(255) NOT NULL,
  trigger_type    alert_trigger NOT NULL,
  threshold_value NUMERIC(10, 2) NOT NULL,
  severity        alert_severity NOT NULL DEFAULT 'WARNING',
  is_active       BOOLEAN NOT NULL DEFAULT TRUE,
  created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE notifications (
  id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  alert_rule_id UUID REFERENCES alert_rules(id),
  title         VARCHAR(255) NOT NULL,
  description   TEXT,
  severity      alert_severity NOT NULL,
  entity_type   VARCHAR(100),
  entity_id     UUID,
  triggered_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_notif_triggered ON notifications(triggered_at DESC);


CREATE TABLE notification_reads (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  notification_id UUID NOT NULL REFERENCES notifications(id),
  admin_id        UUID NOT NULL REFERENCES admin_users(id),
  is_read         BOOLEAN NOT NULL DEFAULT FALSE,
  read_at         TIMESTAMPTZ,
  UNIQUE(notification_id, admin_id)
);

CREATE TABLE user_notification_settings (
  id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  admin_id              UUID NOT NULL UNIQUE REFERENCES admin_users(id),
  email_critical_alerts BOOLEAN NOT NULL DEFAULT FALSE,
  updated_at            TIMESTAMPTZ NOT NULL DEFAULT NOW()
);