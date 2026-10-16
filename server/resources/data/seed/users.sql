-- Password for all users: Welkom123!
INSERT INTO users (
    id,
    email,
    password_hash
)
VALUES
    (1, 'user1@example.com', '$2a$12$pAUOvoHOgkvdrxtlRvAbUetMhO/kHC8/YhPJzDGO7dMNAt.rCviPa'),
    (2, 'user2@example.com', '$2a$12$pAUOvoHOgkvdrxtlRvAbUetMhO/kHC8/YhPJzDGO7dMNAt.rCviPa'),
    (3, 'user3@example.com', '$2a$12$pAUOvoHOgkvdrxtlRvAbUetMhO/kHC8/YhPJzDGO7dMNAt.rCviPa');
