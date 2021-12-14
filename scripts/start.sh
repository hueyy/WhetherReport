#!/bin/sh

set -e

# echo "*/30	*	*	*	*	run-parts	/etc/periodic/30min" >> /etc/crontabs/root
# crontab -l

# crond -f -l 8 &
exec "$@"