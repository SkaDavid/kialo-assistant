-- 1. Vytvoření uživatelů (Tabulka app_users dle @Table(name="app_users"))
INSERT INTO app_users (id, username) VALUES (1, 'Jan_Novak');
INSERT INTO app_users (id, username) VALUES (2, 'Marie_Cerna');
INSERT INTO app_users (id, username) VALUES (3, 'Petr_Svoboda');

-- 2. Vytvoření debat (Tabulka Debate, vlastník namapován na user_id)
INSERT INTO debate (id, title, user_id) VALUES (1, 'Budoucnost AI v medicíně', 1);
INSERT INTO debate (id, title, user_id) VALUES (2, 'Výhody a nevýhody home office', 2);

-- 3. Argumenty pro Debatu 1 (AI v medicíně)
-- ArgumentType: THESIS, PRO, CON (uloženo jako STRING dle @Enumerated)
INSERT INTO argument (id, text, type, debate_id, user_id, parent_id)
VALUES (1, 'AI výrazně zlepší diagnostiku vzácných onemocnění.', 'THESIS', 1, 1, NULL);

INSERT INTO argument (id, text, type, debate_id, user_id, parent_id)
VALUES (2, 'Algoritmy dokáží analyzovat RTG snímky rychleji a přesněji než člověk.', 'PRO', 1, 2, 1);

INSERT INTO argument (id, text, type, debate_id, user_id, parent_id)
VALUES (3, 'Lidský úsudek a empatie jsou při stanovení léčby nenahraditelné.', 'CON', 1, 3, 1);

INSERT INTO argument (id, text, type, debate_id, user_id, parent_id)
VALUES (4, 'Moderní systémy již dnes vykazují v onkologii vyšší úspěšnost než průměrný lékař.', 'PRO', 1, 1, 2);

INSERT INTO argument (id, text, type, debate_id, user_id, parent_id)
VALUES (5, 'Existuje vysoké riziko úniku citlivých pacientských dat při zpracování v cloudu.', 'CON', 1, 2, 1);

INSERT INTO argument (id, text, type, debate_id, user_id, parent_id)
VALUES (6, 'Bezpečnostní rizika lze eliminovat použitím lokálních modelů a šifrování.', 'PRO', 1, 3, 5);

-- 4. Argumenty pro Debatu 2 (Home office)
INSERT INTO argument (id, text, type, debate_id, user_id, parent_id)
VALUES (7, 'Práce z domova prokazatelně zvyšuje celkovou produktivitu týmu.', 'THESIS', 2, 2, NULL);

INSERT INTO argument (id, text, type, debate_id, user_id, parent_id)
VALUES (8, 'Zaměstnanci šetří čas a energii, kterou by jinak ztratili dojížděním.', 'PRO', 2, 1, 7);

INSERT INTO argument (id, text, type, debate_id, user_id, parent_id)
VALUES (9, 'Dlouhodobý home office vede k sociální izolaci a ztrátě firemní kultury.', 'CON', 2, 3, 7);

INSERT INTO argument (id, text, type, debate_id, user_id, parent_id)
VALUES (10, 'Mnoho lidí nemá doma vhodné pracovní podmínky a klid na soustředění.', 'CON', 2, 2, 7);

INSERT INTO argument (id, text, type, debate_id, user_id, parent_id)
VALUES (11, 'Absence dojíždění snižuje hladinu stresu a zlepšuje work-life balance.', 'PRO', 2, 1, 8);

INSERT INTO argument (id, text, type, debate_id, user_id, parent_id)
VALUES (12, 'Firmy mohou díky HO výrazně ušetřit na nákladech za pronájem kanceláří.', 'PRO', 2, 3, 7);


ALTER TABLE app_users ALTER COLUMN id RESTART WITH 4;
ALTER TABLE debate ALTER COLUMN id RESTART WITH 3;
ALTER TABLE argument ALTER COLUMN id RESTART WITH 13;