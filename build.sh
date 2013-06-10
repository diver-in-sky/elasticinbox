#!/bin/bash
MAVEN_OPTS="-Xmx1024m" mvn -P assembly clean package -DskipITs -DskipTests