pipeline {
    agent any

    stages {
        stage('Build') {
            steps {
                timestamps {
                    echo "Build started"
                    echo "Compiling the Python code..."
                    withEnv(["PATH+PYTHON=/path/to/python/bin"]) {
                        sh 'python -m pip install Flask'
                        sh 'python test_app.py'
                    }
                    echo "Build completed"
                }
            }
        }

        stage('Deploy') {
            environment { // Use the environment block to define the variable
                deployDir = "$WORKSPACE/web"
            }
            steps {
                echo "Deploying the Flask web application..."
                
                // Application Installation and Startup
                sh 'mkdir -p $deployDir' // Use the defined variable here
                sh 'curl -O https://bootstrap.pypa.io/get-pip.py'
                withEnv(["PATH+PYTHON=/usr/bin/python3"]) {
                    sh 'python get-pip.py --user'
                    sh 'python -m pip install Flask'
                }

                // Start Flask Application in the Background
                sh "nohup python $deployDir/web.py > /dev/null 2>&1 &"

                echo "Flask web application has been deployed."

                // Wait for the application to start
                sleep 5

                // Send a POST request to /shutdown to stop the Flask application gracefully
                sh """
                    python - <<EOF
                    import requests
                    requests.post("http://127.0.0.1/shutdown")
                    EOF
                """

                echo "Flask web application has been stopped."
            }
        }
    }
}
