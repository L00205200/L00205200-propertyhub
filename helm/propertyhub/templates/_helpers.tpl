{{- define "propertyhub.name" -}}
{{- .Chart.Name | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{- define "propertyhub.labels" -}}
app.kubernetes.io/name: {{ include "propertyhub.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
app.kubernetes.io/version: {{ .Values.image.tag | quote }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
helm.sh/chart: {{ printf "%s-%s" .Chart.Name .Chart.Version }}
app: propertyhub
{{- end -}}

{{- define "propertyhub.selectorLabels" -}}
app.kubernetes.io/name: {{ include "propertyhub.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end -}}
