-- 개발환경용 테스트 데이터

-- 샘플 로그 데이터 (개발 테스트용)
INSERT INTO LLM_LOGS (ENGINE, PROMPT, RESPONSE, RESPONSE_TIME_MS, SUCCESS) VALUES
('vllm', 'Hello, world!', 'Hello! How can I help you today?', 1250, TRUE),
('sglang', 'What is AI?', 'Artificial Intelligence is a field of computer science...', 1800, TRUE),
('vllm', 'Explain machine learning', 'Machine learning is a subset of AI that enables...', 2100, TRUE);

-- 시스템 메트릭 샘플 데이터
INSERT INTO SYSTEM_METRICS (METRIC_NAME, METRIC_VALUE, METRIC_UNIT) VALUES
('cpu_usage', 45.2, 'percent'),
('memory_usage', 1024.5, 'MB'),
('disk_usage', 75.8, 'percent'),
('network_throughput', 125.6, 'Mbps');