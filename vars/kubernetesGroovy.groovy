def call(String registryCred = 'a', String registryin = 'a', String docTag = 'a', String grepo = 'a', String gbranch = 'a', String gitcred = 'a') {

pipeline {
environment { 
		registryCredential = "${registryCred}"
		registry = "$registryin" 	
		dockerTag = "${docTag}_$BUILD_NUMBER"
		gitRepo = "${grepo}"
		gitBranch = "${gbranch}"
		gitCredId = "${gitcred}"
	}
		
	agent { label 'k8s' }
	

	stages {
		stage("GIT SCM"){
			steps {
				 checkout([$class: 'GitSCM', branches: [[name: "$gitBranch"]], extensions: [], userRemoteConfigs: [[credentialsId: "$gitCredId", url: "$gitRepo"]]])
			}
		}	
					
		stage('BUILD IMAGE') { 
			 steps { 
				 script { 
					 dockerImage = docker.build("$registry:$dockerTag") 
				 }
			} 
		}
					
		stage('PUSH HUB') { 
			 steps { 
				 script { 
					 docker.withRegistry( '', "$registryCredential" ) { 
						 dockerImage.push() 
					}
				}		
			} 
		}
					
		stage('DEPLOY') {
			steps {
				script { 
					      sh "kubectl set image deployment/webapp webapp=$registry:$dockerTag --record"
					}
				} 
			}
		}
	}
			  
}
