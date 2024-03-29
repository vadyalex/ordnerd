#!/usr/bin/bash

TAG="$1"
WEBHOOK="$2"
TOKEN="$3"

if [ -z "$(docker service ls | grep ordnerd)" ]; then
  echo "Service ordnerd does not exist! Create using ${TAG} image..";

  docker service create                                                             \
    --limit-memory 512m                                                             \
    --name 'ordnerd'                                                                \
    --replicas 1                                                                    \
    --update-delay 10s                                                              \
    --env PORT=5000                                                                 \
    --env WEBHOOK=${WEBHOOK}                                                        \
    --env TOKEN=${TOKEN}                                                            \
    --network traefik                                                               \
    --label 'traefik.enable=true'                                                   \
    --label 'traefik.http.services.ordnerd.loadbalancer.server.port=5000'           \
    --label 'traefik.http.routers.ordnerd.entrypoints=websecure'                    \
    --label 'traefik.http.routers.ordnerd.rule=Host(`ordnerd.app.vadyalex.me`)'     \
    --label 'traefik.http.routers.ordnerd.tls=true'                                 \
    --label 'traefik.http.routers.ordnerd.tls.certresolver=letsencrypt'             \
    --label 'traefik.http.middlewares.https_redirect.redirectscheme.scheme=https'   \
    --label 'traefik.http.routers.ordnerd_web.entrypoints=web'                      \
    --label 'traefik.http.routers.ordnerd_web.rule=Host(`ordnerd.app.vadyalex.me`)' \
    --label 'traefik.http.routers.ordnerd_web.middlewares=https_redirect'           \
    vadyalex/ordnerd:${TAG}

   echo 'Done!';

else
  echo "Updating ordnerd using ${TAG}";

  docker service update --limit-memory 512m --replicas 1 --update-delay 10s --image "vadyalex/ordnerd:${TAG}" 'ordnerd'

  echo 'Done!';
fi
