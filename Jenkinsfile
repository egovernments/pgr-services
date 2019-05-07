def app = "";
def commit_id="";
def module_name = "${env.JOB_NAME}";
def service_name = "${env.JOB_BASE_NAME}";
def path = "${module_name}"
def build_wkflo;
def ci_image = "egovio/ci:0.0.4"
def notifier = "";

try {
    node("slave"){
        checkout scm
        sh "git rev-parse --short HEAD > .git/commit-id".trim()
        commit_id = readFile('.git/commit-id')

        def docker_file_exists = fileExists("${path}/Dockerfile")

        code_builder = load("jenkins/code_builder.groovy")
        notifier = load("jenkins/notifier.groovy")
        currentBuild.displayName = "${BUILD_ID}-${commit_id}"

        if (!docker_file_exists) {
          code_builder.build(path, ci_image)
        }
        else {
          archiver = load("jenkins/archiver.groovy")
          image_builder = load("jenkins/image_builder.groovy")
          code_builder.build(path, ci_image)
          archiver.archiveArtifacts(["${path}/target/*.jar", "${path}/target/*.html"])
          image_builder.build(module_name, service_name, commit_id)
          image_builder.publish(service_name, commit_id)
          image_builder.clean(service_name, commit_id)
        }
        // cleanup all files and directories in target directories, but only if the CLEANUP build parameter is set to 'true'
        post {
            cleanup {
                deleteDir()
            }
        }
    }
} catch (e) {
    node{
        echo
        notifier.notifyBuild("FAILED")
        throw e
    }
}