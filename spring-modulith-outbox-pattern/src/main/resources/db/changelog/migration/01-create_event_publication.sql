CREATE TABLE IF NOT EXISTS event_publication
(
  id                      UUID NOT NULL,
  listener_id             TEXT NOT NULL,
  event_type              TEXT NOT NULL,
  serialized_event        TEXT NOT NULL,
  publication_date        TIMESTAMP WITH TIME ZONE NOT NULL,
  completion_date         TIMESTAMP WITH TIME ZONE,
  last_resubmission_date  TIMESTAMP WITH TIME ZONE,
  completion_attempts     INTEGER NOT NULL DEFAULT 0,
  status                  smallint NOT NULL DEFAULT '0',
  PRIMARY KEY (id)
);

CREATE INDEX IF NOT EXISTS event_publication_listener_id_serialized_event_idx ON event_publication (listener_id, serialized_event);
CREATE INDEX IF NOT EXISTS event_publication_completion_date_idx ON event_publication (completion_date);