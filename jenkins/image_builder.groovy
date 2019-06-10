def build(module_name, service_name, commit_id){    stage("Build docker image") {

    build_image("${module_name}", "egovio/${service_name}", commit_id)
    String[] migration_locs = [ "${module_name}/src/main/resources/db", "${module_name}/migration"]    
    
    for(i=0; i<migration_locs.length; i++){

        def loc = migration_locs[i] + "/Dockerfile"
        println(loc)
        def file_exists = fileExists(loc)
        if(file_exists)
            build_image( migration_locs[i], "egovio/${service_name}-db", commit_id)
    }
    }
}

def build_image(dockerfile_path, image_name, commit_id){
        sh "cd ${dockerfile_path} && docker build --no-cache -t ${image_name} ."
    sh "docker tag ${image_name} ${image_name}:${BUILD_ID}-${commit_id}"
    sh "docker tag ${image_name} ${image_name}:latest"
}

def publish(service_name, commit_id){
    stage("Publish docker image") {
        sh "docker images | grep egovio/${service_name} | awk '{b=\$1\":\"\$2; print b}' | while read x; do docker push \$x; done"
    }
}


def clean(service_name, commit_id){
    stage("Clean docker image and unused volumes") {
        sh "docker images | grep egovio/${service_name} | awk '{b=\$1\":\"\$2; print b}' | while read x; do docker rmi \$x; done"
    }
}

return this;
