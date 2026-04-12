#!/bin/bash

# 1. Cargar variables
if [ -f .env ]; then
    export $(grep -v '^#' .env | xargs)
    echo "✅ [NAMI] Logística cargada. Iniciando protocolo Base64."
else
    echo "❌ [NAMI] ¡Sin .env no hay rumbo!"
    exit 1
fi

# 2. Coordenadas Maestras
COMPARTMENT_ID="ocid1.compartment.oc1..aaaaaaaaaktyfppyvz7kztiymghcyqw4ewc5tlprxmabacjeuu7m44kart6q"
SUBNET_ID="ocid1.subnet.oc1.iad.aaaaaaaavoo3t574vgln4qvv6blagnt3qzexwja7k22ebewpalzup7ygny4q"
AD_NAME="UQXG:US-ASHBURN-AD-1"
IMAGE="iad.ocir.io/idv1cxg6und8/mobi-auth:v1.10"
USERNAME="idv1cxg6und8/mobi-sandbox-dev-idd/carlos@wearemobi.com"
# Nota de Robin: Mantenemos el URL público pero con la ACL ya abierta en la DB funcionará.
DB_URL="jdbc:oracle:thin:@(description=(retry_count=20)(retry_delay=3)(address=(protocol=tcps)(port=1522)(host=adb.us-ashburn-1.oraclecloud.com))(connect_data=(service_name=g723917ed8eefe4_mobisandboxoradb_high.adb.oraclecloud.com))(security=(ssl_server_dn_match=yes)))"

echo "📝 [ROBIN] Codificando credenciales y sellando el Manifiesto..."

# 3. Constructor Python de Alta Precisión (Ajustado para inyectar el Password correctamente)
python3 <<EOF
import json
import os
import base64

def to_b64(text):
    if not text: return ""
    return base64.b64encode(text.encode('utf-8')).decode('utf-8')

full_request = {
    "compartmentId": "$COMPARTMENT_ID",
    "availabilityDomain": "$AD_NAME",
    "displayName": "mobi-auth-sentinel-v1.10",
    "shape": "CI.Standard.A1.Flex",
    "shapeConfig": {
        "ocpus": 1.0,
        "memoryInGBs": 4.0
    },
    "containers": [
        {
            "displayName": "mobi-auth-container",
            "imageUrl": "$IMAGE",
            "environmentVariables": {
                "MOBI_JWT_SECRET": os.getenv("MOBI_JWT_SECRET"),
                "MOBI_JWT_EXPIRATION_HOURS": os.getenv("MOBI_JWT_EXPIRATION_HOURS"),
                "MOBI_AUTH_CLIENT_ID": os.getenv("MOBI_AUTH_CLIENT_ID"),
                "MOBI_AUTH_CLIENT_SECRET": os.getenv("MOBI_AUTH_CLIENT_SECRET"),
                "MOBI_M2M_CLIENT_ID": os.getenv("MOBI_M2M_CLIENT_ID"),
                "MOBI_M2M_CLIENT_SECRET": os.getenv("MOBI_M2M_CLIENT_SECRET"),
                "MOBI_OCI_ISSUER_URI": os.getenv("MOBI_OCI_ISSUER_URI"),
                "MOBI_OCI_SCIM_URL": os.getenv("MOBI_OCI_SCIM_URL"),
                "MOBI_OCI_TOKEN_URI": os.getenv("MOBI_OCI_TOKEN_URI"),
                "MOBI_AUTH_DB_NAME": os.getenv("MOBI_AUTH_DB_NAME"),
                "MOBI_AUTH_DB_USER": os.getenv("MOBI_AUTH_DB_USER"),
                "MOBI_AUTH_DB_PASS": os.getenv("MOBI_AUTH_DB_PASS"),
                "CLOUDFLARE_API_TOKEN": os.getenv("CLOUDFLARE_API_TOKEN"),
                "CLOUDFLARE_ACCOUNT_ID": os.getenv("CLOUDFLARE_ACCOUNT_ID"),
                "CLOUDFLARE_KV_NAMESPACE_ID": os.getenv("CLOUDFLARE_KV_NAMESPACE_ID"),
                "SPRING_DATASOURCE_URL": "$DB_URL",
                "SPRING_DATASOURCE_USERNAME": "mobi",
                "SPRING_DATASOURCE_PASSWORD": os.getenv("MOBI_AUTH_DB_PASS"),
                "SPRING_PROFILES_ACTIVE": "oci",
                "SPRING_MVC_RELATIVE_REDIRECTS": "true",
                "SERVER_PORT": "9090",
                "SERVER_FORWARD_HEADERS_STRATEGY": "framework",
                "SERVER_TOMCAT_REMOTEIP_INTERNAL_PROXIES": ".*"
            }
        }
    ],
    "vnics": [
        {
            "subnetId": "$SUBNET_ID",
            "isPublicIpAssigned": False,
            "privateIp": "10.0.1.232"
        }
    ],
    "imagePullSecrets": [
        {
            "registryEndpoint": "iad.ocir.io",
            "secretType": "BASIC",
            "username": to_b64("$USERNAME"),
            "password": to_b64(os.getenv("OCI_AUTH_TOKEN"))
        }
    ]
}

with open('mobi_full_request.json', 'w') as f:
    json.dump(full_request, f)
EOF

echo "🚀 [FRANKY] ¡LANZAMIENTO NUCLEAR v3 (Ampere 1/4 - Base64 Shield)!"
# Lanzamos el comando y capturamos el resultado
oci container-instances container-instance create --from-json file://mobi_full_request.json
