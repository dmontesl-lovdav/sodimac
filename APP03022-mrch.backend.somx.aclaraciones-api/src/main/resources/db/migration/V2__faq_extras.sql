-- src/main/resources/db/migration/V2__faq_extras.sql
----------------------------------------------------
-- alias questions ---------------------------------
CREATE TABLE IF NOT EXISTS faq_alias
(
    id          BIGSERIAL PRIMARY KEY,
    faq_id      BIGINT  NOT NULL REFERENCES faq (id) ON DELETE CASCADE,
    question    TEXT    NOT NULL
);

-- related FAQs ------------------------------------
CREATE TABLE IF NOT EXISTS faq_related
(
    source_id   BIGINT NOT NULL REFERENCES faq (id) ON DELETE CASCADE,
    related_id  BIGINT NOT NULL REFERENCES faq (id) ON DELETE CASCADE,
    PRIMARY KEY (source_id, related_id)
);

-- attachments -------------------------------------
CREATE TABLE IF NOT EXISTS faq_attachment
(
    id          BIGSERIAL PRIMARY KEY,
    faq_id      BIGINT      NOT NULL REFERENCES faq (id) ON DELETE CASCADE,
    file_name   VARCHAR(255) NOT NULL,
    url         TEXT         NOT NULL
);
