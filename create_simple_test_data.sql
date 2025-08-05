-- ===========================================
-- SCRIPT TẠO DỮ LIỆU TEST ĐỌN GIẢN
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

-- 3. Tạo Users và Tutors
-- Password hash for "password123": $2a$12$LJK6K5V8JQ9yBXGJnWj8bO.Hw2D5JIVYUpTx6fPcKNmLcKo0kGHMy
INSERT INTO users (full_name, email, phone_number, password, avatar_url, enabled, blocked, created_at, updated_at)
VALUES 
('Akiko Tanaka', 'akiko@example.com', '+84123456789', '$2a$12$LJK6K5V8JQ9yBXGJnWj8bO.Hw2D5JIVYUpTx6fPcKNmLcKo0kGHMy', null, true, false, NOW(), NOW()),
('Hiroshi Yamamoto', 'hiroshi@example.com', '+84123456790', '$2a$12$LJK6K5V8JQ9yBXGJnWj8bO.Hw2D5JIVYUpTx6fPcKNmLcKo0kGHMy', null, true, false, NOW(), NOW()),
('Yuki Sato', 'yuki@example.com', '+84123456791', '$2a$12$LJK6K5V8JQ9yBXGJnWj8bO.Hw2D5JIVYUpTx6fPcKNmLcKo0kGHMy', null, true, false, NOW(), NOW()),
('Kenji Watanabe', 'kenji@example.com', '+84123456792', '$2a$12$LJK6K5V8JQ9yBXGJnWj8bO.Hw2D5JIVYUpTx6fPcKNmLcKo0kGHMy', null, true, false, NOW(), NOW()),
('Admin User', 'admin@example.com', '+84123456793', '$2a$12$LJK6K5V8JQ9yBXGJnWj8bO.Hw2D5JIVYUpTx6fPcKNmLcKo0kGHMy', null, true, false, NOW(), NOW()),
('Student Test', 'student@example.com', '+84123456794', '$2a$12$LJK6K5V8JQ9yBXGJnWj8bO.Hw2D5JIVYUpTx6fPcKNmLcKo0kGHMy', null, true, false, NOW(), NOW())
ON CONFLICT (email) DO NOTHING;

-- Tutors table (extends users)
INSERT INTO tutors (user_id, teaching_requirements)
SELECT u.id, 'Giảng viên chuyên về ' || u.full_name
FROM users u 
WHERE u.email IN ('akiko@example.com', 'hiroshi@example.com', 'yuki@example.com', 'kenji@example.com')
AND NOT EXISTS (SELECT 1 FROM tutors t WHERE t.user_id = u.id);

-- Students table (extends users) 
INSERT INTO students (user_id)
SELECT u.id
FROM users u 
WHERE u.email = 'student@example.com'
AND NOT EXISTS (SELECT 1 FROM students s WHERE s.user_id = u.id);

-- Gán roles cho users
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

-- 4. Tạo Courses
INSERT INTO courses (title, description, duration_in_minutes, level_id, lesson_count, course_overview, course_content, price, thumbnail_url, includes_description, count_buy, tutor_id, status, created_at, updated_at)
SELECT 
    'Hiragana hoàn chỉnh - Nền tảng tiếng Nhật',
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
  AND NOT EXISTS (SELECT 1 FROM courses c WHERE c.title LIKE 'Hiragana hoàn chỉnh%');

INSERT INTO courses (title, description, duration_in_minutes, level_id, lesson_count, course_overview, course_content, price, thumbnail_url, includes_description, count_buy, tutor_id, status, created_at, updated_at)
SELECT 
    'Katakana hoàn chỉnh - Mở rộng từ vựng',
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
  AND NOT EXISTS (SELECT 1 FROM courses c WHERE c.title LIKE 'Katakana hoàn chỉnh%');

-- Tạo thêm 3 courses nữa
INSERT INTO courses (title, description, duration_in_minutes, level_id, lesson_count, course_overview, course_content, price, thumbnail_url, includes_description, count_buy, tutor_id, status, created_at, updated_at)
SELECT 
    'Kanji cơ bản - 100 chữ đầu tiên',
    'Học 100 chữ Kanji cơ bản nhất với nhiều cách ghi nhớ hiệu quả',
    1500,
    l.id,
    20,
    '<h3>Tổng quan khóa học</h3><p>• 100 Kanji cơ bản<br/>• Âm On và Kun<br/>• Thứ tự nét<br/>• Ví dụ từ vựng thực tế</p>',
    'Kanji từ cơ bản với phương pháp học tập khoa học',
    399000,
    'https://res.cloudinary.com/dugsysqjv/image/upload/v1/kanji-course.jpg',
    'Khóa học bao gồm video, bài tập thực hành, và chứng chỉ',
    0,
    t.user_id,
    'APPROVED',
    NOW(),
    NOW()
FROM levels l, tutors t, users u
WHERE l.name = 'ELEMENTARY' AND t.user_id = u.id AND u.email = 'yuki@example.com'
  AND NOT EXISTS (SELECT 1 FROM courses c WHERE c.title LIKE 'Kanji cơ bản%');

INSERT INTO courses (title, description, duration_in_minutes, level_id, lesson_count, course_overview, course_content, price, thumbnail_url, includes_description, count_buy, tutor_id, status, created_at, updated_at)
SELECT 
    'Ngữ pháp N5 - Nền tảng giao tiếp',
    'Tất cả ngữ pháp N5 với ví dụ thực tế và bài tập đa dạng',
    1800,
    l.id,
    25,
    '<h3>Tổng quan khóa học</h3><p>• 50+ mẫu câu N5<br/>• Ví dụ thực tế<br/>• Bài tập đa dạng<br/>• Chuẩn bị thi JLPT N5</p>',
    'Ngữ pháp N5 từ cơ bản đến thành thạo',
    499000,
    'https://res.cloudinary.com/dugsysqjv/image/upload/v1/grammar-n5.jpg',
    'Khóa học bao gồm video, bài tập thực hành, và chứng chỉ',
    0,
    t.user_id,
    'APPROVED',
    NOW(),
    NOW()
FROM levels l, tutors t, users u
WHERE l.name = 'ELEMENTARY' AND t.user_id = u.id AND u.email = 'kenji@example.com'
  AND NOT EXISTS (SELECT 1 FROM courses c WHERE c.title LIKE 'Ngữ pháp N5%');

INSERT INTO courses (title, description, duration_in_minutes, level_id, lesson_count, course_overview, course_content, price, thumbnail_url, includes_description, count_buy, tutor_id, status, created_at, updated_at)
SELECT 
    'Từ vựng N5 - 800 từ thiết yếu (MIỄN PHÍ)',
    'Học 800 từ vựng N5 qua ví dụ và bài tập tương tác',
    900,
    l.id,
    10,
    '<h3>Tổng quan khóa học</h3><p>• 800 từ vựng N5<br/>• Phân loại theo chủ đề<br/>• Audio chuẩn<br/>• Bài tập ghi nhớ</p>',
    'Từ vựng N5 được sắp xếp khoa học theo chủ đề',
    0,
    'https://res.cloudinary.com/dugsysqjv/image/upload/v1/vocabulary-n5.jpg',
    'Khóa học miễn phí bao gồm video và bài tập',
    0,
    t.user_id,
    'APPROVED',
    NOW(),
    NOW()
FROM levels l, tutors t, users u
WHERE l.name = 'BEGINNER' AND t.user_id = u.id AND u.email = 'akiko@example.com'
  AND NOT EXISTS (SELECT 1 FROM courses c WHERE c.title LIKE 'Từ vựng N5%');

-- 5. Tạo Modules cho courses
INSERT INTO modules (title, description, duration_in_minutes, course_id, order_index, created_at, updated_at)
SELECT 
    'あ段 Hiragana',
    'Học nhóm ký tự あ, い, う, え, お',
    240,
    c.id,
    1,
    NOW(),
    NOW()
FROM courses c 
WHERE c.title LIKE 'Hiragana hoàn chỉnh%'
  AND NOT EXISTS (SELECT 1 FROM modules m WHERE m.title = 'あ段 Hiragana' AND m.course_id = c.id);

INSERT INTO modules (title, description, duration_in_minutes, course_id, order_index, created_at, updated_at)
SELECT 
    'か段 Hiragana',
    'Học nhóm ký tự か, き, く, け, こ',
    240,
    c.id,
    2,
    NOW(),
    NOW()
FROM courses c 
WHERE c.title LIKE 'Hiragana hoàn chỉnh%'
  AND NOT EXISTS (SELECT 1 FROM modules m WHERE m.title = 'か段 Hiragana' AND m.course_id = c.id);

INSERT INTO modules (title, description, duration_in_minutes, course_id, order_index, created_at, updated_at)
SELECT 
    'ア段 Katakana',
    'Học nhóm ký tự ア, イ, ウ, エ, オ',
    200,
    c.id,
    1,
    NOW(),
    NOW()
FROM courses c 
WHERE c.title LIKE 'Katakana hoàn chỉnh%'
  AND NOT EXISTS (SELECT 1 FROM modules m WHERE m.title = 'ア段 Katakana' AND m.course_id = c.id);

-- 6. Tạo Lessons cho modules
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

-- 7. Tạo Exercises đa dạng
INSERT INTO exercises (title, description, type, lesson_id, target_text, target_audio_url, difficulty_level, speech_recognition_language, minimum_accuracy_score, created_at, updated_at)
SELECT 
    'Điền ký tự あ段',
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
  AND NOT EXISTS (SELECT 1 FROM exercises e WHERE e.title = 'Điền ký tự あ段' AND e.lesson_id = l.id);

INSERT INTO exercises (title, description, type, lesson_id, target_text, target_audio_url, difficulty_level, speech_recognition_language, minimum_accuracy_score, created_at, updated_at)
SELECT 
    'Chọn ký tự đúng',
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
  AND NOT EXISTS (SELECT 1 FROM exercises e WHERE e.title = 'Chọn ký tự đúng' AND e.lesson_id = l.id)
LIMIT 1;

-- 8. Tạo Questions cho exercises
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
WHERE e.type = 'FILL_IN_BLANK' AND e.title LIKE '%あ段%'
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
WHERE e.type = 'MULTIPLE_CHOICE' AND e.title LIKE '%ký tự đúng%'
  AND NOT EXISTS (SELECT 1 FROM questions q WHERE q.content LIKE '%phát âm là "a"%' AND q.exercise_id = e.id);

-- 9. Tạo Options cho multiple choice questions
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

-- 10. Kiểm tra dữ liệu đã tạo
SELECT '✅ Users created: ' || COUNT(*) as result FROM users;
SELECT '✅ Tutors created: ' || COUNT(*) as result FROM tutors;
SELECT '✅ Courses created: ' || COUNT(*) as result FROM courses;
SELECT '✅ Modules created: ' || COUNT(*) as result FROM modules;
SELECT '✅ Lessons created: ' || COUNT(*) as result FROM lessons;
SELECT '✅ Exercises created: ' || COUNT(*) as result FROM exercises;
SELECT '✅ Questions created: ' || COUNT(*) as result FROM questions;
SELECT '✅ Options created: ' || COUNT(*) as result FROM question_options;

SELECT '🎯 Ready to test! Go to frontend and try the courses!' as message; 