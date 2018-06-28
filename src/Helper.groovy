

class Helper implements Serializable {
    def steps
    // String credentialsId

    Helper(steps) {
        this.steps = steps

    }

    def helloWorld(String name){
        steps.sh "echo $name"

    }

    def showGITTag() {
        steps.sh 'git describe --exact-match --tags $(git log -n1 --pretty=\'%h\')'
    }

    def checkout(String url, String tag) {
        def credentials = Config.getConfig().get("credentials")
        steps.checkout([$class: 'GitSCM', branches: [[name: computeGitRef("${tag}")]], doGenerateSubmoduleConfigurations: false, extensions: [[$class: 'CheckoutOption', timeout: 1000]], submoduleCfg: [], userRemoteConfigs: [[credentialsId: "${credentials}", url: "${url}"]]])
        // showGITTag()
    }

    def checkoutRelativeTargetDir(String url, String relativeTargetDir, String tag) {
        def credentials = Config.getConfig().get("credentials")
        steps.checkout([$class: 'GitSCM', branches: [[name: computeGitRef("${tag}")]], doGenerateSubmoduleConfigurations: false, extensions: [[$class: 'CheckoutOption', timeout: 1000], [$class: 'RelativeTargetDirectory', relativeTargetDir: "${relativeTargetDir}"]], submoduleCfg: [], userRemoteConfigs: [[credentialsId: "${credentials}", url: "${url}"]]])
        //showGITTag()
    }

    def checkoutFromBranchRelativeTargetDir(String url, String relativeTargetDir, String branch) {
        def credentials = Config.getConfig().get("credentials")
        steps.checkout([$class: 'GitSCM', branches: [[name: '**/${branch}']], doGenerateSubmoduleConfigurations: false, extensions: [[$class: 'CheckoutOption', timeout: 1000], [$class: 'RelativeTargetDirectory', relativeTargetDir: "${relativeTargetDir}"]], submoduleCfg: [], userRemoteConfigs: [[credentialsId: "${credentials}", url: "${url}"]]])
        //showGITTag()
    }

    def mvn(args) {
        steps.sh "${steps.tool 'M3'}/bin/mvn  ${args}"
    }



    def archiveArtifacts(String pattern) {
        steps.step([$class: 'ArtifactArchiver', artifacts: "${pattern}", fingerprint: true])
    }




}