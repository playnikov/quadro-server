CREATE TABLE notifications (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    type VARCHAR(50) NOT NULL,
    title VARCHAR(500) NOT NULL,
    body TEXT NOT NULL,
    data TEXT,
    priority VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    channel VARCHAR(20) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    sent_at TIMESTAMPTZ,
    delivered_at TIMESTAMPTZ,
    read_at TIMESTAMPTZ
);

CREATE INDEX idx_notifications_user ON notifications(user_id);
CREATE INDEX idx_notifications_user_status ON notifications(user_id, status);
CREATE INDEX idx_notifications_created ON notifications(created_at);