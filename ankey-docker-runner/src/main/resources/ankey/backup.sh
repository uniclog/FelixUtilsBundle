#!/usr/bin/env bash
set -e

BASE_DIR="/opt/ankey"
BACKUP_DIR="${BASE_DIR}/conf_backup"

FILES_TO_BACKUP=(
  "logger/logback.xml"
  "conf/repo.jdbc.json"
  "conf/search.connection.json"
  "conf/search.settings.json"
  "conf/kafka.configuration.json"
  "conf/report.microservices.json"
  "conf/bpmn.microservices.json"
)

echo "=== Config backup/restore stage ==="

mkdir -p "$BACKUP_DIR"

if [ -z "$(ls -A "$BACKUP_DIR" 2>/dev/null)" ]; then
  echo "Backup directory is empty. Creating backup..."

  for item in "${FILES_TO_BACKUP[@]}"; do
    SRC="${BASE_DIR}/${item}"
    DST="${BACKUP_DIR}/${item}"

    if [ -e "$SRC" ]; then
      mkdir -p "$(dirname "$DST")"
      cp -a "$SRC" "$DST"
      echo "Backed up: $item"
    else
      echo "WARNING: $SRC not found, skipping"
    fi
  done
else
  echo "Backup exists. Restoring configs..."

  for item in "${FILES_TO_BACKUP[@]}"; do
    SRC="${BACKUP_DIR}/${item}"
    DST="${BASE_DIR}/${item}"

    if [ -e "$SRC" ]; then
      rm -rf "$DST"
      mkdir -p "$(dirname "$DST")"
      cp -a "$SRC" "$DST"
      echo "Restored: $item"
    else
      echo "WARNING: backup for $item not found, skipping restore"
    fi
  done
fi