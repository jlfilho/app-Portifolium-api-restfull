#!/bin/sh
set -e

storage_dir="${FILE_STORAGE_LOCATION:-/var/lib/portifolium/files}"

mkdir -p "$storage_dir"
chown -R appuser:appgroup "$storage_dir" 2>/dev/null || chmod -R 0775 "$storage_dir"

exec su-exec appuser:appgroup "$@"
