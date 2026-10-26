#!/usr/bin/env bash
# Gera logins validos, invalidos e bloqueios para popular o dashboard do Grafana.
# Uso: ./scripts/gerar-trafego.sh [http://localhost:8080]
URL=${1:-http://localhost:8080}
J="Content-Type: application/json"

login() { curl -s -o /dev/null -w "POST /login %-8s -> %{http_code}\n" -H "$J" \
            -d "{\"username\":\"$1\",\"senha\":\"$2\"}" "$URL/login" | sed "s/%-8s/$1/"; }

echo "== Cadastrando usuarios (ROLE_MANUTENCAO) =="
for u in carlos bia leo duda rafa; do
  curl -s -o /dev/null -w "POST / $u -> %{http_code}\n" -u manutencao:manut123 -H "$J" \
       -d "{\"username\":\"$u\",\"senha\":\"${u}123\"}" "$URL/"
done

echo "== Logins validos =="
for i in $(seq 1 6); do login maria maria123; login joao joao123; login carlos carlos123; sleep 1; done

echo "== Logins invalidos (bloqueia ana, bia e leo) =="
for u in ana bia leo; do login $u errada; login $u errada; done
login fantasma qualquer

echo "== Usuario bloqueado tentando com a senha certa =="
login ana ana123

echo "== Admin desbloqueia a ana (ROLE_ADMIN) =="
curl -s -o /dev/null -w "PUT /unlock/ana -> %{http_code}\n" -u admin:admin123 -X PUT "$URL/unlock/ana"
login ana ana123

echo "== Troca de senha =="
curl -s -o /dev/null -w "PUT /trocarsenha/joao -> %{http_code}\n" -X PUT -H "$J" \
     -d '{"atual":"joao123","nova":"joao456"}' "$URL/trocarsenha/joao"
login joao joao456

echo "== Usuarios cadastrados (ROLE_MANUTENCAO) =="
curl -s -u manutencao:manut123 "$URL/"; echo
