-- Initialize database with some basic setup
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Grant additional permissions
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO japanese_learning_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO japanese_learning_user;
GRANT ALL PRIVILEGES ON ALL FUNCTIONS IN SCHEMA public TO japanese_learning_user;
