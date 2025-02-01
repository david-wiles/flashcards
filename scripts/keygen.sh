#!/bin/bash

if [ $# -gt 0 ] && [ $# -lt 3 ]; then
    echo "Usage: $0 <key> <public> <private>"
    exit 1
fi

KEY="${1:-rsa.pem}"
PUB="${2:-public.der}"
PRIV="${3:-private.pem}"

openssl genrsa -out $KEY 2048
openssl pkcs8 -topk8 -inform PEM -outform PEM -in $KEY -out $PRIV -nocrypt
openssl rsa -in $KEY -pubout -outform DER -out $PUB