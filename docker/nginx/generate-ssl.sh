#!/bin/bash

# SSL 인증서 생성 스크립트
# Simple LLM Backend용 자체 서명 인증서 생성

set -e

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 로그 함수
log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# SSL 디렉토리 생성
SSL_DIR="./ssl"
if [ ! -d "$SSL_DIR" ]; then
    mkdir -p "$SSL_DIR"
    log_info "Created SSL directory: $SSL_DIR"
fi

# 기존 인증서 확인
if [ -f "$SSL_DIR/server.crt" ] && [ -f "$SSL_DIR/server.key" ]; then
    log_warn "SSL certificates already exist"
    read -p "Do you want to regenerate them? (y/N): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        log_info "Using existing certificates"
        exit 0
    fi
fi

log_info "Generating SSL certificates..."

# OpenSSL 설정 파일 생성
cat > "$SSL_DIR/openssl.conf" << EOF
[req]
default_bits = 2048
prompt = no
default_md = sha256
distinguished_name = dn
req_extensions = v3_req

[dn]
C=KR
ST=Gyeonggi-do
L=Guri-si
O=Simple LLM Backend
OU=Development
CN=localhost

[v3_req]
basicConstraints = CA:FALSE
keyUsage = nonRepudiation, digitalSignature, keyEncipherment
subjectAltName = @alt_names

[alt_names]
DNS.1 = localhost
DNS.2 = simple-llm-backend
DNS.3 = *.simple-llm-backend
IP.1 = 127.0.0.1
IP.2 = ::1
EOF

# 개인키 생성
log_info "Generating private key..."
openssl genrsa -out "$SSL_DIR/server.key" 2048

# CSR 생성
log_info "Generating certificate signing request..."
openssl req -new -key "$SSL_DIR/server.key" -out "$SSL_DIR/server.csr" -config "$SSL_DIR/openssl.conf"

# 자체 서명 인증서 생성
log_info "Generating self-signed certificate..."
openssl x509 -req -in "$SSL_DIR/server.csr" -signkey "$SSL_DIR/server.key" -out "$SSL_DIR/server.crt" \
    -days 365 -extensions v3_req -extfile "$SSL_DIR/openssl.conf"

# 권한 설정
chmod 600 "$SSL_DIR/server.key"
chmod 644 "$SSL_DIR/server.crt"

# 정리
rm "$SSL_DIR/server.csr"
rm "$SSL_DIR/openssl.conf"

log_info "SSL certificates generated successfully!"

# 인증서 정보 출력
log_info "Certificate details:"
openssl x509 -in "$SSL_DIR/server.crt" -text -noout | grep -E "(Subject:|DNS:|IP Address:)"

echo
log_info "Files created:"
echo "  - $SSL_DIR/server.key (private key)"
echo "  - $SSL_DIR/server.crt (certificate)"

echo
log_warn "Note: This is a self-signed certificate for development use only."
log_warn "For production, use certificates from a trusted CA."

echo
log_info "To trust this certificate in your browser:"
echo "  1. Navigate to https://localhost"
echo "  2. Click 'Advanced' on the security warning"
echo "  3. Click 'Proceed to localhost (unsafe)'"
echo "  4. Or add the certificate to your browser's trusted store"

echo
log_info "Certificate generation completed!"