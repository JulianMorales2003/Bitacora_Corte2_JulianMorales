#!/usr/bin/env bash
# Genera un certificado AUTOFIRMADO para desarrollo (HTTPS en https://localhost:8443).
# En produccion se usa un certificado real (Let's Encrypt o el de Azure).
set -euo pipefail

if [ -z "${SSL_KEYSTORE_PASSWORD:-}" ]; then
  echo "Define la clave primero:  export SSL_KEYSTORE_PASSWORD='<una-clave>'" >&2
  exit 1
fi

keytool -genkeypair -alias restaurante -keyalg RSA -keysize 2048 \
  -storetype PKCS12 -keystore src/main/resources/restaurante.p12 \
  -validity 365 \
  -storepass "$SSL_KEYSTORE_PASSWORD" -keypass "$SSL_KEYSTORE_PASSWORD" \
  -dname "CN=localhost, OU=Dev, O=LaBrasaViva, L=Bogota, S=DC, C=CO"

echo "Certificado creado en src/main/resources/restaurante.p12 (esta ignorado por Git)."
