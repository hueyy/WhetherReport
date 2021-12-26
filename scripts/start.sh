#!/bin/sh

set -e

crond -f -l 8 &

exec "$@"