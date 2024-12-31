create table users
(
    id uuid not null
        constraint users_pk
            primary key,
    username varchar(64) not null
        constraint users_pk2
            unique,
    pass_hash varchar(32) not null
);

create table flashcards
(
    id uuid not null
        constraint flashcards_pk
            primary key,
    owner_id uuid not null
        constraint flashcards_users_id_fk
            references users,
    content text not null, -- HTML content which may be displayed on the front-end
    question_text varchar(1024) not null,
    answer_text varchar(256) not null,
    categories varchar(64)[] not null,
    created_at timestamp not null,
    modified_at timestamp not null
);

create table flashcard_reviews
(
    id uuid not null
        constraint flashcard_reviews_pk
            primary key,
    flashcard_id uuid not null
        constraint flashcard_reviews_flashcard_id_fk
            references flashcards,
    reviewer_id uuid not null
        constraint flashcard_reviews_reviewer_id_fk
            references users,
    box int not null,
    last_review timestamp not null
);

create index flashcard_reviews_user_id_box_last_review_ix
    on flashcard_reviews (reviewer_id, box, last_review);

