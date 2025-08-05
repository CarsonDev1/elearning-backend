-- ===========================================
-- SCRIPT TẠO DỮ LIỆU TEST ĐƯỢC SỬA LỖI
-- ===========================================

-- 1. Tạo Levels (nếu chưa có)
INSERT INTO levels (name, description, created_at, updated_at) 
VALUES 
('BEGINNER', 'Cấp độ mới bắt đầu', NOW(), NOW()),
('ELEMENTARY', 'Cấp độ cơ bản', NOW(), NOW()),
('INTERMEDIATE', 'Cấp độ trung cấp', NOW(), NOW()),
('ADVANCED', 'Cấp độ nâng cao', NOW(), NOW())
ON CONFLICT (name) DO NOTHING;

-- 2. Tạo Roles (nếu chưa có)
INSERT INTO roles (name) 
VALUES 
('ROLE_STUDENT'),
('ROLE_TUTOR'),
('ROLE_ADMIN')
ON CONFLICT (name) DO NOTHING;

-- 3. Tạo Users - Password hash for "password123"
INSERT INTO users (full_name, email, phone_number, password, avatar_url, enabled, blocked, created_at, updated_at)
VALUES 
('Akiko Tanaka', 'akiko@example.com', '+84123456789', '$2a$12$LQiLHXhJhMT1jDOeFYdv5eqjtE5QbgJz5T2JhbkwmJqMk8TjE9CGu', null, true, false, NOW(), NOW()),
('Hiroshi Yamamoto', 'hiroshi@example.com', '+84123456790', '$2a$12$LQiLHXhJhMT1jDOeFYdv5eqjtE5QbgJz5T2JhbkwmJqMk8TjE9CGu', null, true, false, NOW(), NOW()),
('Yuki Sato', 'yuki@example.com', '+84123456791', '$2a$12$LQiLHXhJhMT1jDOeFYdv5eqjtE5QbgJz5T2JhbkwmJqMk8TjE9CGu', null, true, false, NOW(), NOW()),
('Kenji Watanabe', 'kenji@example.com', '+84123456792', '$2a$12$LQiLHXhJhMT1jDOeFYdv5eqjtE5QbgJz5T2JhbkwmJqMk8TjE9CGu', null, true, false, NOW(), NOW()),
('Admin User', 'admin@example.com', '+84123456793', '$2a$12$LQiLHXhJhMT1jDOeFYdv5eqjtE5QbgJz5T2JhbkwmJqMk8TjE9CGu', null, true, false, NOW(), NOW()),
('Student Test', 'student@example.com', '+84123456794', '$2a$12$LQiLHXhJhMT1jDOeFYdv5eqjtE5QbgJz5T2JhbkwmJqMk8TjE9CGu', null, true, false, NOW(), NOW())
ON CONFLICT (email) DO NOTHING;

-- 4. Tạo Tutors (extends users)
INSERT INTO tutors (user_id, teaching_requirements)
SELECT u.id, 'Giảng viên chuyên về ' || u.full_name
FROM users u 
WHERE u.email IN ('akiko@example.com', 'hiroshi@example.com', 'yuki@example.com', 'kenji@example.com')
AND NOT EXISTS (SELECT 1 FROM tutors t WHERE t.user_id = u.id);

-- 5. Gán roles cho users
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, roles r
WHERE u.email IN ('akiko@example.com', 'hiroshi@example.com', 'yuki@example.com', 'kenji@example.com') 
  AND r.name = 'ROLE_TUTOR'
  AND NOT EXISTS (SELECT 1 FROM user_roles ur WHERE ur.user_id = u.id AND ur.role_id = r.id);

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, roles r
WHERE u.email = 'admin@example.com' AND r.name = 'ROLE_ADMIN'
  AND NOT EXISTS (SELECT 1 FROM user_roles ur WHERE ur.user_id = u.id AND ur.role_id = r.id);

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, roles r
WHERE u.email = 'student@example.com' AND r.name = 'ROLE_STUDENT'
  AND NOT EXISTS (SELECT 1 FROM user_roles ur WHERE ur.user_id = u.id AND ur.role_id = r.id);

-- 6. Tạo Courses
INSERT INTO courses (title, description, duration_in_minutes, level_id, lesson_count, course_overview, course_content, price, thumbnail_url, includes_description, count_buy, tutor_id, status, created_at, updated_at)
SELECT 
    'Hiragana hoàn chỉnh - Nền tảng tiếng Nhật TEST',
    'Khóa học toàn diện về hệ chữ Hiragana với nhiều dạng bài tập đa dạng',
    1200,
    l.id,
    15,
    '<h3>Tổng quan khóa học</h3><p>• Học 46 ký tự Hiragana<br/>• Phát âm chuẩn<br/>• Viết đúng nét<br/>• 50+ bài tập tương tác</p>',
    'Nội dung chi tiết về Hiragana từ cơ bản đến nâng cao',
    299000,
    'https://res.cloudinary.com/dugsysqjv/image/upload/v1/hiragana-course.jpg',
    'Khóa học bao gồm video, bài tập thực hành, và chứng chỉ',
    0,
    t.user_id,
    'APPROVED',
    NOW(),
    NOW()
FROM levels l, tutors t, users u
WHERE l.name = 'BEGINNER' AND t.user_id = u.id AND u.email = 'akiko@example.com'
  AND NOT EXISTS (SELECT 1 FROM courses c WHERE c.title LIKE 'Hiragana hoàn chỉnh - Nền tảng tiếng Nhật TEST%');

INSERT INTO courses (title, description, duration_in_minutes, level_id, lesson_count, course_overview, course_content, price, thumbnail_url, includes_description, count_buy, tutor_id, status, created_at, updated_at)
SELECT 
    'Katakana hoàn chỉnh - Mở rộng từ vựng TEST',
    'Học hệ chữ Katakana để đọc từ ngoại lai và mở rộng vốn từ vựng',
    1000,
    l.id,
    12,
    '<h3>Tổng quan khóa học</h3><p>• Học 46 ký tự Katakana<br/>• Từ ngoại lai phổ biến<br/>• Bài tập viết và đọc<br/>• Audio chuẩn từ người bản xứ</p>',
    'Katakana từ cơ bản đến thành thạo với nhiều ví dụ thực tế',
    249000,
    'https://res.cloudinary.com/dugsysqjv/image/upload/v1/katakana-course.jpg',
    'Khóa học bao gồm video, bài tập thực hành, và chứng chỉ',
    0,
    t.user_id,
    'APPROVED',
    NOW(),
    NOW()
FROM levels l, tutors t, users u
WHERE l.name = 'BEGINNER' AND t.user_id = u.id AND u.email = 'hiroshi@example.com'
  AND NOT EXISTS (SELECT 1 FROM courses c WHERE c.title LIKE 'Katakana hoàn chỉnh - Mở rộng từ vựng TEST%');

-- 7. Tạo Modules cho courses (chỉ dùng các columns có sẵn)
INSERT INTO modules (title, duration_in_minutes, course_id, order_index, created_at, updated_at)
SELECT 
    'あ段 Hiragana',
    240,
    c.id,
    1,
    NOW(),
    NOW()
FROM courses c 
WHERE c.title LIKE 'Hiragana hoàn chỉnh - Nền tảng tiếng Nhật TEST%'
  AND NOT EXISTS (SELECT 1 FROM modules m WHERE m.title = 'あ段 Hiragana' AND m.course_id = c.id);

INSERT INTO modules (title, duration_in_minutes, course_id, order_index, created_at, updated_at)
SELECT 
    'か段 Hiragana',
    240,
    c.id,
    2,
    NOW(),
    NOW()
FROM courses c 
WHERE c.title LIKE 'Hiragana hoàn chỉnh - Nền tảng tiếng Nhật TEST%'
  AND NOT EXISTS (SELECT 1 FROM modules m WHERE m.title = 'か段 Hiragana' AND m.course_id = c.id);

INSERT INTO modules (title, duration_in_minutes, course_id, order_index, created_at, updated_at)
SELECT 
    'ア段 Katakana',
    200,
    c.id,
    1,
    NOW(),
    NOW()
FROM courses c 
WHERE c.title LIKE 'Katakana hoàn chỉnh - Mở rộng từ vựng TEST%'
  AND NOT EXISTS (SELECT 1 FROM modules m WHERE m.title = 'ア段 Katakana' AND m.course_id = c.id);

-- 8. Tạo Lessons cho modules
INSERT INTO lessons (title, description, duration_in_minutes, module_id, order_index, video_url, created_at, updated_at)
SELECT 
    'Học ký tự あ段',
    'Viết và phát âm ký tự Hiragana あ段',
    30,
    m.id,
    1,
    'https://www.youtube.com/watch?v=example1',
    NOW(),
    NOW()
FROM modules m 
WHERE m.title = 'あ段 Hiragana'
  AND NOT EXISTS (SELECT 1 FROM lessons l WHERE l.title = 'Học ký tự あ段' AND l.module_id = m.id);

INSERT INTO lessons (title, description, duration_in_minutes, module_id, order_index, video_url, created_at, updated_at)
SELECT 
    'Bài tập điền chỗ trống あ段',
    'Luyện tập điền ký tự Hiragana vào chỗ trống',
    30,
    m.id,
    2,
    'https://www.youtube.com/watch?v=example2',
    NOW(),
    NOW()
FROM modules m 
WHERE m.title = 'あ段 Hiragana'
  AND NOT EXISTS (SELECT 1 FROM lessons l WHERE l.title = 'Bài tập điền chỗ trống あ段' AND l.module_id = m.id);

-- 9. Tạo Exercises
INSERT INTO exercises (title, description, type, lesson_id, target_text, target_audio_url, difficulty_level, speech_recognition_language, minimum_accuracy_score, created_at, updated_at)
SELECT 
    'Điền ký tự あ段 TEST',
    'Điền ký tự Hiragana phù hợp vào chỗ trống',
    'FILL_IN_BLANK',
    l.id,
    null,
    null,
    'BEGINNER',
    'ja-JP',
    70,
    NOW(),
    NOW()
FROM lessons l 
WHERE l.title LIKE '%điền chỗ trống%'
  AND NOT EXISTS (SELECT 1 FROM exercises e WHERE e.title = 'Điền ký tự あ段 TEST' AND e.lesson_id = l.id);

INSERT INTO exercises (title, description, type, lesson_id, target_text, target_audio_url, difficulty_level, speech_recognition_language, minimum_accuracy_score, created_at, updated_at)
SELECT 
    'Chọn ký tự đúng TEST',
    'Chọn ký tự Hiragana phù hợp',
    'MULTIPLE_CHOICE',
    l.id,
    null,
    null,
    'BEGINNER',
    'ja-JP',
    70,
    NOW(),
    NOW()
FROM lessons l 
WHERE l.title LIKE '%あ段%' 
  AND NOT EXISTS (SELECT 1 FROM exercises e WHERE e.title = 'Chọn ký tự đúng TEST' AND e.lesson_id = l.id)
LIMIT 1;

-- 10. Tạo Questions cho exercises
INSERT INTO questions (content, hint, correct_answer, answer_explanation, points, exercise_id, created_at, updated_at)
SELECT 
    'Âm "a" được viết bằng ký tự Hiragana: ____',
    'Đây là ký tự đầu tiên trong bảng Hiragana',
    'あ',
    'あ là ký tự đầu tiên trong hệ chữ Hiragana, phát âm là "a"',
    10,
    e.id,
    NOW(),
    NOW()
FROM exercises e 
WHERE e.type = 'FILL_IN_BLANK' AND e.title LIKE '%あ段 TEST%'
  AND NOT EXISTS (SELECT 1 FROM questions q WHERE q.content LIKE '%Âm "a"%' AND q.exercise_id = e.id);

INSERT INTO questions (content, hint, correct_answer, answer_explanation, points, exercise_id, created_at, updated_at)
SELECT 
    'Ký tự nào phát âm là "a"?',
    'Ký tự đầu tiên trong bảng Hiragana',
    'あ',
    'あ là ký tự phát âm "a"',
    10,
    e.id,
    NOW(),
    NOW()
FROM exercises e 
WHERE e.type = 'MULTIPLE_CHOICE' AND e.title LIKE '%ký tự đúng TEST%'
  AND NOT EXISTS (SELECT 1 FROM questions q WHERE q.content LIKE '%phát âm là "a"%' AND q.exercise_id = e.id);

-- 11. Tạo Options cho multiple choice questions
INSERT INTO question_options (content, correct, question_id, created_at, updated_at)
SELECT 'あ', true, q.id, NOW(), NOW()
FROM questions q 
WHERE q.content LIKE '%phát âm là "a"%'
  AND NOT EXISTS (SELECT 1 FROM question_options o WHERE o.content = 'あ' AND o.question_id = q.id);

INSERT INTO question_options (content, correct, question_id, created_at, updated_at)
SELECT 'い', false, q.id, NOW(), NOW()
FROM questions q 
WHERE q.content LIKE '%phát âm là "a"%'
  AND NOT EXISTS (SELECT 1 FROM question_options o WHERE o.content = 'い' AND o.question_id = q.id);

INSERT INTO question_options (content, correct, question_id, created_at, updated_at)
SELECT 'う', false, q.id, NOW(), NOW()
FROM questions q 
WHERE q.content LIKE '%phát âm là "a"%'
  AND NOT EXISTS (SELECT 1 FROM question_options o WHERE o.content = 'う' AND o.question_id = q.id);

INSERT INTO question_options (content, correct, question_id, created_at, updated_at)
SELECT 'え', false, q.id, NOW(), NOW()
FROM questions q 
WHERE q.content LIKE '%phát âm là "a"%'
  AND NOT EXISTS (SELECT 1 FROM question_options o WHERE o.content = 'え' AND o.question_id = q.id);

-- 12. Kiểm tra dữ liệu đã tạo
SELECT '✅ Users created: ' || COUNT(*) as result FROM users WHERE email LIKE '%@example.com';
SELECT '✅ Tutors created: ' || COUNT(*) as result FROM tutors t JOIN users u ON t.user_id = u.id WHERE u.email LIKE '%@example.com';
SELECT '✅ TEST Courses created: ' || COUNT(*) as result FROM courses WHERE title LIKE '%TEST%';
SELECT '✅ Modules created: ' || COUNT(*) as result FROM modules WHERE title LIKE '%Hiragana' OR title LIKE '%Katakana';
SELECT '✅ Lessons created: ' || COUNT(*) as result FROM lessons WHERE title LIKE '%あ段%';
SELECT '✅ Exercises created: ' || COUNT(*) as result FROM exercises WHERE title LIKE '%TEST%';
SELECT '✅ Questions created: ' || COUNT(*) as result FROM questions q JOIN exercises e ON q.exercise_id = e.id WHERE e.title LIKE '%TEST%';
SELECT '✅ Options created: ' || COUNT(*) as result FROM question_options o JOIN questions q ON o.question_id = q.id JOIN exercises e ON q.exercise_id = e.id WHERE e.title LIKE '%TEST%';

SELECT '🎯 Ready to test! Go to frontend and try the TEST courses!' as message; 