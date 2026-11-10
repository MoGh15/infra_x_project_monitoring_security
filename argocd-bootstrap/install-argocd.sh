#!/usr/bin/env bash
set -euo pipefail

# =============================================================================
# ArgoCD Installation via Helm (Zwei-Phasen / idempotent)
# -----------------------------------------------------------------------------
# Henne-Ei-Problem: ArgoCD muss existieren, BEVOR das bootstrap-app-of-apps
# angewendet werden kann (das erst ingress-nginx, cert-manager und den OVH-
# ClusterIssuer bereitstellt). Erst danach koennen ArgoCDs Ingress + TLS
# funktionieren.
#
# Dieses Skript erkennt die Phase automatisch und ist beliebig oft ausfuehrbar:
#   Phase 1 (ingress-nginx ODER ClusterIssuer fehlen):
#     -> ArgoCD wird OHNE Ingress installiert (nur per kubectl port-forward
#        erreichbar). Danach das Bootstrap anwenden.
#   Phase 2 (ingress-nginx UND ClusterIssuer vorhanden):
#     -> helm upgrade aktiviert Ingress + TLS. Domain-Record legt external-dns
#        in OVH an, das Zertifikat holt cert-manager via OVH-DNS01-Issuer.
#
# Empfohlener Ablauf beim ersten Einrichten:
#   1) ./install-argocd.sh                      # Phase 1 (core)
#   2) kubectl apply -f app-of-apps.yaml        # bootstrap-app-of-apps
#   3) ./install-argocd.sh                      # Phase 2 (Ingress + TLS)
#   4) kubectl apply -f ../argocd/app-of-apps.yaml   # normaler Stack
# =============================================================================

NAMESPACE="argocd"
RELEASE_NAME="argocd"
DOMAIN="argocd.infra-x.cloud"
# ClusterIssuer, der vom cert-manager-webhook-ovh (bootstrap) erzeugt wird.
CLUSTER_ISSUER="letsencrypt-ovh-prod"
# IngressClass, die ingress-nginx (bootstrap) bereitstellt.
INGRESS_CLASS="nginx"
TLS_SECRET="argocd-tls"

echo "=== ArgoCD Installation via Helm ==="

# Namespace erstellen
kubectl create namespace "${NAMESPACE}" --dry-run=client -o yaml | kubectl apply -f -

# Helm Repo hinzufuegen
helm repo add argo https://argoproj.github.io/argo-helm
helm repo update

# --- Phase automatisch ermitteln ---------------------------------------------
ENABLE_INGRESS="false"
if kubectl get ingressclass "${INGRESS_CLASS}" >/dev/null 2>&1 \
   && kubectl get clusterissuer "${CLUSTER_ISSUER}" >/dev/null 2>&1; then
  ENABLE_INGRESS="true"
fi

COMMON_SET=(
  --set 'configs.params."server\.insecure"=true'
)

if [[ "${ENABLE_INGRESS}" == "true" ]]; then
  echo ">> Phase 2: ingress-nginx + ClusterIssuer '${CLUSTER_ISSUER}' vorhanden -> Ingress + TLS aktivieren."
  # ArgoCD installieren mit Ingress + TLS via OVH-DNS01-ClusterIssuer
  helm upgrade --install "${RELEASE_NAME}" argo/argo-cd \
    --namespace "${NAMESPACE}" \
    "${COMMON_SET[@]}" \
    --set 'server.ingress.enabled=true' \
    --set "server.ingress.hostname=${DOMAIN}" \
    --set "server.ingress.ingressClassName=${INGRESS_CLASS}" \
    --set "server.ingress.annotations.cert-manager\.io/cluster-issuer=${CLUSTER_ISSUER}" \
    --set 'server.ingress.annotations.nginx\.ingress\.kubernetes\.io/ssl-redirect=false' \
    --set 'server.ingress.annotations.nginx\.ingress\.kubernetes\.io/force-ssl-redirect=false' \
    --set 'server.ingress.annotations.nginx\.ingress\.kubernetes\.io/backend-protocol=HTTP' \
    --set 'server.ingress.tls=true' \
    --set "server.ingress.extraTls[0].hosts[0]=${DOMAIN}" \
    --set "server.ingress.extraTls[0].secretName=${TLS_SECRET}" \
    --wait

  echo ""
  echo "=== ArgoCD mit Ingress + TLS installiert ==="
  echo "URL: https://${DOMAIN}"
  echo ""
  echo "DNS-Record (${DOMAIN}) wird von external-dns in OVH angelegt."
  echo "Zertifikat (Secret ${TLS_SECRET}) wird von cert-manager via '${CLUSTER_ISSUER}' ausgestellt."
  echo "Status pruefen:"
  echo "  kubectl -n ${NAMESPACE} get ingress,certificate"
else
  echo ">> Phase 1: ingress-nginx und/oder ClusterIssuer '${CLUSTER_ISSUER}' fehlen noch."
  echo ">> ArgoCD wird OHNE Ingress installiert (per port-forward erreichbar)."
  # ArgoCD installieren OHNE Ingress
  helm upgrade --install "${RELEASE_NAME}" argo/argo-cd \
    --namespace "${NAMESPACE}" \
    "${COMMON_SET[@]}" \
    --set 'server.ingress.enabled=false' \
    --wait

  echo ""
  echo "=== ArgoCD (ohne Ingress) installiert ==="
  echo "Zugriff vorerst per port-forward:"
  echo "  kubectl -n ${NAMESPACE} port-forward svc/${RELEASE_NAME}-server 8080:80"
  echo "  -> http://localhost:8080"
  echo ""
  echo "Naechste Schritte:"
  echo "  1) kubectl apply -f $(dirname "$0")/app-of-apps.yaml   # Bootstrap deployen"
  echo "  2) Warten bis ingress-nginx + ClusterIssuer '${CLUSTER_ISSUER}' Healthy sind"
  echo "  3) Dieses Skript ERNEUT ausfuehren -> aktiviert Ingress + TLS"
fi

echo ""
echo "Admin-Passwort abrufen:"
echo "  kubectl -n ${NAMESPACE} get secret argocd-initial-admin-secret -o jsonpath='{.data.password}' | base64 -d; echo"
