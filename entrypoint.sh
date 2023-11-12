#!/bin/bash

echo "Du bist hier!"

# Warten Sie die durch PARTICIPATION_DURATION angegebene Zeit.
# Falls PARTICIPATION_DURATION nicht gesetzt ist, verwenden Sie einen Standardwert von 300 Sekunden.
sleep ${PARTICIPATION_DURATION}

echo "Du bist da!"

# Stoppen Sie den Container, indem Sie das Hauptprozess-Signal beenden.
kill 1

