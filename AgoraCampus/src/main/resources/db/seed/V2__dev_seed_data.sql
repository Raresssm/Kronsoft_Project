-- Development seed data. Safe to keep in local/demo databases.

INSERT INTO app_users (keycloak_id, email, username, created_at) VALUES
('00000000-0000-0000-0000-000000000001', 'admin@agora.local', 'admin', NOW() - INTERVAL '12 days'),
('00000000-0000-0000-0000-000000000002', 'demo@agora.local', 'demo', NOW() - INTERVAL '11 days'),
('00000000-0000-0000-0000-000000000003', 'alice@agora.com', 'alice@agora.com', NOW() - INTERVAL '10 days'),
('00000000-0000-0000-0000-000000000004', 'bogdan@agora.com', 'bogdan@agora.com', NOW() - INTERVAL '9 days'),
('00000000-0000-0000-0000-000000000005', 'carmen@agora.com', 'carmen@agora.com', NOW() - INTERVAL '8 days'),
('00000000-0000-0000-0000-000000000006', 'david@agora.com', 'david@agora.com', NOW() - INTERVAL '7 days'),
('00000000-0000-0000-0000-000000000007', 'emma@agora.com', 'emma@agora.com', NOW() - INTERVAL '6 days'),
('00000000-0000-0000-0000-000000000008', 'techlab@agora.com', 'techlab@agora.com', NOW() - INTERVAL '5 days'),
('00000000-0000-0000-0000-000000000009', 'greenteam@agora.com', 'greenteam@agora.com', NOW() - INTERVAL '4 days'),
('00000000-0000-0000-0000-000000000010', 'careers@agora.com', 'careers@agora.com', NOW() - INTERVAL '3 days')
ON CONFLICT DO NOTHING;

INSERT INTO profiles (app_user_id, profile_type, headline, description, location, website, profile_picture, cover_image, updated_at)
SELECT app_user_id, 'INDIVIDUAL', 'Computer Science student', 'Interested in software engineering, campus projects, and collaborative learning.', 'Bucharest', 'https://agora.example/alice', NULL, NULL, NOW()
FROM app_users WHERE email = 'alice@agora.com' AND NOT EXISTS (SELECT 1 FROM profiles WHERE app_user_id = app_users.app_user_id);

INSERT INTO profiles (app_user_id, profile_type, headline, description, location, website, profile_picture, cover_image, updated_at)
SELECT app_user_id, 'INDIVIDUAL', 'Backend developer in training', 'Building Java APIs and learning cloud deployment with campus teams.', 'Cluj-Napoca', 'https://agora.example/bogdan', NULL, NULL, NOW()
FROM app_users WHERE email = 'bogdan@agora.com' AND NOT EXISTS (SELECT 1 FROM profiles WHERE app_user_id = app_users.app_user_id);

INSERT INTO profiles (app_user_id, profile_type, headline, description, location, website, profile_picture, cover_image, updated_at)
SELECT app_user_id, 'INDIVIDUAL', 'UX and product design student', 'Focused on research, prototypes, and accessible product experiences.', 'Iasi', 'https://agora.example/carmen', NULL, NULL, NOW()
FROM app_users WHERE email = 'carmen@agora.com' AND NOT EXISTS (SELECT 1 FROM profiles WHERE app_user_id = app_users.app_user_id);

INSERT INTO profiles (app_user_id, profile_type, headline, description, location, website, profile_picture, cover_image, updated_at)
SELECT app_user_id, 'INDIVIDUAL', 'Data science student', 'Exploring analytics, machine learning, and research projects.', 'Timisoara', 'https://agora.example/david', NULL, NULL, NOW()
FROM app_users WHERE email = 'david@agora.com' AND NOT EXISTS (SELECT 1 FROM profiles WHERE app_user_id = app_users.app_user_id);

INSERT INTO profiles (app_user_id, profile_type, headline, description, location, website, profile_picture, cover_image, updated_at)
SELECT app_user_id, 'INDIVIDUAL', 'Volunteer coordinator', 'Organizing student events, social initiatives, and mentorship activities.', 'Brasov', 'https://agora.example/emma', NULL, NULL, NOW()
FROM app_users WHERE email = 'emma@agora.com' AND NOT EXISTS (SELECT 1 FROM profiles WHERE app_user_id = app_users.app_user_id);

INSERT INTO profiles (app_user_id, profile_type, headline, description, location, website, profile_picture, cover_image, updated_at)
SELECT app_user_id, 'ORGANIZATION', 'Student software lab', 'Tech Lab connects students with practical software projects, workshops, and internships.', 'Bucharest', 'https://techlab.agora.com', NULL, NULL, NOW()
FROM app_users WHERE email = 'techlab@agora.com' AND NOT EXISTS (SELECT 1 FROM profiles WHERE app_user_id = app_users.app_user_id);

INSERT INTO profiles (app_user_id, profile_type, headline, description, location, website, profile_picture, cover_image, updated_at)
SELECT app_user_id, 'ORGANIZATION', 'Sustainability student organization', 'Green Team runs volunteer activities and sustainability campaigns on campus.', 'Cluj-Napoca', 'https://greenteam.agora.com', NULL, NULL, NOW()
FROM app_users WHERE email = 'greenteam@agora.com' AND NOT EXISTS (SELECT 1 FROM profiles WHERE app_user_id = app_users.app_user_id);

INSERT INTO profiles (app_user_id, profile_type, headline, description, location, website, profile_picture, cover_image, updated_at)
SELECT app_user_id, 'ORGANIZATION', 'Career support office', 'Careers Hub publishes internships, competitions, and mentorship opportunities.', 'Remote', 'https://careers.agora.com', NULL, NULL, NOW()
FROM app_users WHERE email = 'careers@agora.com' AND NOT EXISTS (SELECT 1 FROM profiles WHERE app_user_id = app_users.app_user_id);

INSERT INTO individual_profiles (profile_id, first_name, last_name, phone, cv_document)
SELECT p.profile_id, 'Alice', 'Ionescu', '+40720000001', 'https://example.com/cv/alice.pdf'
FROM profiles p JOIN app_users u ON u.app_user_id = p.app_user_id
WHERE u.email = 'alice@agora.com' AND NOT EXISTS (SELECT 1 FROM individual_profiles WHERE profile_id = p.profile_id);

INSERT INTO individual_profiles (profile_id, first_name, last_name, phone, cv_document)
SELECT p.profile_id, 'Bogdan', 'Popescu', '+40720000002', 'https://example.com/cv/bogdan.pdf'
FROM profiles p JOIN app_users u ON u.app_user_id = p.app_user_id
WHERE u.email = 'bogdan@agora.com' AND NOT EXISTS (SELECT 1 FROM individual_profiles WHERE profile_id = p.profile_id);

INSERT INTO individual_profiles (profile_id, first_name, last_name, phone, cv_document)
SELECT p.profile_id, 'Carmen', 'Marin', '+40720000003', 'https://example.com/cv/carmen.pdf'
FROM profiles p JOIN app_users u ON u.app_user_id = p.app_user_id
WHERE u.email = 'carmen@agora.com' AND NOT EXISTS (SELECT 1 FROM individual_profiles WHERE profile_id = p.profile_id);

INSERT INTO individual_profiles (profile_id, first_name, last_name, phone, cv_document)
SELECT p.profile_id, 'David', 'Stan', '+40720000004', 'https://example.com/cv/david.pdf'
FROM profiles p JOIN app_users u ON u.app_user_id = p.app_user_id
WHERE u.email = 'david@agora.com' AND NOT EXISTS (SELECT 1 FROM individual_profiles WHERE profile_id = p.profile_id);

INSERT INTO individual_profiles (profile_id, first_name, last_name, phone, cv_document)
SELECT p.profile_id, 'Emma', 'Dumitrescu', '+40720000005', 'https://example.com/cv/emma.pdf'
FROM profiles p JOIN app_users u ON u.app_user_id = p.app_user_id
WHERE u.email = 'emma@agora.com' AND NOT EXISTS (SELECT 1 FROM individual_profiles WHERE profile_id = p.profile_id);

INSERT INTO organization_profiles (profile_id, organization_name, phone, industry, specialties)
SELECT p.profile_id, 'Tech Lab', '+40730000001', 'Software', 'Java, React, Cloud, Product Engineering'
FROM profiles p JOIN app_users u ON u.app_user_id = p.app_user_id
WHERE u.email = 'techlab@agora.com' AND NOT EXISTS (SELECT 1 FROM organization_profiles WHERE profile_id = p.profile_id);

INSERT INTO organization_profiles (profile_id, organization_name, phone, industry, specialties)
SELECT p.profile_id, 'Green Team', '+40730000002', 'Sustainability', 'Volunteering, Events, Environmental Projects'
FROM profiles p JOIN app_users u ON u.app_user_id = p.app_user_id
WHERE u.email = 'greenteam@agora.com' AND NOT EXISTS (SELECT 1 FROM organization_profiles WHERE profile_id = p.profile_id);

INSERT INTO organization_profiles (profile_id, organization_name, phone, industry, specialties)
SELECT p.profile_id, 'Careers Hub', '+40730000003', 'Education and Careers', 'Internships, Mentorship, Recruiting'
FROM profiles p JOIN app_users u ON u.app_user_id = p.app_user_id
WHERE u.email = 'careers@agora.com' AND NOT EXISTS (SELECT 1 FROM organization_profiles WHERE profile_id = p.profile_id);

INSERT INTO background (individual_profile_id, type, title, description, start_date, end_date, currently_ongoing)
SELECT individual_profile_id, 'EDUCATION', 'Agora University', 'Computer Science bachelor program.', DATE '2023-10-01', NULL, TRUE
FROM individual_profiles ip JOIN profiles p ON p.profile_id = ip.profile_id JOIN app_users u ON u.app_user_id = p.app_user_id
WHERE u.email = 'alice@agora.com';

INSERT INTO posts (post_id, app_user_id, content, media_url, created_at)
SELECT 1001, app_user_id, 'Welcome to Agora Campus. We are looking for students interested in product engineering workshops this month.', NULL, NOW() - INTERVAL '2 days'
FROM app_users WHERE email = 'techlab@agora.com'
ON CONFLICT DO NOTHING;

INSERT INTO posts (post_id, app_user_id, content, media_url, created_at)
SELECT 1002, app_user_id, 'Green Team is preparing a campus cleanup and sustainability fair. Volunteers are welcome.', NULL, NOW() - INTERVAL '1 day'
FROM app_users WHERE email = 'greenteam@agora.com'
ON CONFLICT DO NOTHING;

INSERT INTO posts (post_id, app_user_id, content, media_url, created_at)
SELECT 1003, app_user_id, 'I just joined Agora Campus and I am looking for teammates for a React study project.', NULL, NOW() - INTERVAL '12 hours'
FROM app_users WHERE email = 'alice@agora.com'
ON CONFLICT DO NOTHING;

INSERT INTO opportunities (opportunity_id, posted_by_user_id, organization_profile_id, individual_profile_id, type, title, location, period, description, additional_info, created_at)
SELECT 1001, u.app_user_id, op.organization_profile_id, NULL, 'INTERNSHIP', 'Frontend Internship', 'Bucharest', 'Summer 2026', 'Join Tech Lab for a frontend internship focused on React, accessibility, and product workflows.', 'React, TypeScript, CSS', NOW() - INTERVAL '18 hours'
FROM app_users u JOIN profiles p ON p.app_user_id = u.app_user_id JOIN organization_profiles op ON op.profile_id = p.profile_id
WHERE u.email = 'techlab@agora.com'
ON CONFLICT DO NOTHING;

INSERT INTO internships (opportunity_id, duration, compensation, requirements)
VALUES (1001, '3 months', 'Paid', 'React basics, Git, and willingness to learn')
ON CONFLICT DO NOTHING;

INSERT INTO opportunities (opportunity_id, posted_by_user_id, organization_profile_id, individual_profile_id, type, title, location, period, description, additional_info, created_at)
SELECT 1002, u.app_user_id, op.organization_profile_id, NULL, 'VOLUNTEERING', 'Campus Sustainability Fair Volunteers', 'Cluj-Napoca', 'Next month', 'Help organize booths, guide visitors, and document sustainability ideas from students.', 'Events, communication, sustainability', NOW() - INTERVAL '10 hours'
FROM app_users u JOIN profiles p ON p.app_user_id = u.app_user_id JOIN organization_profiles op ON op.profile_id = p.profile_id
WHERE u.email = 'greenteam@agora.com'
ON CONFLICT DO NOTHING;

INSERT INTO volunteering (opportunity_id, cause, schedule, benefits)
VALUES (1002, 'Sustainability awareness', 'Weekends and event day', 'Certificate and networking')
ON CONFLICT DO NOTHING;

INSERT INTO opportunities (opportunity_id, posted_by_user_id, organization_profile_id, individual_profile_id, type, title, location, period, description, additional_info, created_at)
SELECT 1003, u.app_user_id, op.organization_profile_id, NULL, 'COMPETITION', 'Campus Product Challenge', 'Remote', 'April 2026', 'Build and present a small product prototype that solves a real campus problem.', 'Product thinking, prototyping, teamwork', NOW() - INTERVAL '7 hours'
FROM app_users u JOIN profiles p ON p.app_user_id = u.app_user_id JOIN organization_profiles op ON op.profile_id = p.profile_id
WHERE u.email = 'careers@agora.com'
ON CONFLICT DO NOTHING;

INSERT INTO competitions (opportunity_id, theme, eligibility, prize, deadline)
VALUES (1003, 'Campus life improvement', 'Agora Campus members', 'Mentorship and demo day feature', DATE '2026-04-30')
ON CONFLICT DO NOTHING;

INSERT INTO opportunities (opportunity_id, posted_by_user_id, organization_profile_id, individual_profile_id, type, title, location, period, description, additional_info, created_at)
SELECT 1004, u.app_user_id, NULL, ip.individual_profile_id, 'STUDENT_PROJECT', 'Study Buddy Planner', 'Remote', 'Flexible', 'Student project for a small planner app that helps students form study groups and schedule sessions.', 'React, Java, UI design', NOW() - INTERVAL '5 hours'
FROM app_users u JOIN profiles p ON p.app_user_id = u.app_user_id JOIN individual_profiles ip ON ip.profile_id = p.profile_id
WHERE u.email = 'alice@agora.com'
ON CONFLICT DO NOTHING;

INSERT INTO student_projects (opportunity_id, project_domain, required_skills, team_size)
VALUES (1004, 'Education technology', 'React, Java, PostgreSQL', 4)
ON CONFLICT DO NOTHING;

INSERT INTO opportunity_applications (opportunity_id, applicant_user_id, status, applied_at)
SELECT 1001, app_user_id, 'PENDING', NOW() - INTERVAL '4 hours'
FROM app_users WHERE email = 'bogdan@agora.com'
ON CONFLICT DO NOTHING;

INSERT INTO opportunity_applications (opportunity_id, applicant_user_id, status, applied_at)
SELECT 1001, app_user_id, 'ACCEPTED', NOW() - INTERVAL '3 hours'
FROM app_users WHERE email = 'carmen@agora.com'
ON CONFLICT DO NOTHING;

INSERT INTO opportunity_applications (opportunity_id, applicant_user_id, status, applied_at)
SELECT 1002, app_user_id, 'PENDING', NOW() - INTERVAL '2 hours'
FROM app_users WHERE email = 'emma@agora.com'
ON CONFLICT DO NOTHING;

INSERT INTO connections (requester_user_id, receiver_user_id, status, created_at)
SELECT a.app_user_id, b.app_user_id, 'ACCEPTED', NOW() - INTERVAL '2 days'
FROM app_users a, app_users b
WHERE a.email = 'alice@agora.com' AND b.email = 'bogdan@agora.com'
ON CONFLICT DO NOTHING;

INSERT INTO messages (sender_user_id, receiver_user_id, content, is_read, sent_at)
SELECT a.app_user_id, b.app_user_id, 'Hi Bogdan, do you want to join the study planner project?', FALSE, NOW() - INTERVAL '6 hours'
FROM app_users a, app_users b
WHERE a.email = 'alice@agora.com' AND b.email = 'bogdan@agora.com';

SELECT setval(pg_get_serial_sequence('posts', 'post_id'), GREATEST((SELECT MAX(post_id) FROM posts), 1));
SELECT setval(pg_get_serial_sequence('opportunities', 'opportunity_id'), GREATEST((SELECT MAX(opportunity_id) FROM opportunities), 1));
