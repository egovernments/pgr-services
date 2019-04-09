#!/bin/bash

#set -ex
ci_image=egovio/ci:0.0.4
db=`sudo docker run -d postgres:9.6`
echo "Started db container: $db"

pwd=`pwd`
ci=`sudo docker run -d -v $HOME/.m2:/root/.m2 -v $pwd:/var/lib/egov-services:rw -w /var/lib/egov-services --net=container:${db} ${ci_image} /bin/bash -c "set -e; cd $1;mvn clean verify package -U -s /var/lib/egov-services/settings.xml;"`
echo "Started ci container to run build: $ci"

echo "Attaching to ci container: $ci"
docker attach $ci
result=$?

if [[ $result == 0 ]]; then
    echo "Commiting ci container: $ci"
    docker commit $ci ${ci_image}
fi

echo "Stopping db-container: $db"
docker rm -f $db

echo "Stopping ci-container: $ci"
docker rm -f -v $ci

exit $result
