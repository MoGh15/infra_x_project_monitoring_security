#!/usr/bin/env bash
#
# deployment.sh
# -------------
# Deployt bzw. synct eine bestimmte ArgoCD-Application auf einem Cluster aus dem
# aktuell ausgecheckten (und nach origin gepushten) Git-Branch heraus. Gedacht
# für Entwickler, die ihr Feature kurz auf einem Cluster deployen und testen.
#
# Es wird AUSSCHLIESSLICH kubectl verwendet (keine argocd-CLI nötig):
#   - targetRevision der Application wird per kubectl patch auf den Branch gesetzt
#   - ein Sync wird über das .operation-Feld der Application ausgelöst
#   - auf Synced/Healthy wird über den Application-Status gepollt
#
# Die Application-Definitionen stammen aus diesem Repo:
#   - --typ bootstrap -> argocd-bootstrap/apps  (parent app: bootstrap-app-of-apps)
#   - --typ normal    -> argocd/apps            (parent app: app-of-apps)
#
# Benötigt: kubectl, git, jq
#
# Beispiele:
#   scripte/deployment.sh --typ normal --application grafana --environment dev-cluster
#   scripte/deployment.sh -t bootstrap -a cert-manager -e staging-cluster
#
set -euo pipefail

# --- Defaults -----------------------------------------------------------------
REPO_URL_DEFAULT="https://github.com/MoGh15/infra_x_project_monitoring_security.git"
ARGOCD_NAMESPACE="argocd"
SYNC_TIMEOUT="300"

TYPE=""
APPLICATION=""
ENVIRONMENT=""
REPO_URL="${REPO_URL_DEFAULT}"
DO_SYNC="true"
DO_WAIT="true"

# --- Logging ------------------------------------------------------------------
c_reset=$'\033[0m'; c_red=$'\033[31m'; c_grn=$'\033[32m'; c_ylw=$'\033[33m'; c_blu=$'\033[34m'
log()  { printf '%s[deploy]%s %s\n' "${c_blu}" "${c_reset}" "$*"; }
ok()   { printf '%s[ ok ]%s %s\n'  "${c_grn}" "${c_reset}" "$*"; }
warn() { printf '%s[warn]%s %s\n'  "${c_ylw}" "${c_reset}" "$*" >&2; }
die()  { printf '%s[fail]%s %s\n'  "${c_red}" "${c_reset}" "$*" >&2; exit 1; }

usage() {
  cat <<'EOF'
Usage: deployment.sh --typ <bootstrap|normal> --application <app-name> --environment <cluster-context> [options]

Deployt die gewählte ArgoCD-Application aus dem AKTUELL ausgecheckten Git-Branch.

Required:
  -t, --typ          Art der App-Quelle: "bootstrap" oder "normal".
  -a, --application  Name der ArgoCD-Application, die deployt/gesynct wird
                     (z.B. grafana, cert-manager, praxis-form, app-of-apps).
  -e, --environment  Cluster-Context (kubectl context), auf dem deployt wird.

Optional:
      --repo         Git-Repo-URL der Quelle. Default:
                     https://github.com/MoGh15/infra_x_project_monitoring_security.git
      --namespace    Namespace der ArgoCD-Applications. Default: argocd.
      --timeout      Timeout (Sekunden) fürs Warten auf Synced/Healthy. Default: 300.
      --no-sync      Nur targetRevision setzen, keinen Sync auslösen.
      --no-wait      Nach dem Sync nicht auf Synced/Healthy warten.
  -h, --help         Diese Hilfe anzeigen.

Der Branch ist immer der aktuell ausgecheckte Branch und muss nach origin
gepusht sein.
EOF
}

# --- Argumente parsen ---------------------------------------------------------
while [[ $# -gt 0 ]]; do
  case "$1" in
    -t|--typ)         TYPE="${2:-}"; shift 2 ;;
    -a|--application) APPLICATION="${2:-}"; shift 2 ;;
    -e|--environment) ENVIRONMENT="${2:-}"; shift 2 ;;
    --repo)           REPO_URL="${2:-}"; shift 2 ;;
    --namespace)      ARGOCD_NAMESPACE="${2:-}"; shift 2 ;;
    --timeout)        SYNC_TIMEOUT="${2:-}"; shift 2 ;;
    --no-sync)        DO_SYNC="false"; shift ;;
    --no-wait)        DO_WAIT="false"; shift ;;
    -h|--help)        usage; exit 0 ;;
    *) die "Unbekannte Option: $1 (siehe --help)" ;;
  esac
done

# --- Validierung --------------------------------------------------------------
[[ -n "$TYPE" ]]        || { usage; die "--typ ist erforderlich."; }
[[ -n "$APPLICATION" ]] || { usage; die "--application ist erforderlich."; }
[[ -n "$ENVIRONMENT" ]] || { usage; die "--environment ist erforderlich."; }

case "$TYPE" in
  bootstrap|normal) ;;
  *) die "--typ muss 'bootstrap' oder 'normal' sein (war: '$TYPE')." ;;
esac

for bin in kubectl git jq; do
  command -v "$bin" >/dev/null 2>&1 || die "'$bin' nicht gefunden. Bitte installieren."
done

# --- Aktuell ausgecheckten Branch ermitteln & prüfen (muss gepusht sein) ------
BRANCH="$(git rev-parse --abbrev-ref HEAD 2>/dev/null || true)"
[[ -n "$BRANCH" && "$BRANCH" != "HEAD" ]] \
  || die "Aktueller Branch konnte nicht ermittelt werden (kein Git-Repo oder detached HEAD)."
log "Aktuell ausgecheckter Branch: ${BRANCH}"

log "Prüfe, ob Branch '${BRANCH}' auf origin existiert (gepusht) ..."
git ls-remote --exit-code --heads origin "refs/heads/${BRANCH}" >/dev/null 2>&1 \
  || die "Branch '${BRANCH}' wurde nicht nach origin gepusht. Bitte erst 'git push origin ${BRANCH}'."
ok "Branch '${BRANCH}' ist auf origin vorhanden."

# --- Cluster-Context setzen ---------------------------------------------------
log "Wechsle kubectl-Context auf '${ENVIRONMENT}' ..."
kubectl config get-contexts -o name 2>/dev/null | grep -qx "$ENVIRONMENT" \
  || die "kubectl-Context '${ENVIRONMENT}' existiert nicht. Verfügbar:\n$(kubectl config get-contexts -o name)"
kubectl config use-context "$ENVIRONMENT" >/dev/null
kubectl cluster-info >/dev/null 2>&1 || die "Cluster über Context '${ENVIRONMENT}' nicht erreichbar."
ok "Context '${ENVIRONMENT}' aktiv."

NS="$ARGOCD_NAMESPACE"
PARENT_APP=$([[ "$TYPE" == "bootstrap" ]] && echo "bootstrap-app-of-apps" || echo "app-of-apps")
log "Typ '${TYPE}' (parent app-of-apps: ${PARENT_APP}), Ziel-Application: '${APPLICATION}'."

# --- Application existiert? ---------------------------------------------------
APP_JSON="$(kubectl get application "$APPLICATION" -n "$NS" -o json 2>/dev/null)" \
  || die "Application '${APPLICATION}' nicht gefunden (namespace ${NS}). Ist der ${PARENT_APP} bereits deployt?"

# --- targetRevision auf den Branch setzen (per kubectl patch) -----------------
norm_repo="${REPO_URL%.git}"
if [[ "$(echo "$APP_JSON" | jq -r 'has("spec") and (.spec | has("sources"))')" == "true" ]]; then
  # Multi-Source: Index(e) aller Git-Quellen finden, die auf unser Repo zeigen (0-basiert).
  mapfile -t indices < <(echo "$APP_JSON" | jq -r --arg url "$norm_repo" \
    '.spec.sources | to_entries[] | select((.value.repoURL // "" | sub("\\.git$";"")) == $url) | .key')
  [[ "${#indices[@]}" -gt 0 ]] || die "Keine Git-Quelle mit Repo '${REPO_URL}' in '${APPLICATION}' gefunden."
  for idx in "${indices[@]}"; do
    log "Setze Revision der Git-Quelle (sources[${idx}]) auf '${BRANCH}' ..."
    kubectl patch application "$APPLICATION" -n "$NS" --type json \
      -p "[{\"op\":\"replace\",\"path\":\"/spec/sources/${idx}/targetRevision\",\"value\":\"${BRANCH}\"}]" >/dev/null
  done
else
  log "Setze Revision (single source) auf '${BRANCH}' ..."
  kubectl patch application "$APPLICATION" -n "$NS" --type merge \
    -p "{\"spec\":{\"source\":{\"targetRevision\":\"${BRANCH}\"}}}" >/dev/null
fi
ok "targetRevision von '${APPLICATION}' auf '${BRANCH}' gesetzt."

# Hinweis: Child-Apps werden von ${PARENT_APP} verwaltet (selfHeal). Will man die
# gesamte Stack-Definition aus dem Branch testen, die --application ${PARENT_APP}
# wählen, damit die Branch-Revision nicht vom Parent zurückgesetzt wird.
if [[ "$APPLICATION" != "$PARENT_APP" ]]; then
  warn "'${APPLICATION}' wird von '${PARENT_APP}' verwaltet; selfHeal kann die Revision"
  warn "zurücksetzen. Für Stack-weites Branch-Testing --application ${PARENT_APP} nutzen."
fi

# --- Sync auslösen (per kubectl: refresh + .operation) ------------------------
if [[ "$DO_SYNC" == "true" ]]; then
  log "Erzwinge Refresh der Manifeste aus Git ..."
  kubectl annotate application "$APPLICATION" -n "$NS" \
    argocd.argoproj.io/refresh=hard --overwrite >/dev/null

  log "Löse Sync aus ..."
  kubectl patch application "$APPLICATION" -n "$NS" --type merge -p \
    "{\"operation\":{\"initiatedBy\":{\"username\":\"deployment.sh\"},\"info\":[{\"name\":\"Reason\",\"value\":\"dev branch deploy: ${BRANCH}\"}],\"sync\":{\"syncOptions\":[\"CreateNamespace=true\"]}}}" >/dev/null
  ok "Sync-Operation für '${APPLICATION}' gestartet."

  if [[ "$DO_WAIT" == "true" ]]; then
    log "Warte auf Synced/Healthy (Timeout ${SYNC_TIMEOUT}s) ..."
    deadline=$(( SECONDS + SYNC_TIMEOUT ))
    while (( SECONDS < deadline )); do
      sync_status="$(kubectl get application "$APPLICATION" -n "$NS" -o jsonpath='{.status.sync.status}' 2>/dev/null || true)"
      health_status="$(kubectl get application "$APPLICATION" -n "$NS" -o jsonpath='{.status.health.status}' 2>/dev/null || true)"
      op_phase="$(kubectl get application "$APPLICATION" -n "$NS" -o jsonpath='{.status.operationState.phase}' 2>/dev/null || true)"
      if [[ "$sync_status" == "Synced" && "$health_status" == "Healthy" ]]; then
        ok "'${APPLICATION}' ist Synced & Healthy."
        break
      fi
      if [[ "$op_phase" == "Failed" || "$op_phase" == "Error" ]]; then
        die "Sync-Operation fehlgeschlagen (phase=${op_phase}, sync=${sync_status}, health=${health_status})."
      fi
      printf '  ... sync=%s health=%s op=%s\n' "${sync_status:-?}" "${health_status:-?}" "${op_phase:-?}"
      sleep 5
    done
    if (( SECONDS >= deadline )); then
      warn "Timeout nach ${SYNC_TIMEOUT}s. Letzter Status: sync=${sync_status:-?}, health=${health_status:-?}."
      warn "Status live prüfen: kubectl get application ${APPLICATION} -n ${NS} -o wide"
      exit 1
    fi
  fi
  ok "'${APPLICATION}' aus Branch '${BRANCH}' auf '${ENVIRONMENT}' deployt."
else
  ok "Revision gesetzt (--no-sync aktiv, kein Sync ausgeführt)."
fi
