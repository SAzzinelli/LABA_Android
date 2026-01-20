#!/bin/bash

# Script per rimuovere file duplicati di Hilt prima della build

cd "/Users/simone/Desktop/App LABA/LABA_Android"

# Rimuove tutti i file con " 2.class", " 3.class", etc.
find app/build -type f -name "* [0-9]*.class" -delete 2>/dev/null

# Rimuove anche i file .bin duplicati
find app/build -type f -name "* [0-9]*.bin" -delete 2>/dev/null

echo "File duplicati rimossi"
