{{/*
Common labels
*/}}
{{- define "praxis-form.labels" -}}
app.kubernetes.io/managed-by: {{ .Release.Service }}
app.kubernetes.io/instance: {{ .Release.Name }}
helm.sh/chart: {{ .Chart.Name }}-{{ .Chart.Version }}
{{- end }}

{{- define "praxis-form.fullname" -}}
{{ .Release.Name }}-{{ .Chart.Name }}
{{- end }}

