-- Disable integrity checks for smooth insertion (applies to H2)
SET REFERENTIAL_INTEGRITY FALSE;

-- Cleaning tables (in reverse order due to constraints)
TRUNCATE TABLE argument;
TRUNCATE TABLE debate;
TRUNCATE TABLE app_users;

-- 1. Users
INSERT INTO app_users (id, username, keycloak_id) VALUES (1, 'Jan_Novak', 1);
INSERT INTO app_users (id, username, keycloak_id) VALUES (2, 'Marie_Cerna', 2);
INSERT INTO app_users (id, username, keycloak_id) VALUES (3, 'Petr_Svoboda', 3);

-- 2. One main debate
INSERT INTO debate (id, topic, user_id, visibility) VALUES (1, 'The future of AI in medicine', 1, 'PUBLIC');
INSERT INTO debate (id, topic, user_id, visibility) VALUES (2, 'The Future', 2, 'PRIVATE');
INSERT INTO debate (id, topic, user_id, visibility) VALUES (3, 'The future of AI', 3, 'PUBLIC');

-- 3. Arguments (Ordered so that parent exists before child)
-- Level 0: Thesis
INSERT INTO argument (id, text, type, debate_id, user_id, parent_id) VALUES (1, 'Implementing AI into the public healthcare system will significantly improve diagnostics and reduce costs.', 'THESIS', 1, 1, NULL);

-- Level 1: Reactions to Thesis (ID 1)
INSERT INTO argument (id, text, type, debate_id, user_id, parent_id) VALUES (2, 'Algorithms can analyze X-ray images faster and more accurately than humans.', 'PRO', 1, 2, 1);
INSERT INTO argument (id, text, type, debate_id, user_id, parent_id) VALUES (3, 'Human judgment and empathy are irreplaceable when determining treatment.', 'CON', 1, 3, 1);
INSERT INTO argument (id, text, type, debate_id, user_id, parent_id) VALUES (4, 'There is a high risk of sensitive patient data leakage during cloud processing.', 'CON', 1, 2, 1);
INSERT INTO argument (id, text, type, debate_id, user_id, parent_id) VALUES (5, 'AI will enable personalized medicine tailored to an individual’s genetic profile.', 'PRO', 1, 1, 1);
INSERT INTO argument (id, text, type, debate_id, user_id, parent_id) VALUES (18, 'Automating administration will free up doctors’ hands for patient care.', 'PRO', 1, 1, 1);

-- Level 2: Reactions to diagnostics (ID 2), security (ID 4), genetics (ID 5), admin (ID 18)
INSERT INTO argument (id, text, type, debate_id, user_id, parent_id) VALUES (6, 'Modern systems already show higher success rates in oncology than the average doctor.', 'PRO', 1, 1, 2);
INSERT INTO argument (id, text, type, debate_id, user_id, parent_id) VALUES (7, 'Machines can be affected by bias in training data.', 'CON', 1, 3, 2);
INSERT INTO argument (id, text, type, debate_id, user_id, parent_id) VALUES (8, 'Security risks can be eliminated by using local models without the cloud.', 'PRO', 1, 3, 4);
INSERT INTO argument (id, text, type, debate_id, user_id, parent_id) VALUES (14, 'Genetic data could be misused by insurance companies to deny coverage.', 'CON', 1, 3, 5);
INSERT INTO argument (id, text, type, debate_id, user_id, parent_id) VALUES (19, 'Doctors will spend time correcting errors in automatically generated reports.', 'CON', 1, 2, 18);

-- Level 3: Reactions to ID 6, 8, 14, 19
INSERT INTO argument (id, text, type, debate_id, user_id, parent_id) VALUES (9, 'This only applies to specific types of tumors; general diagnostics still lag behind.', 'CON', 1, 2, 6);
INSERT INTO argument (id, text, type, debate_id, user_id, parent_id) VALUES (10, 'Local servers in every hospital would be extremely expensive to maintain.', 'CON', 1, 1, 8);
INSERT INTO argument (id, text, type, debate_id, user_id, parent_id) VALUES (15, 'It is necessary to introduce strict legislation prohibiting discrimination based on DNA.', 'PRO', 1, 2, 14);
INSERT INTO argument (id, text, type, debate_id, user_id, parent_id) VALUES (20, 'Over time, the error rate will decrease to a minimum thanks to self-learning.', 'PRO', 1, 3, 19);

-- Level 4: Reactions to ID 10, 15
INSERT INTO argument (id, text, type, debate_id, user_id, parent_id) VALUES (11, 'Shared regional data centers would effectively distribute the costs.', 'PRO', 1, 3, 10);
INSERT INTO argument (id, text, type, debate_id, user_id, parent_id) VALUES (16, 'The legislative process is too slow compared to the speed of AI development.', 'CON', 1, 1, 15);

-- Level 5: Reactions to ID 11, 16
INSERT INTO argument (id, text, type, debate_id, user_id, parent_id) VALUES (12, 'Even regional centers are targets for ransomware, as we have seen in the past.', 'CON', 1, 2, 11);
INSERT INTO argument (id, text, type, debate_id, user_id, parent_id) VALUES (17, 'The EU is already preparing the AI Act, which addresses these areas in a framework.', 'PRO', 1, 3, 16);

-- Level 6: Reaction to ID 12
INSERT INTO argument (id, text, type, debate_id, user_id, parent_id) VALUES (13, 'Air-gap protocols exist that physically isolate backups from the network.', 'PRO', 1, 1, 12);

-- Reset sequences
ALTER TABLE app_users ALTER COLUMN id RESTART WITH 4;
ALTER TABLE debate ALTER COLUMN id RESTART WITH 4;
ALTER TABLE argument ALTER COLUMN id RESTART WITH 21;

-- Re-enable integrity checks
SET REFERENTIAL_INTEGRITY TRUE;