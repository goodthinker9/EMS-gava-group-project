#!/usr/bin/env bash
# ─────────────────────────────────────────────────────────────
#  Build & run script for the Employee Management System
#  Requires: JDK 17+, mysql-connector-j jar in lib/
# ─────────────────────────────────────────────────────────────

set -e

JAR="lib/mysql-connector-j-8.3.0.jar"   # adjust version as needed
SRC_DIR="src"
OUT_DIR="out"

# 1. Compile
echo "Compiling..."
mkdir -p "$OUT_DIR"
find "$SRC_DIR" -name "*.java" | xargs javac -cp "$JAR" -d "$OUT_DIR"

# 2. Run
echo "Launching EMS..."
java -cp "$OUT_DIR:$JAR" Main
