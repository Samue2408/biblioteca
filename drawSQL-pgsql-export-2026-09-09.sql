CREATE TABLE "app_users"(
    "id" bigserial NOT NULL,
    "name" VARCHAR(150) NOT NULL,
    "email" VARCHAR(150) NOT NULL,
    "password" VARCHAR(255) NOT NULL,
    "role" VARCHAR(20) NOT NULL,
    "blocked_until" TIMESTAMP(0) WITHOUT TIME ZONE NULL,
    "blocked" BOOLEAN NOT NULL,
    "delays" INTEGER NOT NULL
);
ALTER TABLE
    "app_users" ADD PRIMARY KEY("id");
ALTER TABLE
    "app_users" ADD CONSTRAINT "app_users_email_unique" UNIQUE("email");
CREATE TABLE "books"(
    "id" bigserial NOT NULL,
    "title" VARCHAR(255) NOT NULL,
    "author" VARCHAR(255) NOT NULL,
    "isbn" VARCHAR(20) NOT NULL,
    "publication_year" INTEGER NULL,
    "status" VARCHAR(20) NOT NULL DEFAULT 'DISPONIBLE',
    "cover_url" VARCHAR(500) NULL,
    "subjects" VARCHAR(500) NULL
);
ALTER TABLE
    "books" ADD PRIMARY KEY("id");
ALTER TABLE
    "books" ADD CONSTRAINT "books_isbn_unique" UNIQUE("isbn");
CREATE TABLE "loans"(
    "id" bigserial NOT NULL,
    "book_id" BIGINT NOT NULL,
    "borrower" bigserial NOT NULL,
    "loan_date" DATE NOT NULL,
    "due_date" DATE NOT NULL,
    "return_date" DATE NULL,
    "reminder_sent_at" TIMESTAMP(0) WITHOUT TIME ZONE NULL,
    "overdue_notice_sent_at" TIMESTAMP(0) WITHOUT TIME ZONE NULL
);
ALTER TABLE
    "loans" ADD PRIMARY KEY("id");
CREATE INDEX "loans_due_date_index" ON
    "loans"("due_date");
CREATE TABLE "reservations"(
    "id" bigserial NOT NULL,
    "book_id" BIGINT NOT NULL,
    "borrower" bigserial NOT NULL,
    "request_date" TIMESTAMP(0) WITHOUT TIME ZONE NOT NULL,
    "status" VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE'
);
ALTER TABLE
    "reservations" ADD PRIMARY KEY("id");
ALTER TABLE
    "loans" ADD CONSTRAINT "loans_book_id_foreign" FOREIGN KEY("book_id") REFERENCES "books"("id");
ALTER TABLE
    "reservations" ADD CONSTRAINT "reservations_book_id_foreign" FOREIGN KEY("book_id") REFERENCES "books"("id");
ALTER TABLE
    "reservations" ADD CONSTRAINT "reservations_borrower_foreign" FOREIGN KEY("borrower") REFERENCES "app_users"("id");
ALTER TABLE
    "loans" ADD CONSTRAINT "loans_borrower_foreign" FOREIGN KEY("borrower") REFERENCES "app_users"("id");