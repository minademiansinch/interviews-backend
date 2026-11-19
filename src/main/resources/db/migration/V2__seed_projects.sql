INSERT INTO projects (id, name, account_type, trust_level, blocked, last_event_at, version)
VALUES
    ('proj-cloud-limited',     'Acme SMS Cloud',         'CLOUD',      'LIMITED',       FALSE, now() - interval '2 hours', 0),
    ('proj-cloud-unverified',  'Trial Messaging App',    'CLOUD',      'UNVERIFIED',    FALSE, now() - interval '1 hour',  0),
    ('proj-cloud-trusted',     'Verified Fintech Prod',  'CLOUD',      'TRUSTED',       FALSE, now() - interval '1 day',   0),
    ('proj-enterprise-exempt', 'Global Bank Enterprise', 'ENTERPRISE', 'EXEMPT',        FALSE, now() - interval '7 days',  0),
    ('proj-already-blocked',   'Phishing Sender 42',     'CLOUD',      'NON_COMPLIANT', TRUE,  now() - interval '30 minutes', 1),
    ('proj-high-volume',       'Flash Sale Dispatcher',  'CLOUD',      'LIMITED',       FALSE, now() - interval '5 minutes',  0);

INSERT INTO block_audit (id, project_id, action, actor, reason, previous_trust_level, new_trust_level, created_at)
VALUES
    ('11111111-1111-1111-1111-111111111111',
     'proj-already-blocked',
     'BLOCK',
     'compliance-ops@sinch.com',
     'Suspected phishing SMS campaign',
     'LIMITED',
     'NON_COMPLIANT',
     now() - interval '30 minutes');
