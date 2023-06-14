#!groovy

def workerNode = "devel11"

pipeline {
	agent {label workerNode}
	options {
		timestamps()
	}
	tools {
		jdk "jdk11"
        maven "Maven 3"
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
				sh "mvn verify pmd:pmd javadoc:aggregate"

				junit testResults: 'target/surefire-reports/TEST-*.xml'

				script {
					def java = scanForIssues tool: [$class: 'Java']
					def javadoc = scanForIssues tool: [$class: 'JavaDoc']
					publishIssues issues: [java, javadoc]

					def pmd = scanForIssues tool: [$class: 'Pmd'], pattern: '**/target/pmd.xml'
					publishIssues issues: [pmd]
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
