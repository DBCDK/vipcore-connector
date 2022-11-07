#!groovy

def workerNode = "devel10"

pipeline {
	agent {label workerNode}
	options {
		timestamps()
	}
	tools {
		jdk "jdk11"
        maven "Maven 3"
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
