CREATE TABLE IF NOT EXISTS note_attachment (id VARCHAR(150) NOT NULL, note VARCHAR(150) NOT NULL, src TEXT NOT NULL, alt VARCHAR(100000) NULL);
ALTER TABLE note_attachment ADD CONSTRAINT note_attachment_id_unique UNIQUE (id);
ALTER TABLE note_attachment ADD CONSTRAINT fk_note_attachment_note__id FOREIGN KEY (note) REFERENCES note(id) ON DELETE CASCADE ON UPDATE RESTRICT;
