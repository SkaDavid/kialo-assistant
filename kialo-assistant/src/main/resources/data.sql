-- Vypnutí kontrol integrity pro hladké vložení (platí pro H2)
SET REFERENTIAL_INTEGRITY FALSE;

-- Vyčištění tabulek (v opačném pořadí kvůli vazbám)
TRUNCATE TABLE argument;
TRUNCATE TABLE debate;
TRUNCATE TABLE app_users;

-- 1. Uživatelé
INSERT INTO app_users (id, username, keycloak_id) VALUES (1, 'Jan_Novak', 1);
INSERT INTO app_users (id, username, keycloak_id) VALUES (2, 'Marie_Cerna', 2);
INSERT INTO app_users (id, username, keycloak_id) VALUES (3, 'Petr_Svoboda', 3);

-- 2. Jedna hlavní debata
INSERT INTO debate (id, title, user_id) VALUES (1, 'Budoucnost AI v medicíně', 1);

-- 3. Argumenty (Seřazeny tak, aby parent existoval dříve než potomek)
-- Úroveň 0: Teze
INSERT INTO argument (id, text, type, debate_id, user_id, parent_id) VALUES (1, 'Implementace AI do státního zdravotnictví výrazně zlepší diagnostiku a sníží náklady.', 'THESIS', 1, 1, NULL);

-- Úroveň 1: Reakce na Tezi (ID 1)
INSERT INTO argument (id, text, type, debate_id, user_id, parent_id) VALUES (2, 'Algoritmy dokáží analyzovat RTG snímky rychleji a přesněji než člověk.', 'PRO', 1, 2, 1);
INSERT INTO argument (id, text, type, debate_id, user_id, parent_id) VALUES (3, 'Lidský úsudek a empatie jsou při stanovení léčby nenahraditelné.', 'CON', 1, 3, 1);
INSERT INTO argument (id, text, type, debate_id, user_id, parent_id) VALUES (4, 'Hrozí vysoké riziko úniku citlivých pacientských dat při zpracování v cloudu.', 'CON', 1, 2, 1);
INSERT INTO argument (id, text, type, debate_id, user_id, parent_id) VALUES (5, 'AI umožní personalizovanou medicínu šitou na míru genetickému profilu.', 'PRO', 1, 1, 1);
INSERT INTO argument (id, text, type, debate_id, user_id, parent_id) VALUES (18, 'Automatizace administrativy uvolní lékařům ruce pro péči o pacienty.', 'PRO', 1, 1, 1);

-- Úroveň 2: Reakce na diagnostiku (ID 2), bezpečnost (ID 4), genetiku (ID 5), admin (ID 18)
INSERT INTO argument (id, text, type, debate_id, user_id, parent_id) VALUES (6, 'Moderní systémy již dnes vykazují v onkologii vyšší úspěšnost než průměrný lékař.', 'PRO', 1, 1, 2);
INSERT INTO argument (id, text, type, debate_id, user_id, parent_id) VALUES (7, 'Stroje mohou být ovlivněny zkreslením (bias) v trénovacích datech.', 'CON', 1, 3, 2);
INSERT INTO argument (id, text, type, debate_id, user_id, parent_id) VALUES (8, 'Bezpečnostní rizika lze eliminovat použitím lokálních modelů bez cloudu.', 'PRO', 1, 3, 4);
INSERT INTO argument (id, text, type, debate_id, user_id, parent_id) VALUES (14, 'Genetická data mohou být zneužita pojišťovnami k odmítnutí péče.', 'CON', 1, 3, 5);
INSERT INTO argument (id, text, type, debate_id, user_id, parent_id) VALUES (19, 'Lékaři budou trávit čas opravováním chyb v automaticky generovaných zprávách.', 'CON', 1, 2, 18);

-- Úroveň 3: Reakce na ID 6, 8, 14, 19
INSERT INTO argument (id, text, type, debate_id, user_id, parent_id) VALUES (9, 'To platí jen u specifických typů nádorů, obecná diagnostika pokulhává.', 'CON', 1, 2, 6);
INSERT INTO argument (id, text, type, debate_id, user_id, parent_id) VALUES (10, 'Lokální servery v každé nemocnici by byly extrémně drahé na údržbu.', 'CON', 1, 1, 8);
INSERT INTO argument (id, text, type, debate_id, user_id, parent_id) VALUES (15, 'Je nutné zavést přísnou legislativu zakazující diskriminaci na základě DNA.', 'PRO', 1, 2, 14);
INSERT INTO argument (id, text, type, debate_id, user_id, parent_id) VALUES (20, 'Postupem času se chybovost díky samoučení sníží na minimum.', 'PRO', 1, 3, 19);

-- Úroveň 4: Reakce na ID 10, 15
INSERT INTO argument (id, text, type, debate_id, user_id, parent_id) VALUES (11, 'Sdílená krajská datacentra by náklady efektivně rozložila.', 'PRO', 1, 3, 10);
INSERT INTO argument (id, text, type, debate_id, user_id, parent_id) VALUES (16, 'Legislativní proces je příliš pomalý oproti rychlosti vývoje AI.', 'CON', 1, 1, 15);

-- Úroveň 5: Reakce na ID 11, 16
INSERT INTO argument (id, text, type, debate_id, user_id, parent_id) VALUES (12, 'I krajská centra jsou terčem pro ransomware, jak jsme viděli v minulosti.', 'CON', 1, 2, 11);
INSERT INTO argument (id, text, type, debate_id, user_id, parent_id) VALUES (17, 'EU již připravuje AI Act, který tyto oblasti rámcově řeší.', 'PRO', 1, 3, 16);

-- Úroveň 6: Reakce na ID 12
INSERT INTO argument (id, text, type, debate_id, user_id, parent_id) VALUES (13, 'Existují protokoly Air-gap, které fyzicky izolují zálohy od sítě.', 'PRO', 1, 1, 12);

-- Reset sekvencí
ALTER TABLE app_users ALTER COLUMN id RESTART WITH 4;
ALTER TABLE debate ALTER COLUMN id RESTART WITH 2;
ALTER TABLE argument ALTER COLUMN id RESTART WITH 21;

-- Opětovné zapnutí kontrol
SET REFERENTIAL_INTEGRITY TRUE;