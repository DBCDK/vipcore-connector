#!groovy

def workerNode = "devel12"

pipeline {
	agent {label workerNode}
	options {
		timestamps()
	}
	triggers {
		pollSCM("H/03 * * * *")
		upstream(upstreamProjects: "Docker-payara6-bump-trigger",
				threshold: hudson.model.Result.SUCCESS)
	}
	stages {
		stage("clear workspace") {
			steps {
				deleteDir()
				checkout scm
			}
		}
		stage("verify") {
			steps {
				sh "mvn-B -Dmaven.repo.local=$WORKSPACE/.repo --no-transfer-progress verify javadoc:aggregate"

				junit testResults: 'target/surefire-reports/TEST-*.xml'

                def sonarOptions = "-Dsonar.branch.name=$BRANCH_NAME"
                if (env.BRANCH_NAME != 'master') {
                    sonarOptions += " -Dsonar.newCode.referenceBranch=master"
                }
                status += sh returnStatus: true, script: """
                    mvn -B -Dmaven.repo.local=$WORKSPACE/.repo --no-transfer-progress $sonarOptions sonar:sonar
                """

                junit testResults: '**/target/*-reports/*.xml'

                def javadoc = scanForIssues tool: [$class: 'JavaDoc']
                publishIssues issues: [javadoc], unstableTotalAll: 1

                if (status != 0) {
                    error("build failed")
                }
			}
		}
        stage("quality gate") {
            steps {
                timeout(time: 1, unit: 'HOURS') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }
		stage("deploy") {
			when {
				branch "main"
			}
			steps {
				sh "mvn jar:jar deploy:deploy"
			}
		}
	}
}
