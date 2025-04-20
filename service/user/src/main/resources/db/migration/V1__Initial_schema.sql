-- 用户表
DROP TABLE IF EXISTS users CASCADE;
CREATE TABLE users (
                       id CHAR(26) PRIMARY KEY, -- ULID长度固定26字符
                       username VARCHAR(50) UNIQUE NOT NULL,
                       password_hash VARCHAR(100) NOT NULL,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 聊天室表
DROP TABLE IF EXISTS chat_rooms CASCADE;
CREATE TABLE chat_rooms (
                            id CHAR(26) PRIMARY KEY,
                            name VARCHAR(100) NOT NULL,
                            creator_id CHAR(26) NOT NULL REFERENCES users(id), -- 外键约束
                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 消息表
DROP TABLE IF EXISTS messages;
CREATE TABLE messages (
                          id CHAR(26) PRIMARY KEY,
                          content TEXT NOT NULL,
                          user_id CHAR(26) NOT NULL REFERENCES users(id),    -- 外键约束
                          room_id CHAR(26) NOT NULL REFERENCES chat_rooms(id),-- 外键约束
                          sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 用户-聊天室关联表
DROP TABLE IF EXISTS user_chat_rooms;
CREATE TABLE user_chat_rooms (
                                 user_id CHAR(26) REFERENCES users(id),
                                 room_id CHAR(26) REFERENCES chat_rooms(id),
                                 PRIMARY KEY (user_id, room_id)
);

-- 索引优化
CREATE INDEX idx_messages_room_id ON messages(room_id);
CREATE INDEX idx_user_chat_rooms_user ON user_chat_rooms(user_id);